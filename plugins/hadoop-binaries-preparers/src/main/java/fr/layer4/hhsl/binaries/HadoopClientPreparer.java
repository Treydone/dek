/*-
 * #%L
 * HHSL
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
package fr.layer4.hhsl.binaries;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.layer4.hhsl.DefaultServices;
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
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class HadoopClientPreparer extends AbstractApacheClientPreparer {

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
                || DefaultServices.HBASE.equalsIgnoreCase(service)
                || DefaultServices.HIVE.equalsIgnoreCase(service)
                || DefaultServices.SQOOP.equalsIgnoreCase(service)
                || DefaultServices.SPARK.equalsIgnoreCase(service)
                ; // Don't care about the versions
    }

    @Override
    public Map<String, String> prepare(String basePath, String service, String version, boolean force) {
        String archive = "hadoop-" + version + ".tar.gz";

        // Check if archive if already present
        if (force || !Files.exists(Paths.get(basePath, archive))) {
            download(basePath, version, archive);
        }

        // Check signature
        boolean isSameSignature = compareLocalAndRemoteSignature(basePath, archive, version);
        if (!isSameSignature) {
            // Signature is different, try to redownload the archive
            download(basePath, version, archive);
            isSameSignature = compareLocalAndRemoteSignature(basePath, archive, version);
            if (!isSameSignature) {
                throw new RuntimeException("Incorrect signature after redownload");
            }
        }

        // Unpack
        File dest = new File(basePath, FilenameUtils.getBaseName(archive));
        if (force || !dest.exists()) {
            try {
                uncompress(new File(basePath, archive), dest);
            } catch (IOException e) {
                throw new RuntimeException("Can not extract client", e);
            }
        }

        // Add winutils if Windows
        if (OSUtils.IS_WINDOWS) {
            log.info("Download winutils...");

            String winutilsHadoopVersion = findWinUtilsMatchingVersion(version);

            downloadWinUtilsBinaries(this.restTemplate, force, dest, winutilsHadoopVersion);
        }

        // Update environment variables
        Map<String, String> envVars = new HashMap<>();
        envVars.put("HADOOP_HOME", dest.getAbsolutePath());
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
                throw new RuntimeException("Can not copy " + c.getPath() + " to " + dest.getAbsolutePath(), e);
            }
        });
    }

    protected static String findWinUtilsMatchingVersion(String version) {
        String winutilsHadoopVersion;
        if (version.compareTo("2.6.0") <= 0) {
            winutilsHadoopVersion = "2.6.0";
        } else if (version.compareTo("2.6.0") > 0 && version.compareTo("2.6.3") <= 0) {
            winutilsHadoopVersion = "2.6.3";
        } else if (version.compareTo("2.6.3") > 0 && version.compareTo("2.7") < 0) {
            winutilsHadoopVersion = "2.6.4";
        } else if (version.compareTo("2.7") > 0 && version.compareTo("2.8") < 0) {
            winutilsHadoopVersion = "2.7.1";
        } else if (version.compareTo("2.8") > 0 && version.compareTo("2.8.3") < 0) {
            winutilsHadoopVersion = "2.8.1";
        } else if (version.compareTo("2.8.3") >= 0 && version.compareTo("3.0.0") < 0) {
            winutilsHadoopVersion = "2.8.3";
        } else if (version.compareTo("3.0.0") >= 0) {
            winutilsHadoopVersion = "3.0.0";
        } else {
            throw new RuntimeException("Can not find a compatible winutils version for Hadoop version " + version);
        }
        return winutilsHadoopVersion;
    }

    protected boolean compareLocalAndRemoteSignature(String basePath, String archive, String version) {

        // Get local SHA-256
        String localSha256 = getLocalSha256(basePath, archive);

        // Get remote SHA-256
        String remoteSha256 = getRemoteSha256(archive, version);

        return remoteSha256.equalsIgnoreCase(localSha256);
    }

    protected String getLocalSha256(String basePath, String archive) {
        Path path = Paths.get(basePath, archive);
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not found...", e);
        }
        byte[] hash;
        try {
            hash = digest.digest(FileUtils.readFileToByteArray(path.toFile()));
        } catch (IOException e) {
            throw new RuntimeException("Can not compute local SHA-256", e);
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
                if (("/build/source/target/artifacts/" + archive + ":").equals(line.trim())) {
                    line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    line = line.trim();
                    if (line.startsWith("SHA256 = ")) {
                        remoteSha256 = line.replace("SHA256 = ", "").replace(" ", "");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (remoteSha256 == null) {
            throw new RuntimeException("Can not retrieve remote SHA-256");
        }
        return remoteSha256;
    }

    protected String getApachePart(String archive, String version) {
        return "hadoop/common/hadoop-" + version + "/" + archive;
    }
}
