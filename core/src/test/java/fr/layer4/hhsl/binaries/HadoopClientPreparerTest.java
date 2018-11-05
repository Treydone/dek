package fr.layer4.hhsl.binaries;

import com.google.common.io.Files;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.io.File;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.assertj.core.api.Assertions.assertThat;

public class HadoopClientPreparerTest {

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(dsl(
            service("dist.apache.org")
                    .get("/repos/dist/release/hadoop/common/hadoop-2.8.5/hadoop-2.8.5.tar.gz.mds")
                    .willReturn(success()
                            .body(
                                    "/build/source/target/artifacts/hadoop-2.8.5.tar.gz: \n" +
                                            "   MD5 = 8E 48 B6 01 54 64 ED 19  9D 26 08 0B 2F F0 2D 65\n" +
                                            "/build/source/target/artifacts/hadoop-2.8.5.tar.gz: \n" +
                                            "  SHA1 = FC10 37CE 9A60 1EA0 1D35  FF2A A286 2586 3B38 09C3\n" +
                                            "/build/source/target/artifacts/hadoop-2.8.5.tar.gz: \n" +
                                            "RMD160 = 5BB7 DF15 0DC9 0BA8 9C1E  8CDC 87EB A555 19CE 7BDB\n" +
                                            "/build/source/target/artifacts/hadoop-2.8.5.tar.gz: \n" +
                                            "SHA224 = ADF41CD6 1EC2A739 31C7ED9D 1183DB03 C5A985B5 A2296CC9 F0315C22\n" +
                                            "/build/source/target/artifacts/hadoop-2.8.5.tar.gz: \n" +
                                            "SHA256 = F9C726DF 693CE2DA A4107886 F603270D 66E7257F 77A92C98 86502D6C D4A884A4\n" +
                                            "/build/source/target/artifacts/hadoop-2.8.5.tar.gz: \n" +
                                            "SHA384 = 905D941A 9FBC9C46 A5A883EA 5CFF3FA1 629D6212 0B35B540 3B5E5AAD 0A35B32D\n" +
                                            "         4773734B 08C9EE5A A1A24548 3A02F53A\n" +
                                            "/build/source/target/artifacts/hadoop-2.8.5.tar.gz: \n" +
                                            "SHA512 = 4174E7A6 7B614B7D 5E47A1A2 420CBE9A 57978908 F8AD0405 F1D17730 6FB36ED8\n" +
                                            "         7C895810 F70E3C6A 6CBADC76 AFB9303F 1C49CBCA 67237E18 C799D30F 87AFA57C")),

            service("api.github.com")
                    .get("/repos/steveloughran/winutils")
                    .willReturn(success()
                            .header("Content-type", "application/json; charset=utf-8")
                            .body("{\n" +
                                    "  \"id\": 42450016,\n" +
                                    "  \"node_id\": \"MDEwOlJlcG9zaXRvcnk0MjQ1MDAxNg==\",\n" +
                                    "  \"name\": \"winutils\",\n" +
                                    "  \"full_name\": \"steveloughran/winutils\"" +
                                    "}"))
                    .get("/repos/steveloughran/winutils/contents/hadoop-2.8.1/bin")
                    .queryParam("ref", "master")
                    .willReturn(success()
                            .header("Content-type", "application/json; charset=utf-8")
                            .body("[\n" +
                                    "  {\n" +
                                    "    \"name\": \"OnOutOfMemory.cmd\",\n" +
                                    "    \"path\": \"hadoop-2.8.1/bin/OnOutOfMemory.cmd\",\n" +
                                    "    \"download_url\": \"https://raw.githubusercontent.com/steveloughran/winutils/master/hadoop-2.8.1/bin/OnOutOfMemory.cmd\",\n" +
                                    "    \"type\": \"file\"" +
                                    "  }\n," +
                                    "  {\n" +
                                    "    \"name\": \"hadoop\",\n" +
                                    "    \"path\": \"hadoop-2.8.1/bin/hadoop\",\n" +
                                    "    \"download_url\": \"https://raw.githubusercontent.com/steveloughran/winutils/master/hadoop-2.8.1/bin/hadoop\",\n" +
                                    "    \"type\": \"file\"" +
                                    "  }\n" +
                                    "]"))

                    .get("/repos/steveloughran/winutils/contents/hadoop-2.8.1/bin/OnOutOfMemory.cmd")
                    .willReturn(success()
                            .body("dummy content for OnOutOfMemory.cmd"))

                    .get("/repos/steveloughran/winutils/contents/hadoop-2.8.1/bin/hadoop")
                    .willReturn(success()
                            .body("dummy content for hadoop"))
    ));

    private HadoopClientPreparer hadoopClientPreparer;

    @Before
    public void init() {
        this.hadoopClientPreparer = new HadoopClientPreparer(null, new RestTemplate(), null);
    }

    @Test
    public void getRemoteSha256() {

        // Given

        // When
        String sha256 = this.hadoopClientPreparer.getRemoteSha256("hadoop-2.8.5.tar.gz", "2.8.5");

        // Then
        assertThat(sha256).isEqualTo("F9C726DF693CE2DAA4107886F603270D66E7257F77A92C9886502D6CD4A884A4");

    }

    @Test
    public void downloadWinUtilsBinaries() {

        // Given
        File tempFile = Files.createTempDir();

        // When
        HadoopClientPreparer.downloadWinUtilsBinaries(true, tempFile, "2.8.1");

        // Then
        assertThat(tempFile).isDirectory();
        File bin = new File(tempFile, "bin");
        assertThat(bin.list()).containsExactlyInAnyOrder("OnOutOfMemory.cmd", "hadoop");
        assertThat(new File(bin, "OnOutOfMemory.cmd")).hasContent("dummy content for OnOutOfMemory.cmd");
        assertThat(new File(bin, "hadoop")).hasContent("dummy content for hadoop");

    }

    @Test
    public void findWinUtilsMatchingVersion() {

        assertThat(HadoopClientPreparer.findWinUtilsMatchingVersion("2.5.x")).isEqualTo("2.6.0");
        assertThat(HadoopClientPreparer.findWinUtilsMatchingVersion("2.6.0")).isEqualTo("2.6.0");
        assertThat(HadoopClientPreparer.findWinUtilsMatchingVersion("2.6.2")).isEqualTo("2.6.3");
        assertThat(HadoopClientPreparer.findWinUtilsMatchingVersion("2.6.3")).isEqualTo("2.6.3");
        assertThat(HadoopClientPreparer.findWinUtilsMatchingVersion("2.6.9")).isEqualTo("2.6.4");
        assertThat(HadoopClientPreparer.findWinUtilsMatchingVersion("2.7.0")).isEqualTo("2.7.1");
        assertThat(HadoopClientPreparer.findWinUtilsMatchingVersion("2.8.0")).isEqualTo("2.8.1");
        assertThat(HadoopClientPreparer.findWinUtilsMatchingVersion("2.8.0-RC2")).isEqualTo("2.8.1");
        assertThat(HadoopClientPreparer.findWinUtilsMatchingVersion("2.8.2")).isEqualTo("2.8.1");
        assertThat(HadoopClientPreparer.findWinUtilsMatchingVersion("2.8.3")).isEqualTo("2.8.3");
        assertThat(HadoopClientPreparer.findWinUtilsMatchingVersion("2.8.3-SNAPSHOT")).isEqualTo("2.8.3");
        assertThat(HadoopClientPreparer.findWinUtilsMatchingVersion("2.9.1")).isEqualTo("2.8.3");
        assertThat(HadoopClientPreparer.findWinUtilsMatchingVersion("2.9.9")).isEqualTo("2.8.3");
        assertThat(HadoopClientPreparer.findWinUtilsMatchingVersion("3.0.0")).isEqualTo("3.0.0");
        assertThat(HadoopClientPreparer.findWinUtilsMatchingVersion("3.1.0")).isEqualTo("3.0.0");

    }
}
