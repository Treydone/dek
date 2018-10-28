package fr.layer4.hhsl.info;

import fr.layer4.hhsl.Cluster;

import java.util.Collection;
import java.util.Map;

public interface ClusterInfoResolver {

    /**
     * Return the type of this cluster info resolver. Ex: "ambari", "cloudera"...
     *
     * @return
     */
    String getType();

    /**
     * Return the available services on a cluster. Ex: "hdfs", "yarn"...
     *
     * @param cluster
     * @return
     */
    Collection<String> resolveAvailableServices(Cluster cluster);

    /**
     * Return the environment variables mandatory to use the cluster.
     *
     * @param cluster
     * @return
     */
    Map<String, String> resolveEnvironmentVariables(Cluster cluster);

    /**
     * Render the configuration files to use the cluster.
     *
     * @param cluster
     * @return
     */
    Map<String, byte[]> renderConfigurationFiles(Cluster cluster);
}
