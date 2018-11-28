package fr.layer4.dek.binaries;

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

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
                                            "         7C895810 F70E3C6A 6CBADC76 AFB9303F 1C49CBCA 67237E18 C799D30F 87AFA57C"))

                    .get("/repos/dist/release/hadoop/common/hadoop-2.7.7/hadoop-2.7.7.tar.gz.mds")
                    .willReturn(success()
                            .body(
                                    "hadoop-2.7.7.tar.gz:    MD5 = CC 2F 01 9F 2A 41 45 8D  F8 43 0C 44 8B B9 F7 60\n" +
                                            "hadoop-2.7.7.tar.gz:   SHA1 = DA60 1BF9 79CB 63DB 78EC  F85A 617B 4FF4 B265 5D23\n" +
                                            "hadoop-2.7.7.tar.gz: RMD160 = 0C91 94F1 C22A DB54 0B23  4841 4B80 2074 D4AF 56A2\n" +
                                            "hadoop-2.7.7.tar.gz: SHA224 = 83D70DC7 D579DDB4 3B3BA00D BA1A9695 2D73A0A4\n" +
                                            "                              9286F404 219186AB\n" +
                                            "hadoop-2.7.7.tar.gz: SHA256 = D129D08A 2C9DAFEC 32855A37 6CBD2AB9 0C6A4279\n" +
                                            "                              0898CABB AC6BE4D2 9F9C2026\n" +
                                            "hadoop-2.7.7.tar.gz: SHA384 = 7FD6F49A F16D4324 988B41B6 46C690B6 AFBABB24\n" +
                                            "                              BA18C123 571AE4E2 277495EA 31335245 BF767707\n" +
                                            "                              ED44BBAF 9A978F3F\n" +
                                            "hadoop-2.7.7.tar.gz: SHA512 = 17C89172 11DD4C25 F78BF601 30A390F9 E273B014\n" +
                                            "                              9737094E 45F4AE5C 917B1174 B97EB908 18C5DF06\n" +
                                            "                              8E607835 12012628 1BCC0751 4F38BD7F D3CB8E9D\n" +
                                            "                              3DB1BDDE")),

            service("api.github.com")
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
                                    "]")),

            service("raw.githubusercontent.com")
                    .get("/steveloughran/winutils/master/hadoop-2.8.1/bin/OnOutOfMemory.cmd")
                    .willReturn(success()
                            .body("dummy content for OnOutOfMemory.cmd"))

                    .get("/steveloughran/winutils/master/hadoop-2.8.1/bin/hadoop")
                    .willReturn(success()
                            .body("dummy content for hadoop"))
    ));

    private HadoopClientPreparer hadoopClientPreparer;

    @Before
    public void init() {
        this.hadoopClientPreparer = new HadoopClientPreparer(null, new RestTemplate(), null);
    }

    @Test
    public void getRemoteSha256_simpleOneLine() {

        // Given

        // When
        String sha256 = this.hadoopClientPreparer.getRemoteSha256("hadoop-2.8.5.tar.gz", "2.8.5");

        // Then
        assertThat(sha256).isEqualTo("F9C726DF693CE2DAA4107886F603270D66E7257F77A92C9886502D6CD4A884A4");

    }

    @Test
    public void getRemoteSha256_multiLine() {

        // Given

        // When
        String sha256 = this.hadoopClientPreparer.getRemoteSha256("hadoop-2.7.7.tar.gz", "2.7.7");

        // Then
        assertThat(sha256).isEqualTo("D129D08A2C9DAFEC32855A376CBD2AB90C6A42790898CABBAC6BE4D29F9C2026");

    }

    @Test
    public void downloadWinUtilsBinaries() throws IOException {

        // Given
        File tempFile = Files.createTempDirectory("winutils").toFile();

        // When
        HadoopClientPreparer.downloadWinUtilsBinaries(new RestTemplate(), true, tempFile, "2.8.1");

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
