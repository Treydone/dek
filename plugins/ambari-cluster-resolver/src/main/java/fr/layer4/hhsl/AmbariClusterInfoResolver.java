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
    public Collection<ServiceAndVersion> resolveAvailableServices(Cluster cluster) {
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
            return ServiceAndVersion.of(serviceName, stackServiceResponse.getStackServices().getServiceVersion());
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, String> resolveEnvironmentVariables(Cluster cluster) {

        ApiClient apiClient = getApiClient(cluster);

        ClustersApi clustersApi = new ClustersApi(apiClient);

        ClusterResponseWrapperContext clusters = clustersApi.getClusters(null, null, null, null, null);
        ClusterResponse clusterResponse = clusters.getItems().get(0).getClusters();

        String clusterName = clusterResponse.getClusterName();


        Map<String, String> envVars = new HashMap<>();

        // Get services and components
        // For each components, add custom entries in envVars
        envVars.put("JAVA_HOME", "");
        //HADOOP
        envVars.put("HADOOP_HOME", "");
        envVars.put("HADOOP_CONF_DIR", "");
        envVars.put("HADOOP_CLIENT_OPTS", "-Xmx1g");
        envVars.put("MAPRED_DISTCP_OPTS", "-Xmx2g");
        envVars.put("HADOOP_DISTCP_OPTS", "-Xmx2g");
        // HBase
        envVars.put("HBASE_HOME", "");
        envVars.put("HBASE_CLASSPATH", "");
        envVars.put("HBASE_CONF_DIR", "");
        // Yarn
        envVars.put("YARN_HOME", "");
        envVars.put("YARN_CONF_DIR", "");
        // Zookeeper
        envVars.put("ZOOKEEPER_HOME", "");
        envVars.put("ZOOKEEPER_CONF_DIR", "");
        // Pig
        envVars.put("PIG_HOME", "");
        // Hive
        envVars.put("HIVE_HOME", "");

        return envVars;
    }

    @Override
    public Map<String, Map<String, byte[]>> renderConfigurationFiles(Cluster cluster) {

        ApiClient apiClient = getApiClient(cluster);

        ClustersApi clustersApi = new ClustersApi(apiClient);

        ClusterResponseWrapperContext clusters = clustersApi.getClusters(null, null, null, null, null);
        ClusterResponse clusterResponse = clusters.getItems().get(0).getClusters();

        String clusterName = clusterResponse.getClusterName();

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("clusterName", clusterName);
        uriVariables.put("serviceName", "HIVE"); // TODO
        uriVariables.put("componentName", "HIVE_CLIENT"); // TODO
        String path = UriComponentsBuilder.fromPath("/clusters/{clusterName}/services/{serviceName}//components/{componentName}").buildAndExpand(uriVariables).toUriString();

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", "client_config_tar"));
        List<MediaType> accept = apiClient.selectHeaderAccept(new String[]{"*/*"});
        ParameterizedTypeReference<byte[]> returnType = new ParameterizedTypeReference<byte[]>() {
        };
        byte[] bytes = apiClient.invokeAPI(path, HttpMethod.GET, queryParams, null, new HttpHeaders(), null, accept, null, new String[]{"basicAuth"}, returnType);


        Map<String, Map<String, byte[]>> configurationFiles = new HashMap<>();

        URI uri = cluster.getUri();

        // TODO Render Hadoop Configuration to XML
        Map<String, byte[]> configuration = new HashMap<>();
        configuration.put("hdfs-site.xml", "".getBytes());
        configuration.put("core-site.xml", "".getBytes());
        configurationFiles.put("hdfs", configuration);

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
