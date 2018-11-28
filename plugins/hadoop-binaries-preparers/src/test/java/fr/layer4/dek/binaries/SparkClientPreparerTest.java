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

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.assertj.core.api.Assertions.assertThat;

public class SparkClientPreparerTest {

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(dsl(
            service("dist.apache.org")
                    .get("/repos/dist/release/spark/spark-2.3.2/spark-2.3.2-bin-hadoop2.7.tgz.sha512")
                    .willReturn(success()
                            .body(
                                    "spark-2.3.2-bin-hadoop2.7.tgz: E61D9330 125746A2 4D778416 6A15B415 14546CAD\n" +
                                            "                               874357C9 8DCA0A2C 39FA7303 D8FA7C04 9BA6CDF5\n" +
                                            "                               A24C172D 4F47A2E5 B6E1F658 A57A9B2A 30D46D98\n" +
                                            "                               58CDB531\n"))
    ));

    private SparkClientPreparer sparkClientPreparer;

    @Before
    public void init() {
        this.sparkClientPreparer = new SparkClientPreparer(null, new RestTemplate(), null);
    }

    @Test
    public void getRemoteSha512() {

        // Given

        // When
        String sha256 = this.sparkClientPreparer.getRemoteSha512("spark-2.3.2-bin-hadoop2.7.tgz", "2.3.2");

        // Then
        assertThat(sha256).isEqualTo("E61D9330125746A24D7784166A15B41514546CAD874357C98DCA0A2C39FA7303D8FA7C049BA6CDF5A24C172D4F47A2E5B6E1F658A57A9B2A30D46D9858CDB531");

    }
}
