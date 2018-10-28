package fr.layer4.hhsl.registry;

import fr.layer4.hhsl.prompt.Prompter;
import fr.layer4.hhsl.store.LockableLocalStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocalRegistryResolver implements RegistryResolver {

    @Autowired
    private LockableLocalStore lockableLocalStore;

    @Autowired
    private Prompter prompter;

    @Override
    public String getType() {
        return "local";
    }

    @Override
    public Registry prepare(RegistryConnection registryConnection) {
        // Information about registry connection is not useful here
        LocalRegistry localRegistry = new LocalRegistry(lockableLocalStore, prompter);
        return localRegistry;
    }
}
