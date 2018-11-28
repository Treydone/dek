package fr.layer4.dek.commands;

/*-
 * #%L
 * DEK
 * %%
 * Copyright (C) 2018 Layer4
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import fr.layer4.dek.*;
import fr.layer4.dek.auth.Credentials;
import fr.layer4.dek.banner.Banner;
import fr.layer4.dek.banner.BannerManager;
import fr.layer4.dek.binaries.BinariesStore;
import fr.layer4.dek.info.ClusterInfoManager;
import fr.layer4.dek.info.ClusterInfoResolver;
import fr.layer4.dek.registry.Registry;
import fr.layer4.dek.registry.RegistryConnection;
import fr.layer4.dek.registry.RegistryManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.Table;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@ShellComponent
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ClusterCommands {

    private final RegistryManager registryManager;
    private final ClusterInfoManager clusterInfoManager;
    private final BinariesStore binariesStore;
    private final BannerManager bannerManager;

    @ShellMethod(key = "list cluster", value = "List all clusters", group = "Cluster")
    public Table listClusters(@ShellOption(defaultValue = Constants.LOCAL_REGISTRY_NAME, value = "registry") String registryName) {
        // List cluster for all registries...
        List<Registry> all = this.registryManager.all();
        List<Pair<Registry, Cluster>> clusters = all.stream().flatMap(r -> this.registryManager.getFromName(registryName).getClusterService().listClusters().stream().map(c -> Pair.of(r, c)))
                .collect(Collectors.toList());

        String[][] data = new String[clusters.size() + 1][];
        data[0] = new String[]{"Registry", "Name", "Type", "URI"};

        int it = 1;
        Iterator<Pair<Registry, Cluster>> iterator = clusters.iterator();
        while (iterator.hasNext()) {
            Pair<Registry, Cluster> pair = iterator.next();
            data[it] = new String[]{pair.getKey().getUnderlyingConnection().getName(), pair.getValue().getName(), pair.getValue().getType(), pair.getValue().getUri().toString()};
            it++;
        }
        return CommandUtils.getTable(data);
    }

    @ShellMethod(key = "delete cluster", value = "Delete a cluster inside a registry", group = "Cluster")
    public void deleteCluster(@ShellOption(defaultValue = Constants.LOCAL_REGISTRY_NAME) String registryName, String name) {
        Registry registry = this.registryManager.getFromName(registryName);
        registry.getClusterService().deleteCluster(name);
    }

    @ShellMethod(key = "add cluster", value = "Add a cluster", group = "Cluster")
    public void addCluster(
            @ShellOption(defaultValue = Constants.LOCAL_REGISTRY_NAME) String registryName,
            String type,
            String name,
            String uri,
            @ShellOption(defaultValue = "") String banner) throws IOException {

        // Check banner
        banner = this.bannerManager.checkAndLoadTemplate(banner);

        Registry registry = this.registryManager.getFromName(registryName);
        RegistryConnection underlyingConnection = registry.getUnderlyingConnection();
        ClusterService clusterService = registry.getClusterService();

        ClusterInfoResolver clusterInfoResolver = this.clusterInfoManager.fromType(type);
        Credentials credentials = clusterInfoResolver.getCredentials();

        Cluster cluster = clusterService.addOrUpdateCluster(type, name, uri, banner, credentials);
        prepare(underlyingConnection, cluster, false);
    }

    @ShellMethod(key = "update cluster", value = "Update cluster configuration", group = "Cluster")
    public void updateCluster(
            @ShellOption(defaultValue = Constants.LOCAL_REGISTRY_NAME) String registryName,
            String name) throws IOException {
        Registry registry = this.registryManager.getFromName(registryName);
        RegistryConnection underlyingConnection = registry.getUnderlyingConnection();
        Cluster cluster = registry.getClusterService().getCluster(name).orElseThrow(() -> new RuntimeException("Can not find cluster"));
        prepare(underlyingConnection, cluster, true);
    }

    @ShellMethod(key = {"use", "use cluster"}, value = "Use the configuration of a cluster", group = "Cluster")
    public Banner useCluster(@ShellOption(defaultValue = Constants.LOCAL_REGISTRY_NAME) String registryName, String name) {
        Registry registry = this.registryManager.getFromName(registryName);
        RegistryConnection underlyingConnection = registry.getUnderlyingConnection();
        Cluster cluster = registry.getClusterService().getCluster(name).orElseThrow(() -> new RuntimeException("Can not find cluster"));

        use(underlyingConnection, cluster);

        // Print banner
        return new Banner(new String(cluster.getBanner()), Collections.singletonMap("cluster", cluster));
    }

    protected void use(RegistryConnection underlyingConnection, Cluster cluster) {
        Path basePath = Constants.getRootPath();
        Path clusterGeneratedPath = basePath.resolve(Paths.get(underlyingConnection.getId().toString(), cluster.getId().toString()));

        try {
            FileUtils.copyFile(clusterGeneratedPath.resolve(Constants.ENV_SH).toFile(), basePath.resolve(Constants.ENV_SH).toFile());
            FileUtils.copyFile(clusterGeneratedPath.resolve(Constants.ENV_BAT).toFile(), basePath.resolve(Constants.ENV_BAT).toFile());
        } catch (IOException e) {
            throw new DekException(e);
        }
    }

    protected void prepare(RegistryConnection underlyingConnection, Cluster cluster, boolean force) throws IOException {
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
}
