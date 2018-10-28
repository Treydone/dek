package fr.layer4.hhsl.registry;

import java.util.List;

/**
 * A service managing the connection to registries.
 */
public interface RegistryConnectionManager {

    /**
     * @param name
     * @return
     */
    RegistryConnection getRegistry(String name);

    /**
     * @param name
     */
    void deleteRegistry(String name);

    /**
     * @return
     */
    List<RegistryConnection> listRegistries();

    /**
     * @param name
     * @param uri
     */
    void addRegistry(String name, String uri);
}
