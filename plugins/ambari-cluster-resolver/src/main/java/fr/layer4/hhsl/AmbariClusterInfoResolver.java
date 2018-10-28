package fr.layer4.hhsl;

import fr.layer4.hhsl.ambari.ApiClient;
import fr.layer4.hhsl.ambari.api.ClustersApi;
import fr.layer4.hhsl.ambari.api.HostComponentsApi;
import fr.layer4.hhsl.ambari.api.model.ClusterResponse;
import fr.layer4.hhsl.ambari.api.model.ClusterResponseWrapper;
import fr.layer4.hhsl.ambari.api.model.HostComponentSwagger;
import fr.layer4.hhsl.info.ClusterInfoResolver;
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
import java.util.*;

@Component
public class AmbariClusterInfoResolver implements ClusterInfoResolver {

    private RestTemplate restTemplate = new RestTemplate();

    public String getType() {
        return "ambari";
    }

    @Override
    public Collection<String> resolveAvailableServices(Cluster cluster) {
        return Arrays.asList("hdfs", "yarn", "spark"); // TODO
    }

    public Map<String, String> resolveEnvironmentVariables(Cluster cluster) {

        URI uri = cluster.getUri();
        String user = cluster.getUser();
        String password = cluster.getPassword();

        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(uri.toString());
        apiClient.setUsername(user);
        apiClient.setPassword(password);

        ClustersApi clustersApi = new ClustersApi(apiClient);
        List<ClusterResponseWrapper> clusters = clustersApi.getClusters(null, null, null, null, null);
        ClusterResponse clusterResponse = clusters.get(0).getClusters();

        String clusterName = clusterResponse.getClusterName();

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("componentName", "HIVE");
        uriVariables.put("clusterName", clusterName);
        String path = UriComponentsBuilder.fromPath("/clusters/{clusterName}/components/{componentName}").buildAndExpand(uriVariables).toUriString();

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        HttpHeaders headerParams = new HttpHeaders();
        MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", "client_config_tar"));

        final String[] accepts = {"*/*"};
        List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        MediaType contentType = apiClient.selectHeaderContentType(new String[]{});

        ParameterizedTypeReference<byte[]> returnType = new ParameterizedTypeReference<byte[]>() {
        };
        byte[] bytes = apiClient.invokeAPI(path, HttpMethod.GET, queryParams, null, headerParams, formParams, accept, contentType, new String[]{}, returnType);


        Map<String, String> envVars = new HashMap<>();

        // Get services and components
        //HDFS
        envVars.put("HADOOP_HOME", "");
        // HBase
        envVars.put("", "");

        // For each components, add custom entries in envVars

        return envVars;
    }

    public Map<String, byte[]> renderConfigurationFiles(Cluster cluster) {

        Map<String, byte[]> configurationFiles = new HashMap<>();

        URI uri = cluster.getUri();

        // Render Hadoop Configuration to XML

        configurationFiles.put("hdfs-site.xml", "".getBytes());
        configurationFiles.put("hbase-site.xml", "".getBytes());
        configurationFiles.put("core-site.xml", "".getBytes());

        return configurationFiles;
    }
}
