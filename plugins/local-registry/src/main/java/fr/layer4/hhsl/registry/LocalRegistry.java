package fr.layer4.hhsl.registry;

import fr.layer4.hhsl.ClusterService;
import fr.layer4.hhsl.LocalClusterService;
import fr.layer4.hhsl.prompt.Prompter;
import fr.layer4.hhsl.store.LockableLocalStore;
import lombok.Data;

@Data
public class LocalRegistry implements Registry {

    private LocalClusterService localClusterService;

    private final LockableLocalStore lockableLocalStore;
    private final Prompter prompter;

    @Override
    public RegistryConnection getUnderlyingConnection() {
        return null;
    }

    @Override
    public void init(RegistryConnection registryConnection) {
        localClusterService = new LocalClusterService(lockableLocalStore, prompter);
    }

    @Override
    public ClusterService getClusterService() {
        return localClusterService;
    }
}
