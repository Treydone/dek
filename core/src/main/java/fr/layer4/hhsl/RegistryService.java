package fr.layer4.hhsl;

import java.net.URI;
import java.util.List;

public interface RegistryService {

    List<Registry> findRegistries();

    Registry findRegistryByName(String name);

    List<Cluster> findClusters();

    void addCluster(Registry registry, String name, URI uri);
}
