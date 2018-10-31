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
import org.apache.commons.io.FilenameUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class HadoopClientPreparer extends AbstractClientPreparer {

    private final ApacheMirrorFinder apacheMirrorFinder;

    @Autowired
    public HadoopClientPreparer(CloseableHttpClient client, ApacheMirrorFinder apacheMirrorFinder) {
        super(client);
        this.apacheMirrorFinder = apacheMirrorFinder;
    }

    @Override
    public boolean isCompatible(String service, String version) {
        // Contains both HDFS and Yarn client
        return DefaultServices.HDFS.equalsIgnoreCase(service)
                || DefaultServices.YARN.equalsIgnoreCase(service); // Don't care about the versions
    }

    @Override
    public void prepare(String basePath, String service, String version) {
        String archive = "hadoop-" + version + ".tar.gz";

        boolean letDoIt = false;

        // Check if archive if already present
//        if(!Files.exists(Paths.get(basePath, archive))) {
//
//        }
        // TODO

        // Check signature
        // TODO
        //https://dist.apache.org/repos/dist/release/hadoop/common/hadoop-2.8.5/hadoop-2.8.5.tar.gz.mds
        // ex: ...
        // SHA224 = ADF41CD6 1EC2A739 31C7ED9D 1183DB03 C5A985B5 A2296CC9 F0315C22
        // build/source/target/artifacts/hadoop-2.8.5.tar.gz:
        // SHA256 = F9C726DF 693CE2DA A4107886 F603270D 66E7257F 77A92C98 86502D6C D4A884A4
        // build/source/target/artifacts/hadoop-2.8.5.tar.gz:
        // ...

        // Download
        URI uri = apacheMirrorFinder.resolve("hadoop/common/hadoop-" + version + "/" + archive);
        String downloadedFileName;
        try {
            downloadedFileName = download(basePath, uri);
        } catch (IOException e) {
            throw new RuntimeException("Can not download the client");
        }

        // Unpack
        try {
            uncompress(new File(basePath, downloadedFileName), new File(basePath, FilenameUtils.getBaseName(downloadedFileName)));
        } catch (IOException e) {
            throw new RuntimeException("Can not extract client");
        }
    }
}
