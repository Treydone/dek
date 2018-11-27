/*-
 * #%L
 * DEK
 * %%
 * Copyright (C) 2018 Layer4
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package fr.layer4.dek.binaries;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.layer4.dek.DefaultServices;
import fr.layer4.dek.DekException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.bouncycastle.util.encoders.Hex;
import org.jline.utils.OSUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class HadoopClientPreparer extends AbstractApacheHadoopClientPreparer {

    public static final String HDP_260 = "2.6.0";
    public static final String HDP_263 = "2.6.3";
    public static final String HDP_264 = "2.6.4";
    public static final String HDP_27 = "2.7";
    public static final String HDP_28 = "2.8";
    public static final String HDP_283 = "2.8.3";
    public static final String HDP_300 = "3.0.0";
    public static final String HDP_281 = "2.8.1";
    public static final String HDP_271 = "2.7.1";
    public static final String SHA_256 = "SHA256 = ";

    private final RestTemplate restTemplate;

    @Autowired
    public HadoopClientPreparer(CloseableHttpClient client, RestTemplate restTemplate, ApacheMirrorFinder apacheMirrorFinder) {
        super(client, apacheMirrorFinder);
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean isCompatible(String service, String version) {
        return DefaultServices.HDFS.equalsIgnoreCase(service)
                || DefaultServices.YARN.equalsIgnoreCase(service)
//                || DefaultServices.HBASE.equalsIgnoreCase(service)
//                || DefaultServices.HIVE.equalsIgnoreCase(service)
//                || DefaultServices.SQOOP.equalsIgnoreCase(service)
//                || DefaultServices.SPARK.equalsIgnoreCase(service)
                ; // Don't care about the versions
    }

    @Override
    public Map<String, List<String>> prepare(Path basePath, String service, String version, boolean force) {
        String nameAndVersion = "hadoop-" + version;
        String archive = nameAndVersion + ".tar.gz";
        File dest = basePath.resolve(FilenameUtils.getBaseName(archive)).toFile();
        log.debug("Preparing {} to {}", archive, dest);

        // Check if archive if already present
        if (force || !basePath.resolve(archive).toFile().exists()) {
            download(basePath, version, archive);
        }

        // Check signature
        try {
            boolean isSameSignature = compareLocalAndRemoteSignature(basePath, archive, version);
            if (!isSameSignature) {
                // Signature is different, try to redownload the archive
                download(basePath, version, archive);
                isSameSignature = compareLocalAndRemoteSignature(basePath, archive, version);
                if (!isSameSignature) {
                    throw new DekException("Incorrect signature after redownload");
                }
            }
        } catch (RestClientException e) {
            log.warn("Can not get the remote signature", e);
        }

        // Unpack
        File source = basePath.resolve(archive).toFile();
        log.debug("Uncompress {} to {}", source, dest);
        if (force || !dest.exists()) {
            try {
                uncompress(source, dest);
            } catch (IOException e) {
                throw new DekException("Can not extract client", e);
            }
        }

        dest = new File(dest, nameAndVersion);

        // Chmod+x
        Path bin = dest.toPath().resolve("bin");
        try {
            chmodExecuteForEachFile(bin);
        } catch (IOException e) {
            throw new DekException("Can not chmod files in " + bin.toAbsolutePath().toString(), e);
        }

        // Add winutils if Windows
        if (OSUtils.IS_WINDOWS) {
            log.info("Download winutils...");
            String winutilsHadoopVersion = findWinUtilsMatchingVersion(version);
            downloadWinUtilsBinaries(this.restTemplate, force, dest, winutilsHadoopVersion);
        }

        // Update environment variables
        Map<String, List<String>> envVars = new HashMap<>();
        envVars.put("HADOOP_HOME", Collections.singletonList(dest.getAbsolutePath()));
        envVars.put("PATH", Collections.singletonList(new File(dest, "bin").getAbsolutePath()));
        return envVars;
    }


    @Data
    public static class Content {
        private String name;
        private String path;
        @JsonProperty("download_url")
        private String downloadUrl;
        private String type;
    }

    protected static void downloadWinUtilsBinaries(RestTemplate restTemplate, boolean force, File dest, String winutilsHadoopVersion) {
        File binDirectory = new File(dest, "bin");
        log.debug("Download winutils {} to {}", winutilsHadoopVersion, binDirectory);
        if (!binDirectory.exists()) {
            binDirectory.mkdir();
        }
        ResponseEntity<List<Content>> contents = restTemplate.exchange("https://api.github.com/repos/steveloughran/winutils/contents/hadoop-" + winutilsHadoopVersion + "/bin?ref=master", HttpMethod.GET, null, new ParameterizedTypeReference<List<Content>>() {
        });
        contents.getBody().forEach(c -> {
            try {
                File binFile = new File(binDirectory, c.getName());
                if (force || !binFile.exists()) {
                    FileUtils.copyInputStreamToFile(restTemplate.getRequestFactory().createRequest(URI.create(c.getDownloadUrl()), HttpMethod.GET).execute().getBody(), binFile);
                }
            } catch (IOException e) {
                throw new DekException("Can not copy " + c.getPath() + " to " + dest.getAbsolutePath(), e);
            }
        });
    }

    protected static String findWinUtilsMatchingVersion(String version) {
        String winutilsHadoopVersion;
        if (version.compareTo(HDP_260) <= 0) {
            winutilsHadoopVersion = HDP_260;
        } else if (version.compareTo(HDP_260) > 0 && version.compareTo(HDP_263) <= 0) {
            winutilsHadoopVersion = HDP_263;
        } else if (version.compareTo(HDP_263) > 0 && version.compareTo(HDP_27) < 0) {
            winutilsHadoopVersion = HDP_264;
        } else if (version.compareTo(HDP_27) > 0 && version.compareTo(HDP_28) < 0) {
            winutilsHadoopVersion = HDP_271;
        } else if (version.compareTo(HDP_28) > 0 && version.compareTo(HDP_283) < 0) {
            winutilsHadoopVersion = HDP_281;
        } else if (version.compareTo(HDP_283) >= 0 && version.compareTo(HDP_300) < 0) {
            winutilsHadoopVersion = HDP_283;
        } else if (version.compareTo(HDP_300) >= 0) {
            winutilsHadoopVersion = HDP_300;
        } else {
            throw new DekException("Can not find a compatible winutils version for Hadoop version " + version);
        }
        return winutilsHadoopVersion;
    }

    protected boolean compareLocalAndRemoteSignature(Path basePath, String archive, String version) {

        // Get local SHA-256
        String localSha256 = getLocalSha256(basePath, archive);

        // Get remote SHA-256
        String remoteSha256 = getRemoteSha256(archive, version);

        return remoteSha256.equalsIgnoreCase(localSha256);
    }

    protected String getLocalSha256(Path basePath, String archive) {
        Path path = basePath.resolve(archive);
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new DekException("SHA-256 not found...", e);
        }
        byte[] hash;
        try {
            hash = digest.digest(FileUtils.readFileToByteArray(path.toFile()));
        } catch (IOException e) {
            throw new DekException("Can not compute local SHA-256", e);
        }
        return new String(Hex.encode(hash));
    }

    protected String getRemoteSha256(String archive, String version) {
        ResponseEntity<String> rawResponse = restTemplate.getForEntity("https://dist.apache.org/repos/dist/release/" + getApachePart(archive, version) + ".mds", String.class);
        String remoteSha256 = null;
        try (BufferedReader reader = new BufferedReader(new StringReader(rawResponse.getBody()))) {
            for (; ; ) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                // Case for /build/source/target/artifacts/hadoop-2.8.5.tar.gz:\nSHA256 =...
                if (("/build/source/target/artifacts/" + archive + ":").equals(line.trim())) {
                    line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    line = line.trim();
                    int i = line.indexOf(SHA_256);
                    if (i > -1) {
                        remoteSha256 = line.substring(i + SHA_256.length()).replace(" ", "");
                        break;
                    }
                }
                // Case for "hadoop-2.7.7.tar.gz: SHA256 =....."
                else if (line.trim().startsWith(archive)) {
                    int i = line.indexOf(SHA_256);
                    if (i > -1) {
                        remoteSha256 = line.substring(i + SHA_256.length()).replace(" ", "");
                        boolean loop = true;
                        while (loop) {
                            line = reader.readLine();
                            if (line == null) {
                                break;
                            }
                            line = line.trim();
                            if (!line.trim().startsWith(archive)) {
                                remoteSha256 += line.replace(" ", "");
                            } else {
                                loop = false;
                            }
                        }
                        break;
                    }

                }
            }
        } catch (IOException e) {
            throw new DekException(e);
        }
        if (remoteSha256 == null) {
            throw new DekException("Can not retrieve remote SHA-256");
        }
        return remoteSha256;
    }

    protected String getApachePart(String archive, String version) {
        return "hadoop/common/hadoop-" + version + "/" + archive;
    }
}
