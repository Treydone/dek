package fr.layer4.hhsl;

import org.junit.Test;

import java.net.URI;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AmbariClusterInfoResolverTest {

    @Test
    public void resolveEnvironmentVariables() {

        // Given
        AmbariClusterInfoResolver resolver = new AmbariClusterInfoResolver();

        Cluster cluster = new Cluster();
        cluster.setUri(URI.create("http://pouet/"));
        cluster.setUser("le_user");
        cluster.setPassword("le_password");

        // When
        Map<String, String> envVars = resolver.resolveEnvironmentVariables(cluster);

        // Then
        assertEquals(2, envVars.size());
        assertTrue(envVars.containsKey("HADOOP_HOME"));
        assertEquals("", envVars.get("HADOOP_HOME"));
    }
}
