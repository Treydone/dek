package fr.layer4.dek.banner;

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

import fr.layer4.dek.Cluster;
import org.assertj.core.api.Assertions;
import org.jline.terminal.TerminalBuilder;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

public class BannerTest {

    @Test
    public void render_testBannerFromTerminal() throws IOException {

        System.err.println(new Banner(
                "<#list 1..width as x>-</#list>\n" +
                        " DEK\n" +
                        " Version: TODO\n" +
                        " Terminal\n" +
                        " - size: ${terminal.size}\n" +
                        " - class: ${statics['org.springframework.aop.support.AopUtils'].getTargetClass(terminal)}\n" +
                        " - type: ${terminal.type}\n" +
                        " - attributes: ${terminal.attributes}\n" +
                        "<#list 1..width as x>-</#list>\n",
                Collections.singletonMap("terminal", TerminalBuilder.terminal())).render(200));
    }

    @Test
    public void render_defaultBanner() {

        // Given
        Cluster cluster = new Cluster();
        cluster.setName("Test Cluster HDP");
        Banner banner = new Banner(Banner.DEFAULT_CLUSTER_BANNER, Collections.singletonMap("cluster", cluster));

        // When
        CharSequence render = banner.render(8);

        // Then
        Assertions.assertThat(render).isEqualTo("--------\nUsing Test Cluster HDP\n--------");

    }
}
