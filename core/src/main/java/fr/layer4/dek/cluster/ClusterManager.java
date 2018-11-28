package fr.layer4.dek.cluster;

import fr.layer4.dek.Constants;
import fr.layer4.dek.DekException;
import fr.layer4.dek.ServiceClientAndVersion;
import fr.layer4.dek.auth.Credentials;
import fr.layer4.dek.binaries.BinariesStore;
import fr.layer4.dek.info.ClusterInfoManager;
import fr.layer4.dek.info.ClusterInfoResolver;
import fr.layer4.dek.registry.ClusterService;
import fr.layer4.dek.registry.Registry;
import fr.layer4.dek.registry.RegistryConnection;
import fr.layer4.dek.registry.RegistryManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ClusterManager {

    private final ClusterInfoManager clusterInfoManager;
    private final BinariesStore binariesStore;
    private final RegistryManager registryManager;

    public List<Pair<Registry, Cluster>> getAllClusters(String registryName) {
        // List cluster for all registries...
        List<Registry> all = this.registryManager.all();
        return all.stream().flatMap(r -> this.registryManager.getFromName(registryName).getClusterService().listClusters().stream().map(c -> Pair.of(r, c)))
                .collect(Collectors.toList());
    }

    public void add(String registryName, String type, String name, String uri, String banner) throws IOException {
        Registry registry = this.registryManager.getFromName(registryName);
        ClusterService clusterService = registry.getClusterService();

        ClusterInfoResolver clusterInfoResolver = this.clusterInfoManager.fromType(type);
        Credentials credentials = clusterInfoResolver.getCredentials();

        clusterService.addOrUpdateCluster(type, name, uri, banner, credentials);
        prepare(registryName, name, false);
    }

    public Cluster use(String registryName, String name) {
        Registry registry = this.registryManager.getFromName(registryName);
        RegistryConnection underlyingConnection = registry.getUnderlyingConnection();
        Cluster cluster = registry.getClusterService().getCluster(name).orElseThrow(() -> new RuntimeException("Can not find cluster"));

        Path basePath = Constants.getRootPath();
        Path clusterGeneratedPath = basePath.resolve(Paths.get(underlyingConnection.getId().toString(), cluster.getId().toString()));

        try {
            FileUtils.copyFile(clusterGeneratedPath.resolve(Constants.ENV_SH).toFile(), basePath.resolve(Constants.ENV_SH).toFile());
            FileUtils.copyFile(clusterGeneratedPath.resolve(Constants.ENV_BAT).toFile(), basePath.resolve(Constants.ENV_BAT).toFile());
        } catch (IOException e) {
            throw new DekException(e);
        }
        return cluster;
    }

    public void prepare(String registryName, String name, boolean force) throws IOException {
        Registry registry = this.registryManager.getFromName(registryName);
        RegistryConnection underlyingConnection = registry.getUnderlyingConnection();
        Cluster cluster = registry.getClusterService().getCluster(name).orElseThrow(() -> new RuntimeException("Can not find cluster"));

        Path basePath = Constants.getRootPath();

        Path registryPath = basePath.resolve(underlyingConnection.getId().toString());
        if (!registryPath.toFile().exists()) {
            Files.createDirectory(registryPath);
        }
        Path clusterGeneratedPath = registryPath.resolve(cluster.getId().toString());
        if (!clusterGeneratedPath.toFile().exists()) {
            Files.createDirectory(clusterGeneratedPath);
        }
        Path archivesPath = basePath.resolve(Constants.ARCHIVES);

        // List available services
        ClusterInfoResolver clusterInfoResolver = this.clusterInfoManager.fromType(cluster.getType());
        Collection<ServiceClientAndVersion> availableServices = clusterInfoResolver.resolveAvailableServiceClients(cluster);
        log.info("Found available services: {}", availableServices);

        // Download missing clients in archives and prepare some env
        Map<String, List<String>> env = availableServices.stream()
                .flatMap(s -> this.binariesStore.prepare(archivesPath, s.getService(), s.getVersion(), force).entrySet().stream())
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (a, b) -> {
                    List<String> merge = new ArrayList<>();
                    merge.addAll(a);
                    merge.addAll(b);
                    return merge;
                }));

        // Render and write configuration files
        Map<String, Map<String, byte[]>> confs = clusterInfoResolver.renderConfigurationFiles(cluster);
        log.info("Rendered files {}", confs.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue().keySet())).collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
        confs.forEach((service, files) -> files.forEach((filename, content) -> {
            try {
                Path serviceHomePath = clusterGeneratedPath.resolve(service);
                // Create directory for the service
                if (!serviceHomePath.toFile().exists()) {
                    Files.createDirectory(serviceHomePath);
                }
                Files.write(serviceHomePath.resolve(filename), content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            } catch (IOException e) {
                throw new DekException(e);
            }
        }));

        // Print env variables in env.bat/env.sh
        env.putAll(clusterInfoResolver.resolveEnvironmentVariables(archivesPath, clusterGeneratedPath, cluster));

        try (FileWriter unixFileWriter = new FileWriter(clusterGeneratedPath.resolve(Constants.ENV_SH).toFile(), false);
             FileWriter windowsFileWriter = new FileWriter(clusterGeneratedPath.resolve(Constants.ENV_BAT).toFile(), false)) {

            String currentPathEnvVar = System.getenv("PATH");

            env.entrySet().forEach(i -> {
                try {
                    unixFileWriter.append("export " + i.getKey() + "=\"" + (i.getKey().equalsIgnoreCase("PATH") ? currentPathEnvVar + ":" : "") + i.getValue().stream().collect(Collectors.joining(":")) + "\";\n");
                    windowsFileWriter.append("set " + i.getKey() + "=" + (i.getKey().equalsIgnoreCase("PATH") ? currentPathEnvVar + ";" : "") + i.getValue().stream().collect(Collectors.joining(";")) + "\r\n");
                } catch (IOException e) {
                    throw new DekException(e);
                }
            });
        } catch (IOException e) {
            throw new DekException(e);
        }
    }

    public void deleteCluser(String registryName, String name) {
        Registry registry = this.registryManager.getFromName(registryName);
        registry.getClusterService().deleteCluster(name);
    }
}
