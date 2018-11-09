package fr.layer4.hhsl.commands;

/*-
 * #%L
 * HHSL
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

import fr.layer4.hhsl.Cluster;
import fr.layer4.hhsl.Constants;
import fr.layer4.hhsl.ServiceClientAndVersion;
import fr.layer4.hhsl.banner.Banner;
import fr.layer4.hhsl.banner.BannerManager;
import fr.layer4.hhsl.binaries.BinariesStore;
import fr.layer4.hhsl.info.ClusterInfoManager;
import fr.layer4.hhsl.info.ClusterInfoResolver;
import fr.layer4.hhsl.registry.Registry;
import fr.layer4.hhsl.registry.RegistryConnection;
import fr.layer4.hhsl.registry.RegistryManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.Table;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
            @ShellOption(defaultValue = "") String banner) {

        // Check banner
        banner = this.bannerManager.checkAndLoadTemplate(banner);

        Registry registry = this.registryManager.getFromName(registryName);
        RegistryConnection underlyingConnection = registry.getUnderlyingConnection();
        Cluster cluster = registry.getClusterService().addOrUpdateCluster(type, name, uri, banner);
        prepare(underlyingConnection, cluster, false);
    }

    @ShellMethod(key = "update cluster", value = "Update cluster configuration", group = "Cluster")
    public void updateCluster(
            @ShellOption(defaultValue = Constants.LOCAL_REGISTRY_NAME) String registryName,
            String name) {
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
        return new Banner(new String(cluster.getBanner()), cluster);
    }

    protected void use(RegistryConnection underlyingConnection, Cluster cluster) {
        String basePath = Constants.getRootPath();
        String clusterGeneratedPath = basePath + File.separator + underlyingConnection.getId().toString() + File.separator + cluster.getId().toString();

        try {
            FileUtils.copyFile(new File(clusterGeneratedPath, Constants.ENV_SH), new File(basePath, Constants.ENV_SH));
            FileUtils.copyFile(new File(clusterGeneratedPath, Constants.ENV_BAT), new File(basePath, Constants.ENV_BAT));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void prepare(RegistryConnection underlyingConnection, Cluster cluster, boolean force) {
        String basePath = Constants.getRootPath();

        String clusterGeneratedPath = basePath + File.separator + underlyingConnection.getId().toString() + File.separator + cluster.getId().toString();
        String archivesPath = basePath + File.separator + Constants.ARCHIVES;

        // List available services
        ClusterInfoResolver clusterInfoResolver = this.clusterInfoManager.fromType(cluster.getType());
        Collection<ServiceClientAndVersion> availableServices = clusterInfoResolver.resolveAvailableServiceClients(cluster);

        // Download missing clients in archives and prepare some env
        Map<String, String> env = availableServices.stream()
                .flatMap(s -> this.binariesStore.prepare(archivesPath, s.getService(), s.getVersion(), force).entrySet().stream())
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (a, b) -> a)); // TODO No the best way to do it...

        // Render and write configuration files
        Map<String, Map<String, byte[]>> confs = clusterInfoResolver.renderConfigurationFiles(cluster);
        confs.forEach((service, files) -> files.forEach((filename, content) -> {
            try {
                Path serviceHome = Paths.get(clusterGeneratedPath, service);
                // Create directory for the service
                Files.createDirectory(serviceHome);
                Files.write(serviceHome.resolve(filename), content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));

        // Print env variables in env.bat/env.sh
        env.putAll(clusterInfoResolver.resolveEnvironmentVariables(archivesPath, clusterGeneratedPath, cluster));

        try (FileWriter unixFileWriter = new FileWriter(new File(clusterGeneratedPath, Constants.ENV_SH));
             FileWriter windowsFileWriter = new FileWriter(new File(clusterGeneratedPath, Constants.ENV_BAT))) {
            env.entrySet().forEach(i -> {
                try {
                    unixFileWriter.append("export " + i.getKey() + "=" + i.getValue() + ";\n");
                    windowsFileWriter.append("set " + i.getKey() + "=" + i.getValue() + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
