package fr.layer4.hhsl;

import lombok.Data;

import java.net.URI;
import java.util.List;

@Data
public class LocalRegistryService implements RegistryService {

    private String dbFile;

    public List<Registry> findRegistries() {
        return null;
    }

    public Registry findRegistryByName(String name) {
        return null;
    }

    public List<Cluster> findClusters() {
        return null;
    }

    public void addCluster(Registry registry, String name, URI uri) {

    }
}
