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
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpResponseException;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

@Slf4j
public abstract class AbstractApacheClientPreparer extends AbstractClientPreparer {

    private final ApacheMirrorFinder apacheMirrorFinder;

    public AbstractApacheClientPreparer(CloseableHttpClient client, ApacheMirrorFinder apacheMirrorFinder) {
        super(client);
        this.apacheMirrorFinder = apacheMirrorFinder;
    }

    protected abstract String getApachePart(String archive, String version);

    protected String download(Path basePath, String version, String archive) {
        // Download
        String apachePart = getApachePart(archive, version);
        URI uri = this.apacheMirrorFinder.resolve(apachePart);
        try {
            return download(basePath, uri);
        } catch (HttpResponseException e) {
            log.warn("Can not download client. Try on archives...", e);
            try {
                return download(basePath, URI.create("https://archive.apache.org/dist/" + apachePart));
            } catch (IOException e1) {
                throw new DekException("Can not download the client even on archive", e1);
            }
        } catch (IOException e) {
            throw new DekException("Can not download the client", e);
        }
    }
}
