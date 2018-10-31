package fr.layer4.hhsl.binaries;

/*-
 * #%L
 * HHSL
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

import java.net.URI;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.assertj.core.api.Assertions.assertThat;

public class ApacheMirrorFinderTest {

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(dsl(
            service("www.apache.org")

                    .get("/dyn/closer.cgi/hadoop/common/hadoop-2.8.5/hadoop-2.8.5.tar.gz")
                    .queryParam("as_json", "1")
                    .willReturn(success()
                            .header("content-type", "application/json")
                            .body(
                                    "{" +
                                            "    \"backup\": [ \"https://www-eu.apache.org/dist/\", \"https://www-us.apache.org/dist/\" ]," +
                                            "    \"cca2\": \"fr\"," +
                                            "    \"ftp\": [ \"ftp://mirrors.ircam.fr/pub/apache/\" ]," +
                                            "    \"http\": [ \"http://apache.crihan.fr/dist/\", \"http://apache.mediamirrors.org/\", \"http://apache.mirrors.ovh.net/ftp.apache.org/dist/\", \"http://mirror.ibcp.fr/pub/apache/\", \"http://mirrors.ircam.fr/pub/apache/\", \"http://mirrors.standaloneinstaller.com/apache/\", \"http://wwwftp.ciril.fr/pub/apache/\" ]," +
                                            "    \"in_attic\": false," +
                                            "    \"in_dist\": true," +
                                            "    \"path_info\": \"hadoop/common/hadoop-2.8.5/hadoop-2.8.5.tar.gz\"," +
                                            "    \"preferred\": \"http://mirrors.standaloneinstaller.com/apache/\"" +
                                            "}"))
    ));

    private ApacheMirrorFinder apacheMirrorFinder;

    @Before
    public void init() {
        this.apacheMirrorFinder = new ApacheMirrorFinder(new RestTemplate());
    }

    @Test
    public void resolve() {

        // Given

        // When
        URI resolvedPath = this.apacheMirrorFinder.resolve("hadoop/common/hadoop-2.8.5/hadoop-2.8.5.tar.gz");

        // Then
        assertThat(resolvedPath).isEqualTo(URI.create("http://mirrors.standaloneinstaller.com/apache/hadoop/common/hadoop-2.8.5/hadoop-2.8.5.tar.gz"));

    }
}
