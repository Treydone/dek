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
package fr.layer4.dek.info;

import fr.layer4.dek.Cluster;
import fr.layer4.dek.ServiceClientAndVersion;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
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
    Collection<ServiceClientAndVersion> resolveAvailableServiceClients(Cluster cluster);

    /**
     * Return the environment variables mandatory to use the cluster.
     *
     *
     * @param archivesPath
     * @param clusterGeneratedPath
     * @param cluster
     * @return
     */
    Map<String, List<String>> resolveEnvironmentVariables(Path archivesPath, Path clusterGeneratedPath, Cluster cluster);

    /**
     * Render the configuration files to use the cluster.
     *
     * @param cluster
     * @return the binary content of the files, indexed by service and by filename
     */
    Map<String, Map<String, byte[]>> renderConfigurationFiles(Cluster cluster);
}
