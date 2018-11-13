package fr.layer4.hhsl.commands;

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

import fr.layer4.hhsl.banner.Banner;
import lombok.extern.slf4j.Slf4j;
import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.Collections;

@Slf4j
@ShellComponent
public class InfoCommands {

    @Autowired
    @Lazy
    private Terminal terminal;

    @ShellMethod(key = "info", value = "Info", group = "Others")
    public Banner info() {
        return new Banner(
                "<#list 1..width as x>-</#list>\n" +
                        " HHSL\n" +
                        " Version: TODO\n" +
                        " Terminal\n" +
                        " - size: ${terminal.size}\n" +
                        " - class: ${statics['org.springframework.aop.support.AopUtils'].getTargetClass(terminal)}\n" +
                        " - type: ${terminal.type}\n" +
                        " - attributes: ${terminal.attributes}\n" +
                        "<#list 1..width as x>-</#list>\n",
                Collections.singletonMap("terminal", this.terminal)
        );
    }
}
