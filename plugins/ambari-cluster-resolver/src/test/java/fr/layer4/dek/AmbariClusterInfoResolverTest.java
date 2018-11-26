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
package fr.layer4.dek;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.response;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class AmbariClusterInfoResolverTest {

    @ClassRule
    public static HoverflyRule hoverflyRule;

    static {
        try {
            hoverflyRule = HoverflyRule.inSimulationMode(dsl(
                    service("www.my-test.com")

                            .get("/api/v1/clusters")
                            .header("authorization", "Basic bGVfdXNlcjpsZV9wYXNzd29yZA==")
                            .willReturn(success()
                                    .header("content-type", "text/plain")
                                    .body("{" +
                                            "\"href\": \"hrefffff\"," +
                                            "\"items\": [" +
                                            "   {" +
                                            "   \"href\": \"href1111\"," +
                                            "   \"Clusters\":" +
                                            "       {" +
                                            "       \"cluster_name\": \"SAMPLE_DEV\"," +
                                            "       \"version\": \"HDP-2.6\"" +
                                            "       }" +
                                            "   }" +
                                            "]" +
                                            "}"))

                            .get("/api/v1/clusters/SAMPLE_DEV/services")
                            .header("authorization", "Basic bGVfdXNlcjpsZV9wYXNzd29yZA==")
                            .willReturn(success()
                                    .header("content-type", "text/plain")
                                    .body("{" +
                                            "\"href\": \"hrefffff\"," +
                                            "\"items\": [" +
                                            "   {" +
                                            "   \"href\": \"href1111\"," +
                                            "   \"ServiceInfo\":" +
                                            "       {" +
                                            "       \"cluster_name\": \"SAMPLE_DEV\"," +
                                            "       \"service_name\": \"HIVE\"" +
                                            "       }" +
                                            "   }," +
                                            "   {" +
                                            "   \"href\": \"href1111\"," +
                                            "   \"ServiceInfo\":" +
                                            "       {" +
                                            "       \"cluster_name\": \"SAMPLE_DEV\"," +
                                            "       \"service_name\": \"SERVICE_WITH_NOT_CLIENT\"" +
                                            "       }" +
                                            "   }" +
                                            "]" +
                                            "}"))

                            .get("/api/v1/stacks/HDP/versions/2.6/services/HIVE")
                            .header("authorization", "Basic bGVfdXNlcjpsZV9wYXNzd29yZA==")
                            .willReturn(success()
                                    .header("content-type", "text/plain")
                                    .body("{" +
                                            "\"href\": \"hrefffff\"," +
                                            "\"StackServices\":" +
                                            "   {" +
                                            "   \"service_version\": \"1.2.1000\"" +
                                            "   }" +
                                            "}"))

                            .get("/api/v1/stacks/HDP/versions/2.6/services/SERVICE_WITH_NOT_CLIENT")
                            .header("authorization", "Basic bGVfdXNlcjpsZV9wYXNzd29yZA==")
                            .willReturn(success()
                                    .header("content-type", "text/plain")
                                    .body("{" +
                                            "\"href\": \"hrefffff\"," +
                                            "\"StackServices\":" +
                                            "   {" +
                                            "   \"service_version\": \"6.6.6\"" +
                                            "   }" +
                                            "}"))

                            .get("/api/v1/clusters/SAMPLE_DEV/services/HIVE/components/HIVE_CLIENT")
                            .queryParam("format", "client_config_tar")
                            .header("authorization", "Basic bGVfdXNlcjpsZV9wYXNzd29yZA==")
                            .willReturn(success()
                                    .encoded(true)
                                    .body(Base64.encodeBase64String(IOUtils.toByteArray(AmbariClusterInfoResolver.class.getClassLoader().getResourceAsStream("test.tar.gz")))))

                            .get("/api/v1/clusters/SAMPLE_DEV/services/SERVICE_WITH_NOT_CLIENT/components/SERVICE_WITH_NOT_CLIENT_CLIENT")
                            .queryParam("format", "client_config_tar")
                            .header("authorization", "Basic bGVfdXNlcjpsZV9wYXNzd29yZA==")
                            .willReturn(response()
                                    .status(HttpStatus.NOT_FOUND.value()))
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private AmbariClusterInfoResolver resolver;

    @Before
    public void init() {
        resolver = new AmbariClusterInfoResolver(new RestTemplate());
    }

    @Test
    public void resolveEnvironmentVariables() {

        // Given
        Cluster cluster = new Cluster();
        cluster.setUri(URI.create("http://www.my-test.com/api/v1"));
        cluster.setUser("le_user");
        cluster.setPassword("le_password");

        // When
        Map<String, List<String>> envVars = resolver.resolveEnvironmentVariables(Paths.get("/archives/path"), Paths.get("/cluster/conf/path"), cluster);

        // Then
        assertEquals(0, envVars.size());
        // TODO
//        assertTrue(envVars.containsKey("HADOOP_HOME"));
//        assertEquals("", envVars.get("HADOOP_HOME"));
    }

    @Test
    public void resolveAvailableServices() {

        // Given
        Cluster cluster = new Cluster();
        cluster.setUri(URI.create("http://www.my-test.com/api/v1"));
        cluster.setUser("le_user");
        cluster.setPassword("le_password");

        // When
        Collection<ServiceClientAndVersion> services = resolver.resolveAvailableServiceClients(cluster);

        // Then
        assertThat(services).hasSize(2).containsExactlyInAnyOrder(ServiceClientAndVersion.of("hive", "1.2.1000"), ServiceClientAndVersion.of("service_with_not_client", "6.6.6"));
    }

    @Test
    public void resolveEnvironmentVariablesFromServices() {

        // Given
        ServiceClientAndVersion hive = ServiceClientAndVersion.of(DefaultServices.HIVE, "1.2.1000");
        ServiceClientAndVersion hdfs = ServiceClientAndVersion.of(DefaultServices.HDFS, "1.2.1000");
        ServiceClientAndVersion hbase = ServiceClientAndVersion.of(DefaultServices.HBASE, "1.2.1000");
        ServiceClientAndVersion yarn = ServiceClientAndVersion.of(DefaultServices.YARN, "1.2.1000");
        ServiceClientAndVersion zookeeper = ServiceClientAndVersion.of(DefaultServices.ZOOKEEPER, "1.2.1000");
        List<ServiceClientAndVersion> services = Arrays.asList(hive, hdfs, hbase, yarn, zookeeper);


        Path clusterGeneratedPath = Paths.get("/base/conf_for_cluster/");

        // When
        Map<String, List<String>> envVars = resolver.resolveEnvironmentVariablesFromServices(clusterGeneratedPath, services);

        // Then
        assertThat(envVars).hasSize(7)
                .containsEntry("HADOOP_CONF_DIR", Collections.singletonList(clusterGeneratedPath.resolve("hdfs").toAbsolutePath().toString()))
                .containsEntry("HADOOP_CLIENT_OPTS", Collections.singletonList("-Xmx1g"))
                .containsEntry("MAPRED_DISTCP_OPTS", Collections.singletonList("-Xmx2g"))
                .containsEntry("HADOOP_DISTCP_OPTS", Collections.singletonList("-Xmx2g"))
                .containsEntry("HBASE_CONF_DIR", Collections.singletonList(clusterGeneratedPath.resolve("hbase").toAbsolutePath().toString()))
                .containsEntry("YARN_CONF_DIR", Collections.singletonList(clusterGeneratedPath.resolve("yarn").toAbsolutePath().toString()))
                .containsEntry("ZOOKEEPER_CONF_DIR", Collections.singletonList(clusterGeneratedPath.resolve("zookeeper").toAbsolutePath().toString()));
    }

    @Test
    public void renderConfigurationFiles() throws IOException {

        // Given
        Cluster cluster = new Cluster();
        cluster.setUri(URI.create("http://www.my-test.com/api/v1"));
        cluster.setUser("le_user");
        cluster.setPassword("le_password");

        // When
        Map<String, Map<String, byte[]>> configurationFiles = resolver.renderConfigurationFiles(cluster);

        // Then
        assertThat(configurationFiles).hasSize(2).containsOnlyKeys("hive", "service_with_not_client");
        assertThat(configurationFiles.get("hive")).hasSize(1).containsOnlyKeys("test.xml");
        assertThat(configurationFiles.get("service_with_not_client")).hasSize(0);

        byte[] expected = IOUtils.toByteArray(AmbariClusterInfoResolver.class.getClassLoader().getResourceAsStream("test.xml"));
        byte[] actual = configurationFiles.get("hive").get("test.xml");
        assertThat(actual).isEqualTo(expected);
    }
}
