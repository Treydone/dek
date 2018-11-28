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

public class StormClientPreparerTest {

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(dsl(
            service("dist.apache.org")
                    .get("/repos/dist/release/storm/apache-storm-1.2.2/apache-storm-1.2.2.tar.gz.sha")
                    .willReturn(success()
                            .body(
                                    "apache-storm-1.2.2.tar.gz: 16EB62DA D4EA40F6 DD8747CF 6A49678E D1A4A53E B3A9E67D\n" +
                                            "                          C53A89F1 471D1DC5 5147E5CA D1AED8B0 B22A01F5 C1F6F6CA\n" +
                                            "                          4B4E9562 61CDA9B6 91D94C16 26593AFB\n"))
    ));

    private StormClientPreparer stormClientPreparer;

    @Before
    public void init() {
        this.stormClientPreparer = new StormClientPreparer(null, new RestTemplate(), null);
    }

    @Test
    public void getRemoteSha512() {

        // Given

        // When
        String sha256 = this.stormClientPreparer.getRemoteSha512("apache-storm-1.2.2.tar.gz", "1.2.2");

        // Then
        assertThat(sha256).isEqualTo("16EB62DAD4EA40F6DD8747CF6A49678ED1A4A53EB3A9E67DC53A89F1471D1DC55147E5CAD1AED8B0B22A01F5C1F6F6CA4B4E956261CDA9B691D94C1626593AFB");

    }
}
