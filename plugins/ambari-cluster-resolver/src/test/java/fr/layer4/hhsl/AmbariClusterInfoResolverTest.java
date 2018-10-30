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
package fr.layer4.hhsl;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AmbariClusterInfoResolverTest {

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(dsl(
            service("www.my-test.com")

                    .get("/api/v1/clusters")
                    .header("authorization", "Basic bGVfdXNlcjpsZV9wYXNzd29yZA==")
                    .willReturn(success()
                            .header("content-type", "application/json")
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
                            .header("content-type", "application/json")
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
                                    "   }" +
                                    "]" +
                                    "}"))

                    .get("/api/v1/stacks/HDP/versions/2.6/services/HIVE")
                    .header("authorization", "Basic bGVfdXNlcjpsZV9wYXNzd29yZA==")
                    .willReturn(success()
                            .header("content-type", "application/json")
                            .body("{" +
                                    "\"href\": \"hrefffff\"," +
                                    "\"StackServices\":" +
                                    "   {" +
                                    "   \"service_version\": \"1.2.1000\"" +
                                    "   }" +
                                    "}"))

                    .get("/api/v1/clusters/SAMPLE_DEV/services/HIVE/components/HIVE_CLIENT")
                    .queryParam("format", "client_config_tar")
                    .header("authorization", "Basic bGVfdXNlcjpsZV9wYXNzd29yZA==")
                    .willReturn(success()
                            .header("content-type", "application/zip")
                            .body(""))
    ));

    private AmbariClusterInfoResolver resolver;

    @Before
    public void init() {
        resolver = new AmbariClusterInfoResolver();
        RestTemplate restTemplate = new RestTemplate();

        resolver.setRestTemplate(restTemplate);
    }

    @Test
    public void resolveEnvironmentVariables() {

        // Given
        Cluster cluster = new Cluster();
        cluster.setUri(URI.create("http://www.my-test.com/api/v1"));
        cluster.setUser("le_user");
        cluster.setPassword("le_password");

        // When
        Map<String, String> envVars = resolver.resolveEnvironmentVariables(cluster);

        // Then
        assertEquals(2, envVars.size());
        assertTrue(envVars.containsKey("HADOOP_HOME"));
        assertEquals("", envVars.get("HADOOP_HOME"));
    }

    @Test
    public void resolveAvailableServices() {

        // Given
        Cluster cluster = new Cluster();
        cluster.setUri(URI.create("http://www.my-test.com/api/v1"));
        cluster.setUser("le_user");
        cluster.setPassword("le_password");

        // When
        Collection<ServiceAndVersion> services = resolver.resolveAvailableServices(cluster);

        // Then
        assertThat(services).hasSize(1).containsExactly(ServiceAndVersion.of("HIVE", "1.2.1000"));
    }

    @Test
    public void renderConfigurationFiles() {

        // Given
        Cluster cluster = new Cluster();
        cluster.setUri(URI.create("http://www.my-test.com/api/v1"));
        cluster.setUser("le_user");
        cluster.setPassword("le_password");

        // When
        Map<String, Map<String, byte[]>> configurationFiles = resolver.renderConfigurationFiles(cluster);

        // Then
        assertEquals(2, configurationFiles.size());
    }
}
