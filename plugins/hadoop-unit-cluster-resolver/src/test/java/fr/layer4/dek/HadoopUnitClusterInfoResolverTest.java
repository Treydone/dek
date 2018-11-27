package fr.layer4.dek;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class HadoopUnitClusterInfoResolverTest {

    private HadoopUnitClusterInfoResolver resolver;

    @Before
    public void init() throws IOException {
        YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
        yamlPropertiesFactoryBean.setResources(
                new ByteArrayResource(IOUtils.toByteArray(HadoopUnitClusterInfoResolverTest.class.getClassLoader().getResourceAsStream("hdp.matrix.yml"))),
                new ByteArrayResource(IOUtils.toByteArray(HadoopUnitClusterInfoResolverTest.class.getClassLoader().getResourceAsStream("hadoop-unit.matrix.yml")))
        );
        this.resolver = new HadoopUnitClusterInfoResolver(yamlPropertiesFactoryBean.getObject());
    }

    @Test
    public void resolveEnvironmentVariables() throws URISyntaxException {

        // Given
        Cluster cluster = new Cluster();
        cluster.setUri(HadoopUnitClusterInfoResolver.class.getClassLoader().getResource("hadoop-unit").toURI());

        // When
        Map<String, List<String>> envVars = resolver.resolveEnvironmentVariables(Paths.get("/archives/path"), Paths.get("/cluster/conf/path"), cluster);

        // Then
        assertEquals(8, envVars.size());
        // TODO
//        assertTrue(envVars.containsKey("HADOOP_HOME"));
//        assertEquals("", envVars.get("HADOOP_HOME"));
    }

    @Test(expected = RuntimeException.class)
    public void resolveAvailableServices_unknownPath() throws URISyntaxException {

        // Given
        Cluster cluster = new Cluster();
        cluster.setUri(HadoopUnitClusterInfoResolver.class.getClassLoader().getResource("conf-unknown").toURI());

        // When
        resolver.resolveAvailableServiceClients(cluster);

    }

    @Test
    public void resolveAvailableServices() throws URISyntaxException {

        // Given
        Cluster cluster = new Cluster();
        cluster.setUri(HadoopUnitClusterInfoResolver.class.getClassLoader().getResource("hadoop-unit").toURI());

        // When
        Collection<ServiceClientAndVersion> services = resolver.resolveAvailableServiceClients(cluster);

        // Then
        assertThat(services).hasSize(7).containsExactlyInAnyOrder(
                ServiceClientAndVersion.of("hive", "2.1.0"),
                ServiceClientAndVersion.of("yarn", "2.7.3"),
                ServiceClientAndVersion.of("hdfs", "2.7.3"),
                ServiceClientAndVersion.of("oozie", "4.2.0"),
                ServiceClientAndVersion.of("kafka", "0.10.1.1"),
                ServiceClientAndVersion.of("zookeeper", "3.4.6"),
                ServiceClientAndVersion.of("hbase", "1.1.0")
        );
    }

    @Test
    public void resolveEnvironmentVariablesFromServices() throws URISyntaxException {

        // Given
        ServiceClientAndVersion hive = ServiceClientAndVersion.of(DefaultServices.HIVE, "1.2.1000");
        ServiceClientAndVersion hdfs = ServiceClientAndVersion.of(DefaultServices.HDFS, "1.2.1000");
        ServiceClientAndVersion hbase = ServiceClientAndVersion.of(DefaultServices.HBASE, "1.2.1000");
        ServiceClientAndVersion yarn = ServiceClientAndVersion.of(DefaultServices.YARN, "1.2.1000");
        ServiceClientAndVersion zookeeper = ServiceClientAndVersion.of(DefaultServices.ZOOKEEPER, "1.2.1000");
        ServiceClientAndVersion oozie = ServiceClientAndVersion.of(DefaultServices.OOZIE, "1.2.1000");
        List<ServiceClientAndVersion> services = Arrays.asList(hive, hdfs, hbase, yarn, zookeeper, oozie);

        Cluster cluster = new Cluster();
        cluster.setUri(HadoopUnitClusterInfoResolver.class.getClassLoader().getResource("hadoop-unit").toURI());
        Path clusterGeneratedPath = Paths.get("/base/conf_for_cluster/");

        // When
        Map<String, List<String>> envVars = resolver.resolveEnvironmentVariablesFromServices(clusterGeneratedPath, services, cluster);

        // Then
        assertThat(envVars).hasSize(8)
                .containsEntry("HADOOP_CONF_DIR", Collections.singletonList(clusterGeneratedPath.resolve("hdfs").toAbsolutePath().toString()))
                .containsEntry("HADOOP_CLIENT_OPTS", Collections.singletonList("-Xmx1g"))
                .containsEntry("MAPRED_DISTCP_OPTS", Collections.singletonList("-Xmx2g"))
                .containsEntry("HADOOP_DISTCP_OPTS", Collections.singletonList("-Xmx2g"))
                .containsEntry("HBASE_CONF_DIR", Collections.singletonList(clusterGeneratedPath.resolve("hbase").toAbsolutePath().toString()))
                .containsEntry("YARN_CONF_DIR", Collections.singletonList(clusterGeneratedPath.resolve("yarn").toAbsolutePath().toString()))
                .containsEntry("ZOOKEEPER_CONF_DIR", Collections.singletonList(clusterGeneratedPath.resolve("zookeeper").toAbsolutePath().toString()))
                .containsEntry("OOZIE_URL", Collections.singletonList("http://localhost:20113/oozie"))
        ;
    }

    @Test
    public void renderConfigurationFiles() throws IOException, URISyntaxException {

        // Given
        Cluster cluster = new Cluster();
        cluster.setUri(HadoopUnitClusterInfoResolver.class.getClassLoader().getResource("hadoop-unit").toURI());

        // When
        Map<String, Map<String, byte[]>> configurationFiles = resolver.renderConfigurationFiles(cluster);

        // Then
        assertThat(configurationFiles).hasSize(7).containsOnlyKeys("hdfs", "kafka", "hive", "zookeeper", "yarn", "oozie", "hbase");

        assertThat(configurationFiles.get("hdfs")).hasSize(2).containsOnlyKeys("core-site.xml", "hdfs-site.xml");
        assertThat(new String(configurationFiles.get("hdfs").get("hdfs-site.xml")))
                .isEqualTo(new String(IOUtils.toByteArray(HadoopUnitClusterInfoResolver.class.getClassLoader().getResourceAsStream("hdfs-site.xml"))));
        assertThat(new String(configurationFiles.get("hdfs").get("core-site.xml")))
                .isEqualTo(new String(IOUtils.toByteArray(HadoopUnitClusterInfoResolver.class.getClassLoader().getResourceAsStream("core-site.xml"))));

        assertThat(configurationFiles.get("hbase")).hasSize(1).containsOnlyKeys("hbase-site.xml");
        assertThat(new String(configurationFiles.get("hbase").get("hbase-site.xml")))
                .isEqualTo(new String(IOUtils.toByteArray(HadoopUnitClusterInfoResolver.class.getClassLoader().getResourceAsStream("hbase-site.xml"))));

        assertThat(configurationFiles.get("hive")).hasSize(1).containsOnlyKeys("hive-site.xml");
        assertThat(new String(configurationFiles.get("hive").get("hive-site.xml")))
                .isEqualTo(new String(IOUtils.toByteArray(HadoopUnitClusterInfoResolver.class.getClassLoader().getResourceAsStream("hive-site.xml"))));
    }
}
