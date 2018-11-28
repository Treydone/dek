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

public class CassandraClientPreparerTest {

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(dsl(
            service("dist.apache.org")
                    .get("/repos/dist/release/cassandra/3.11.3/apache-cassandra-3.11.3-bin.tar.gz.sha1")
                    .willReturn(success()
                            .body("dbc6ddbd074d74da97eff66db9699b5ce28ec6f0"))
    ));

    private CassandraClientPreparer cassandraClientPreparer;

    @Before
    public void init() {
        this.cassandraClientPreparer = new CassandraClientPreparer(null, new RestTemplate(), null);
    }

    @Test
    public void getRemoteSha1() {

        // Given

        // When
        String sha256 = this.cassandraClientPreparer.getRemoteSha1("apache-cassandra-3.11.3-bin.tar.gz", "3.11.3");

        // Then
        assertThat(sha256).isEqualTo("dbc6ddbd074d74da97eff66db9699b5ce28ec6f0");

    }
}
