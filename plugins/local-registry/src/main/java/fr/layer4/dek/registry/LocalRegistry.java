package fr.layer4.dek.registry;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.layer4.dek.store.LocalSecuredStore;
import lombok.Data;

@Data
public class LocalRegistry implements Registry {

    private LocalClusterService localClusterService;

    private final RegistryConnection registryConnection;
    private final LocalSecuredStore localSecuredStore;
    private final ObjectMapper objectMapper;

    @Override
    public RegistryConnection getUnderlyingConnection() {
        return this.registryConnection;
    }

    @Override
    public void init(RegistryConnection registryConnection) {
        this.localClusterService = new LocalClusterService(this.localSecuredStore, this.objectMapper);
    }

    @Override
    public ClusterService getClusterService() {
        return this.localClusterService;
    }
}
