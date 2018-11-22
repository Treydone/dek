package fr.layer4.hhsl.binaries;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.response;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AbstractApacheClientPreparerTest {

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(dsl(
            service("mirrors.standaloneinstaller.com")
                    .get("/apache/hadoop/common/hadoop-2.8.5/hadoop-2.8.5.tar.gz")
                    .willReturn(success()
                            .body("content"))

                    .get("/apache/hadoop/common/hadoop-2.8.6/hadoop-2.8.6.tar.gz")
                    .willReturn(response()
                            .status(404)),

            service("archive.apache.org")
                    .get("/dist/hadoop/common/hadoop-2.8.6/hadoop-2.8.6.tar.gz")
                    .willReturn(success()
                            .body("content fallback"))
    ));

    private AbstractApacheClientPreparer abstractApacheClientPreparer;

    @Mock
    private ApacheMirrorFinder apacheMirrorFinder;

    @Before
    public void init() throws Exception {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setProxy(new HttpHost("localhost", hoverflyRule.getProxyPort()))
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, (x509Certificates, s) -> true).build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
        this.abstractApacheClientPreparer = new AbstractApacheClientPreparer(httpClient, apacheMirrorFinder) {
            @Override
            protected String getApachePart(String archive, String version) {
                return "hadoop/common/hadoop-" + version + "/" + archive;
            }

            @Override
            public boolean isCompatible(String service, String version) {
                return true;
            }

            @Override
            public Map<String, List<String>> prepare(Path basePath, String service, String version, boolean force) {
                return null;
            }
        };
    }

    @Test
    public void download_ok() throws IOException {

        // Given
        Path tempDirectory = Files.createTempDirectory("local");
        Mockito.when(this.apacheMirrorFinder.resolve("hadoop/common/hadoop-2.8.5/hadoop-2.8.5.tar.gz")).thenReturn(URI.create("http://mirrors.standaloneinstaller.com/apache/hadoop/common/hadoop-2.8.5/hadoop-2.8.5.tar.gz"));

        // When
        String resolvedPath = this.abstractApacheClientPreparer.download(tempDirectory, "2.8.5", "hadoop-2.8.5.tar.gz");

        // Then
        assertThat(resolvedPath).isEqualTo("hadoop-2.8.5.tar.gz");
        assertThat(tempDirectory).isDirectory();
        assertThat(tempDirectory.resolve("hadoop-2.8.5.tar.gz")).hasContent("content");

    }

    @Test
    public void download_fallbackToArchives() throws IOException {

        // Given
        Path tempDirectory = Files.createTempDirectory("local");
        Mockito.when(this.apacheMirrorFinder.resolve("hadoop/common/hadoop-2.8.6/hadoop-2.8.6.tar.gz")).thenReturn(URI.create("http://mirrors.standaloneinstaller.com/apache/hadoop/common/hadoop-2.8.6/hadoop-2.8.6.tar.gz"));

        // When
        String resolvedPath = this.abstractApacheClientPreparer.download(tempDirectory, "2.8.6", "hadoop-2.8.6.tar.gz");

        // Then
        assertThat(resolvedPath).isEqualTo("hadoop-2.8.6.tar.gz");
        assertThat(tempDirectory).isDirectory();
        assertThat(tempDirectory.resolve("hadoop-2.8.6.tar.gz")).hasContent("content fallback");

    }
}
