package fr.layer4.hhsl;

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

import fr.layer4.hhsl.ambari.api.ApiClient;
import fr.layer4.hhsl.ambari.api.ClusterServicesApi;
import fr.layer4.hhsl.ambari.api.ClustersApi;
import fr.layer4.hhsl.ambari.api.StacksApi;
import fr.layer4.hhsl.ambari.api.model.ClusterResponse;
import fr.layer4.hhsl.ambari.api.model.ClusterResponseWrapperContext;
import fr.layer4.hhsl.ambari.api.model.ServiceResponseWrapperContext;
import fr.layer4.hhsl.ambari.api.model.StackServiceResponseSwagger;
import fr.layer4.hhsl.info.ClusterInfoResolver;
import lombok.Setter;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AmbariClusterInfoResolver implements ClusterInfoResolver {

    @Autowired
    @Setter
    private RestTemplate restTemplate;

    @Override
    public String getType() {
        return "ambari";
    }

    @Override
    public Collection<ServiceClientAndVersion> resolveAvailableServiceClients(Cluster cluster) {
        ApiClient apiClient = getApiClient(cluster);

        StacksApi stacksApi = new StacksApi(apiClient);
        ClustersApi clustersApi = new ClustersApi(apiClient);
        ClusterServicesApi clusterServicesApi = new ClusterServicesApi(apiClient);

        ClusterResponseWrapperContext clusters = clustersApi.getClusters(null, null, null, null, null);
        ClusterResponse clusterResponse = clusters.getItems().get(0).getClusters();

        String clusterName = clusterResponse.getClusterName();

        ServiceResponseWrapperContext serviceResponse = clusterServicesApi.serviceServiceGetServices(clusterName, null, null, null, null, null);
        return serviceResponse.getItems().stream().map(s -> {
            String serviceName = s.getServiceInfo().getServiceName();
            StackServiceResponseSwagger stackServiceResponse = stacksApi.stacksServiceGetStackService("HDP", clusterResponse.getVersion().replace("HDP-", ""), serviceName, null);
            return ServiceClientAndVersion.of(serviceName, stackServiceResponse.getStackServices().getServiceVersion());
        }).collect(Collectors.toList());
        // TODO keep only service clients (HIVE_CLIENT, but not HIVE_METASTORE)
    }

    @Override
    public Map<String, String> resolveEnvironmentVariables(String archivesPath, String clusterGeneratedPath, Cluster cluster) {

        Collection<ServiceClientAndVersion> serviceClientAndVersions = resolveAvailableServiceClients(cluster);

        Map<String, String> envVars = new HashMap<>();

        // Get services and components
        // For each components, add custom entries in envVars
        serviceClientAndVersions.forEach(e -> {
            switch (e.getService()) {
                case "HDFS":
                    envVars.put("HADOOP_CONF_DIR", clusterGeneratedPath + File.separator + e.getService());
                    envVars.put("HADOOP_CLIENT_OPTS", "-Xmx1g");
                    envVars.put("MAPRED_DISTCP_OPTS", "-Xmx2g");
                    envVars.put("HADOOP_DISTCP_OPTS", "-Xmx2g");
                    break;
                case "HBASE":
//                    envVars.put("HBASE_CLASSPATH", "");
                    envVars.put("HBASE_CONF_DIR", clusterGeneratedPath + File.separator + e.getService());
                    break;
                case "YARN":
                    envVars.put("YARN_CONF_DIR", clusterGeneratedPath + File.separator + e.getService());
                    break;
                case "ZOOKEEPER":
                    envVars.put("ZOOKEEPER_CONF_DIR", clusterGeneratedPath + File.separator + e.getService());
                    break;
                case "PIG":
                    break;
                case "HIVE":
                    break;
            }
        });

        return envVars;
    }

    @Override
    public Map<String, Map<String, byte[]>> renderConfigurationFiles(Cluster cluster) {

        ApiClient apiClient = getApiClient(cluster);

        ClustersApi clustersApi = new ClustersApi(apiClient);

        ClusterResponseWrapperContext clusters = clustersApi.getClusters(null, null, null, null, null);
        ClusterResponse clusterResponse = clusters.getItems().get(0).getClusters();

        String clusterName = clusterResponse.getClusterName();

        Collection<ServiceClientAndVersion> serviceClientAndVersions = resolveAvailableServiceClients(cluster);
        Map<String, Map<String, byte[]>> configurationFiles = serviceClientAndVersions.stream()
                .map(sv -> {
                    Map<String, Object> uriVariables = new HashMap<>();
                    uriVariables.put("clusterName", clusterName);
                    uriVariables.put("serviceName", sv.getService()); // TODO
                    uriVariables.put("componentName", "HIVE_CLIENT"); // TODO
                    String path = UriComponentsBuilder.fromPath("/clusters/{clusterName}/services/{serviceName}/components/{componentName}").buildAndExpand(uriVariables).toUriString();

                    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
                    queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", "client_config_tar"));
                    List<MediaType> accept = apiClient.selectHeaderAccept(new String[]{"*/*"});
                    ParameterizedTypeReference<byte[]> returnType = new ParameterizedTypeReference<byte[]>() {
                    };
                    byte[] bytes = apiClient.invokeAPI(path, HttpMethod.GET, queryParams, null, new HttpHeaders(), null, accept, null, new String[]{"basicAuth"}, returnType);

                    // TODO Extract configuration files from archive
                    Map<String, byte[]> confs = new HashMap<>();
                    try (InputStream fi = new ByteArrayInputStream(bytes);
//                         InputStream bi = new BufferedInputStream(fi);
                         InputStream gzi = new GzipCompressorInputStream(fi);
                         ArchiveInputStream i = new TarArchiveInputStream(gzi)
                    ) {
                        ArchiveEntry entry;
                        while ((entry = i.getNextEntry()) != null) {
                            if (!i.canReadEntryData(entry)) {
                                // TODO log something?
                                continue;
                            }
                            if (!entry.isDirectory()) {
                                confs.put(FilenameUtils.getName(entry.getName()), IOUtils.toByteArray(i));
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Can not extract archive", e);
                    }
                    return Pair.of(sv.getService(), confs);
                })
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

        return configurationFiles;
    }

    protected ApiClient getApiClient(Cluster cluster) {
        URI uri = cluster.getUri();
        String user = cluster.getUser();
        String password = cluster.getPassword();

        ApiClient apiClient = new ApiClient(this.restTemplate);
        apiClient.setBasePath(uri.toString());
        apiClient.setUsername(user);
        apiClient.setPassword(password);
        return apiClient;
    }
}
