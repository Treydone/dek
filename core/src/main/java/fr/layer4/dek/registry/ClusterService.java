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
package fr.layer4.dek.registry;

import fr.layer4.dek.cluster.Cluster;
import fr.layer4.dek.auth.Credentials;

import java.util.List;
import java.util.Optional;

public interface ClusterService {

    /**
     * Add a cluster in the specified registry.
     *
     * @param type
     * @param name
     * @param uri
     * @param banner
     * @return
     */
    Cluster addOrUpdateCluster(String type, String name, String uri, String banner, Credentials credentials);

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
    Optional<Cluster> getCluster(String name);

}
