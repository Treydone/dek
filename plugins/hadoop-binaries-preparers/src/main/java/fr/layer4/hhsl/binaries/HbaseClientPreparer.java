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

import fr.layer4.hhsl.DefaultServices;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class HbaseClientPreparer extends AbstractApacheClientPreparer {

    private final RestTemplate restTemplate;

    @Autowired
    public HbaseClientPreparer(CloseableHttpClient client, RestTemplate restTemplate, ApacheMirrorFinder apacheMirrorFinder) {
        super(client, apacheMirrorFinder);
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean isCompatible(String service, String version) {
        return DefaultServices.HBASE.equalsIgnoreCase(service); // Don't care about the versions
    }

    @Override
    public Map<String, String> prepare(Path basePath, String service, String version, boolean force) {

        String archive = "hbase-" + version + "-bin.tar.gz";
        File dest = basePath.resolve(FilenameUtils.getBaseName(archive)).toFile();
        log.debug("Preparing {} to {}", archive, dest);

        // Check if archive if already present
        if (force || !Files.exists(basePath.resolve(archive))) {
            download(basePath, version, archive);
        }

        // Unpack
        File source = basePath.resolve(archive).toFile();
        log.debug("Uncompress {} to {]", source, dest);
        if (force || !dest.exists()) {
            try {
                uncompress(source, dest);
            } catch (IOException e) {
                throw new RuntimeException("Can not extract client", e);
            }
        }


        // Update environment variables
        Map<String, String> envVars = new HashMap<>();
        envVars.put("HBASE_HOME", dest.getAbsolutePath());
        return envVars;
    }

    protected String getApachePart(String archive, String version) {
        return "hbase/common/hbase-" + version + "/" + archive;
    }
}
