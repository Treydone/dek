package fr.layer4.dek;

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

import fr.layer4.dek.ambari.api.ApiClient;
import fr.layer4.dek.ambari.api.ClusterServicesApi;
import fr.layer4.dek.ambari.api.ClustersApi;
import fr.layer4.dek.ambari.api.StacksApi;
import fr.layer4.dek.ambari.api.model.ClusterResponse;
import fr.layer4.dek.ambari.api.model.ClusterResponseWrapperContext;
import fr.layer4.dek.ambari.api.model.ServiceResponseWrapperContext;
import fr.layer4.dek.ambari.api.model.StackServiceResponseSwagger;
import fr.layer4.dek.auth.Credentials;
import fr.layer4.dek.info.ClusterInfoResolver;
import fr.layer4.dek.prompt.Prompter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AmbariClusterInfoResolver implements ClusterInfoResolver {

    private final RestTemplate restTemplate;
    private final Prompter prompter;

    public AmbariClusterInfoResolver(Prompter prompter, RestTemplate restTemplate) {
        this.prompter = prompter;
        this.restTemplate = new RestTemplate(restTemplate.getRequestFactory());
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(new MediaType("text", "plain")));
        this.restTemplate.setMessageConverters(Arrays.asList(mappingJackson2HttpMessageConverter, new ByteArrayHttpMessageConverter()));
    }

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
            return ServiceClientAndVersion.of(serviceName.toLowerCase(), stackServiceResponse.getStackServices().getServiceVersion());
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, List<String>> resolveEnvironmentVariables(Path archivesPath, Path clusterGeneratedPath, Cluster cluster) {
        Collection<ServiceClientAndVersion> serviceClientAndVersions = resolveAvailableServiceClients(cluster);
        return resolveEnvironmentVariablesFromServices(clusterGeneratedPath, serviceClientAndVersions);
    }

    protected Map<String, List<String>> resolveEnvironmentVariablesFromServices(Path clusterGeneratedPath, Collection<ServiceClientAndVersion> serviceClientAndVersions) {
        Map<String, List<String>> envVars = new HashMap<>();

        // Get services and components
        // For each components, add custom entries in envVars
        serviceClientAndVersions.forEach(e -> {
            switch (e.getService().toLowerCase()) {
                case DefaultServices.HDFS:
                    envVars.put("HADOOP_CONF_DIR", Collections.singletonList(clusterGeneratedPath.resolve(e.getService()).toAbsolutePath().toString()));
                    envVars.put("HADOOP_CLIENT_OPTS", Collections.singletonList("-Xmx1g"));
                    envVars.put("MAPRED_DISTCP_OPTS", Collections.singletonList("-Xmx2g"));
                    envVars.put("HADOOP_DISTCP_OPTS", Collections.singletonList("-Xmx2g"));
                    break;
                case DefaultServices.HBASE:
//                    envVars.put("HBASE_CLASSPATH", "");
                    envVars.put("HBASE_CONF_DIR", Collections.singletonList(clusterGeneratedPath.resolve(e.getService()).toAbsolutePath().toString()));
                    break;
                case DefaultServices.YARN:
                    envVars.put("YARN_CONF_DIR", Collections.singletonList(clusterGeneratedPath.resolve(e.getService()).toAbsolutePath().toString()));
                    break;
                case DefaultServices.ZOOKEEPER:
                    envVars.put("ZOOKEEPER_CONF_DIR", Collections.singletonList(clusterGeneratedPath.resolve(e.getService()).toAbsolutePath().toString()));
                    break;
                case DefaultServices.PIG:
                    break;
                case DefaultServices.HIVE:
                    break;
                default:
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
                    String serviceName = sv.getService().toUpperCase();
                    String clientName = serviceName + "_CLIENT";

                    Map<String, Object> uriVariables = new HashMap<>();
                    uriVariables.put("clusterName", clusterName);
                    uriVariables.put("serviceName", serviceName);
                    uriVariables.put("componentName", clientName);
                    String path = UriComponentsBuilder.fromPath("/clusters/{clusterName}/services/{serviceName}/components/{componentName}").buildAndExpand(uriVariables).toUriString();

                    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
                    queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", "client_config_tar"));
                    List<MediaType> accept = apiClient.selectHeaderAccept(new String[]{"*/*"});
                    ParameterizedTypeReference<byte[]> returnType = new ParameterizedTypeReference<byte[]>() {
                    };

                    Map<String, byte[]> confs = new HashMap<>();

                    try {
                        byte[] bytes = apiClient.invokeAPI(path, HttpMethod.GET, queryParams, null, new HttpHeaders(), null, accept, null, new String[]{"basicAuth"}, returnType);

                        // Extract configuration files from archive
                        try (InputStream raw = new ByteArrayInputStream(bytes);
                             InputStream buffered = new BufferedInputStream(raw);
                             InputStream gz = new GzipCompressorInputStream(buffered);
                             ArchiveInputStream tar = new TarArchiveInputStream(gz)
                        ) {
                            ArchiveEntry entry;
                            while ((entry = tar.getNextEntry()) != null) {
                                if (!tar.canReadEntryData(entry)) {
                                    // TODO log something?
                                    continue;
                                }
                                if (!entry.isDirectory()) {
                                    confs.put(FilenameUtils.getName(entry.getName()), IOUtils.toByteArray(tar));
                                }
                            }
                        } catch (IOException e) {
                            throw new DekException("Can not extract archive", e);
                        }
                    } catch (RestClientException e) {
                        log.warn("Can not find client {} for service {}", clientName, serviceName);
                    }
                    return Pair.of(sv.getService(), confs);
                })
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

        return configurationFiles;
    }

    @Override
    public Credentials getCredentials() {
        return Credentials.basic(this.prompter.prompt("user: "), this.prompter.doublePromptForPassword());
    }

    protected ApiClient getApiClient(Cluster cluster) {
        URI uri = cluster.getUri();
        String user = cluster.getCredentials().getPrincipal();
        String password = cluster.getCredentials().getPassword();

        ApiClient apiClient = new ApiClient(this.restTemplate);
        apiClient.setBasePath(uri.toString());
        apiClient.setUsername(user);
        apiClient.setPassword(password);
        return apiClient;
    }
}
