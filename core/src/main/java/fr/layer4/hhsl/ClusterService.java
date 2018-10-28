package fr.layer4.hhsl;

import java.util.List;

public interface ClusterService {

    /**
     * Add a cluster in the specified registry.
     *
     * @param type
     * @param name
     * @param uri
     * @param banner
     */
    void addCluster(String type, String name, String uri, String banner);

    /**
     * Delete a cluster in the specified registry.
     *
     * @param name
     * @return
     */
    void deleteCluster(String name);

    /**
     * List all the clusters for a registry, or all cluster if not registry has been specified.
     * <p>
     * (nullable)
     *
     * @return
     */
    List<Cluster> listClusters();

    /**
     * Retrieve cluster information.
     *
     * @param name
     * @return
     */
    Cluster getCluster(String name);

}
