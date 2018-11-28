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

import fr.layer4.dek.DefaultServices;
import fr.layer4.dek.DekException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StormClientPreparer extends AbstractApacheHadoopClientPreparer {

    private final RestTemplate restTemplate;

    @Autowired
    public StormClientPreparer(CloseableHttpClient client, RestTemplate restTemplate, ApacheMirrorFinder apacheMirrorFinder) {
        super(client, apacheMirrorFinder);
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean isCompatible(String service, String version) {
        return DefaultServices.STORM.equalsIgnoreCase(service); // Don't care about the versions
    }

    @Override
    protected Map<String, List<String>> getEnvVars(File dest) {
        Map<String, List<String>> envVars = new HashMap<>();
        envVars.put("STORM_HOME", Collections.singletonList(dest.getAbsolutePath()));
        envVars.put("PATH", Collections.singletonList(new File(dest, "bin").getAbsolutePath()));
        return envVars;
    }

    @Override
    protected String getArchive(String version) {
        return getNameAndVersion(version) + ".tar.gz";
    }

    @Override
    protected String getNameAndVersion(String version) {
        return "apache-storm-" + version;
    }

    @Override
    protected String getApachePart(String archive, String version) {
        return "storm/apache-storm-" + version + "/" + archive;
    }

    @Override
    protected boolean compareLocalAndRemoteSignature(Path basePath, String archive, String version) {
        String localSha512 = getLocalShaX(basePath, archive, "SHA-512");
        String remoteSha512 = getRemoteSha512(archive, version);
        return remoteSha512.equalsIgnoreCase(localSha512);
    }

    protected String getRemoteSha512(String archive, String version) {
        ResponseEntity<String> rawResponse = restTemplate.getForEntity("https://dist.apache.org/repos/dist/release/" + getApachePart(archive, version) + ".sha", String.class);
        String remoteSha512 = null;
        try (BufferedReader reader = new BufferedReader(new StringReader(rawResponse.getBody()))) {
            for (; ; ) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                String prefix = archive + ":";
                if (line.trim().startsWith(prefix)) {
                    int i = line.indexOf(prefix);
                    if (i > -1) {
                        remoteSha512 = line.substring(i + prefix.length()).replace(" ", "");
                        boolean loop = true;
                        while (loop) {
                            line = reader.readLine();
                            if (line == null) {
                                break;
                            }
                            line = line.trim();
                            if (!line.trim().startsWith(archive)) {
                                remoteSha512 += line.replace(" ", "");
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
        if (remoteSha512 == null) {
            throw new DekException("Can not retrieve remote signature");
        }
        return remoteSha512;
    }
}
