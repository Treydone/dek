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

import fr.layer4.dek.Constants;
import fr.layer4.dek.banner.Banner;
import fr.layer4.dek.banner.BannerManager;
import fr.layer4.dek.cluster.Cluster;
import fr.layer4.dek.cluster.ClusterManager;
import fr.layer4.dek.registry.Registry;
import fr.layer4.dek.registry.RegistryConnection;
import fr.layer4.dek.registry.RegistryManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.Table;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Slf4j
@ShellComponent
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ClusterCommands {

    private final ClusterManager clusterManager;
    private final BannerManager bannerManager;

    @ShellMethod(key = "list cluster", value = "List all clusters", group = "Cluster")
    public Table listClusters(@ShellOption(defaultValue = Constants.LOCAL_REGISTRY_NAME, value = "registry") String registryName) {
        List<Pair<Registry, Cluster>> clusters = this.clusterManager.getAllClusters(registryName);

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
        this.clusterManager.deleteCluser(registryName, name);
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

        this.clusterManager.add(registryName, type, name, uri, banner);
    }

    @ShellMethod(key = "update cluster", value = "Update cluster configuration", group = "Cluster")
    public void updateCluster(
            @ShellOption(defaultValue = Constants.LOCAL_REGISTRY_NAME) String registryName,
            String name) throws IOException {

        this.clusterManager.prepare(registryName, name, true);
    }

    @ShellMethod(key = {"use", "use cluster"}, value = "Use the configuration of a cluster", group = "Cluster")
    public Banner useCluster(@ShellOption(defaultValue = Constants.LOCAL_REGISTRY_NAME) String registryName, String name) {
        Cluster cluster = this.clusterManager.use(registryName, name);

        // Print banner
        return new Banner(new String(cluster.getBanner()), Collections.singletonMap("cluster", cluster));
    }
}
