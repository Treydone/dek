package fr.layer4.hhsl.registry;

import fr.layer4.hhsl.ClusterService;

public interface Registry {

    /**
     * Returns the RegistryConnection used for building this Registry.
     *
     * @return
     */
    RegistryConnection getUnderlyingConnection();

    /**
     * Initialize the registry from a connnection. Typical tasks are connection to remote service, authentication...
     *
     * @param registryConnection
     */
    void init(RegistryConnection registryConnection);

    /**
     * Returns a ClusterService interacting with this registry.
     *
     * @return
     */
    ClusterService getClusterService();
}
