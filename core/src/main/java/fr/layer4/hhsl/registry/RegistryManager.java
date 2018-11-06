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
package fr.layer4.hhsl.registry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RegistryManager {

    @Autowired
    private RegistryConnectionManager registryConnectionManager;

    @Autowired(required = false)
    private List<RegistryResolver> resolvers;

    private Function<RegistryConnection, Registry> registryConnectionRegistryFunction = c -> {
        Registry registry = fromType(c.getUri().getScheme()).prepare(c);
        registry.init(c);
        return registry;
    };

    public RegistryResolver fromType(String type) {
        return this.resolvers.stream().filter(r -> type.equals(r.getType())).findFirst().get();
    }

    public Collection<String> getAvailableTypes() {
        return this.resolvers.stream().map(RegistryResolver::getType).collect(Collectors.toList());
    }

    public Registry getFromName(String registryName) {
        RegistryConnection registryConnection = registryConnectionManager.getRegistry(registryName);
        return registryConnectionRegistryFunction.apply(registryConnection);
    }

    public List<Registry> all() {
        List<RegistryConnection> registryConnections = registryConnectionManager.listRegistries();
        return registryConnections.stream().map(registryConnectionRegistryFunction).collect(Collectors.toList());
    }
}
