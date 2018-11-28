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

import fr.layer4.dek.DekException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@RequiredArgsConstructor
public abstract class AbstractClientPreparer implements ClientPreparer {

    private final CloseableHttpClient client;

    /**
     * Download the resource specified by the uri to the path, and returns the file name.
     *
     * @param path
     * @param uri
     * @return
     * @throws IOException
     */
    protected String download(Path path, URI uri) throws IOException {
        String destFileName = FilenameUtils.getName(uri.getPath());
        HttpGet request = new HttpGet(uri);
        try (CloseableHttpResponse response = this.client.execute(request)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                Files.copy(entity.getContent(), path.resolve(destFileName), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return destFileName;
    }

    protected void chmodExecuteForEachFile(Path dir) throws IOException {
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);
        perms.add(PosixFilePermission.GROUP_EXECUTE);

        try (Stream<Path> list = Files.list(dir)) {
            list.filter(Files::isRegularFile).forEach(p -> {
                PosixFileAttributeView view = Files.getFileAttributeView(p, PosixFileAttributeView.class);
                if (view != null) {
                    try {
                        view.setPermissions(perms);
                    } catch (IOException e) {
                        throw new DekException("Can not set execute attribute for file " + p.toAbsolutePath().toString(), e);
                    }
                }
            });
        }
    }

    /**
     * Uncompress tar.gz files
     *
     * @param source
     * @param dest
     * @throws IOException
     */
    protected void uncompress(File source, File dest) throws IOException {
        try (InputStream input = new FileInputStream(source);
             InputStream buffered = new BufferedInputStream(input);
             InputStream gzip = new GzipCompressorInputStream(buffered);
             ArchiveInputStream tar = new TarArchiveInputStream(gzip)
        ) {
            ArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                if (!tar.canReadEntryData(entry)) {
                    // TODO log something?
                    continue;
                }

                File f = new File(dest, entry.getName());
                if (entry.isDirectory()) {
                    if (!f.isDirectory() && !f.mkdirs()) {
                        throw new IOException("failed to create directory " + f);
                    }
                } else {
                    File parent = f.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("failed to create directory " + parent);
                    }
                    try (OutputStream o = Files.newOutputStream(f.toPath())) {
                        IOUtils.copy(tar, o);
                    }
                }
            }
        }
    }
}
