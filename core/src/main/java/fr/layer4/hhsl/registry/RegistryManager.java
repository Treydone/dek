package fr.layer4.hhsl.registry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RegistryManager {

    @Autowired
    private RegistryConnectionManager registryConnectionManager;

    @Autowired(required = false)
    private List<RegistryResolver> resolvers;

    public RegistryResolver fromType(String type) {
        return this.resolvers.stream().filter(r -> type.equals(r.getType())).findFirst().get();
    }

    public Collection<String> getAvailableTypes() {
        return this.resolvers.stream().map(RegistryResolver::getType).collect(Collectors.toList());
    }

    public Registry getFromName(String registryName) {
        RegistryConnection registryConnection = registryConnectionManager.getRegistry(registryName);
        Registry registry = fromType(registryConnection.getUri().getScheme()).prepare(registryConnection);
        registry.init(registryConnection);
        return registry;
    }
}
