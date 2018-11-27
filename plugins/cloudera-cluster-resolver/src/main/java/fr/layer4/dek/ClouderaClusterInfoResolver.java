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

import com.cloudera.api.swagger.ClustersResourceApi;
import com.cloudera.api.swagger.ParcelResourceApi;
import com.cloudera.api.swagger.client.ApiClient;
import com.cloudera.api.swagger.client.Configuration;
import com.cloudera.api.swagger.model.ApiCluster;
import com.cloudera.api.swagger.model.ApiClusterList;
import com.cloudera.api.swagger.model.ApiService;
import com.squareup.okhttp.*;
import fr.layer4.dek.http.HttpProperties;
import fr.layer4.dek.info.ClusterInfoResolver;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Path;
import java.util.*;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ClouderaClusterInfoResolver implements ClusterInfoResolver {

    private final PropertyManager propertyManager;

    @Override
    public String getType() {
        return "cloudera";
    }

    @Override
    @SneakyThrows
    public Collection<ServiceClientAndVersion> resolveAvailableServiceClients(Cluster cluster) {

        ApiClient cmClient = Configuration.getDefaultApiClient();
        cmClient.setBasePath(cluster.getUri().toString());
        cmClient.setUsername(cluster.getCredentials().getPrincipal());
        cmClient.setPassword(cluster.getCredentials().getPassword());
        cmClient.setHttpClient(getOkHttpClient());

        ClustersResourceApi clustersResourceApi = new ClustersResourceApi(cmClient);
        ParcelResourceApi parcelResourceApi = new ParcelResourceApi(cmClient);

        ApiClusterList clusters = clustersResourceApi.readClusters(null);
        ApiCluster apiCluster = clusters.getItems().get(0);
        List<ApiService> services = apiCluster.getServices();
        String value = apiCluster.getVersion().getValue();

//        parcelResourceApi.readParcel(apiCluster.getName())
//        services.get(0).getConfig().get

        return Arrays.asList();
    }

    protected OkHttpClient getOkHttpClient() {
        OkHttpClient httpClient = new OkHttpClient();
        if (Boolean.valueOf(this.propertyManager.getProperty(HttpProperties.PROXY_ENABLED).orElse(HttpProperties.PROXY_ENABLED_DEFAULT))) {

            // TODO check no host proxy

            // Check host and port are present
            String host = this.propertyManager.getProperty(HttpProperties.PROXY_HOST).orElseThrow(() -> new RuntimeException("Proxy is enabled but host is missing"));
            Integer port = Integer.valueOf(this.propertyManager.getProperty(HttpProperties.PROXY_PORT).orElseThrow(() -> new RuntimeException("Proxy is enabled but port is missing")));
            httpClient.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port)));

            // Check auth
            String proxyAuth = this.propertyManager.getProperty(HttpProperties.PROXY_AUTH_TYPE).orElse(HttpProperties.PROXY_AUTH_NONE);

            switch (proxyAuth.toLowerCase()) {
                case HttpProperties.PROXY_AUTH_NONE:
                    break;
                case HttpProperties.PROXY_AUTH_BASIC:
                    String user = this.propertyManager.getProperty(HttpProperties.PROXY_AUTH_BASIC_USER).orElseThrow(() -> new RuntimeException("Basic proxy authentication is enabled but user is missing"));
                    String password = this.propertyManager.getProperty(HttpProperties.PROXY_AUTH_BASIC_PASSWORD).orElseThrow(() -> new RuntimeException("Basic proxy authentication is enabled but password is missing"));
                    httpClient.setAuthenticator(new Authenticator() {
                        @Override
                        public Request authenticate(Proxy proxy, Response response) {
                            String credentials = Credentials.basic(user, password);
                            if (credentials.equals(response.request().header("Authorization"))) {
                                return null; // If we already failed with these credentials, don't retry.
                            }
                            return response.request().newBuilder().header("Authorization", credentials).build();
                        }

                        @Override
                        public Request authenticateProxy(Proxy proxy, Response response) {
                            return authenticate(proxy, response);
                        }
                    });
                    break;
                default:
                    throw new RuntimeException("Unknown proxy auth " + proxyAuth);
            }
        }
        return httpClient;
    }

    @Override
    public Map<String, List<String>> resolveEnvironmentVariables(Path archivesPath, Path clusterGeneratedPath, Cluster cluster) {

        Collection<ServiceClientAndVersion> serviceClientAndVersions = resolveAvailableServiceClients(cluster);

        Map<String, List<String>> envVars = new HashMap<>();

        return envVars;
    }

    @Override
    public Map<String, Map<String, byte[]>> renderConfigurationFiles(Cluster cluster) {
        Map<String, Map<String, byte[]>> configurationFiles = new HashMap<>();

        return configurationFiles;
    }

    @Override
    public fr.layer4.dek.auth.Credentials getCredentials() {
        return null;
    }
}
