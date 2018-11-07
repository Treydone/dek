package fr.layer4.hhsl.registry;

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

import fr.layer4.hhsl.ClusterService;
import fr.layer4.hhsl.LocalClusterService;
import fr.layer4.hhsl.prompt.Prompter;
import fr.layer4.hhsl.store.LocalLockableStore;
import lombok.Data;

@Data
public class LocalRegistry implements Registry {

    private LocalClusterService localClusterService;

    private final RegistryConnection registryConnection;
    private final LocalLockableStore localLockableStore;
    private final Prompter prompter;

    @Override
    public RegistryConnection getUnderlyingConnection() {
        return this.registryConnection;
    }

    @Override
    public void init(RegistryConnection registryConnection) {
        this.localClusterService = new LocalClusterService(this.localLockableStore, this.prompter);
    }

    @Override
    public ClusterService getClusterService() {
        return this.localClusterService;
    }
}
