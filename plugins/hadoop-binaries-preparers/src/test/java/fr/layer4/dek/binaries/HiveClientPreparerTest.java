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

public class HiveClientPreparerTest {

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(dsl(
            service("dist.apache.org")
                    .get("/repos/dist/release/hive/hive-3.1.1/apache-hive-3.1.1-bin.tar.gz.sha256")
                    .willReturn(success()
                            .body("74db1859c3af4fcd39373c6caa99ff4c7e38dff3bcb9a198b7457c63b7e3c054  apache-hive-3.1.1-bin.tar.gz\n"))
    ));

    private HiveClientPreparer hiveClientPreparer;

    @Before
    public void init() {
        this.hiveClientPreparer = new HiveClientPreparer(null, new RestTemplate(), null);
    }

    @Test
    public void getRemoteSha256() {

        // Given

        // When
        String sha256 = this.hiveClientPreparer.getRemoteSha256("apache-hive-3.1.1-bin.tar.gz", "3.1.1");

        // Then
        assertThat(sha256).isEqualTo("74db1859c3af4fcd39373c6caa99ff4c7e38dff3bcb9a198b7457c63b7e3c054");

    }
}
