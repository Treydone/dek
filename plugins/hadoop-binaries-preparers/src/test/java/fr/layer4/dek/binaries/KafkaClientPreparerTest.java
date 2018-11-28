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

public class KafkaClientPreparerTest {

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(dsl(
            service("dist.apache.org")
                    .get("/repos/dist/release/kafka/2.1.0/kafka_2.11-2.1.0.tgz.sha512")
                    .willReturn(success()
                            .body(
                                    "kafka_2.11-2.1.0.tgz: B620243C 9D042B3F 3FD73F5D 216E7B2E F01E9B0C 444035F7\n" +
                                            "                      D3EB2962 E6E519D8 E599CB0C 602F2835 E9A94E54 679ED674\n" +
                                            "                      DF14FCD6 FBC7A9CB 3A6C0832 42A4650A\n"))
    ));

    private KafkaClientPreparer kafkaClientPreparer;

    @Before
    public void init() {
        this.kafkaClientPreparer = new KafkaClientPreparer(null, new RestTemplate(), null);
    }

    @Test
    public void getRemoteSha512() {

        // Given

        // When
        String sha256 = this.kafkaClientPreparer.getRemoteSha512("kafka_2.11-2.1.0.tgz", "2.1.0");

        // Then
        assertThat(sha256).isEqualTo("B620243C9D042B3F3FD73F5D216E7B2EF01E9B0C444035F7D3EB2962E6E519D8E599CB0C602F2835E9A94E54679ED674DF14FCD6FBC7A9CB3A6C083242A4650A");

    }
}
