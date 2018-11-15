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

import fr.layer4.hhsl.GitProperties;
import fr.layer4.hhsl.SpringUtils;
import fr.layer4.hhsl.banner.Banner;
import lombok.extern.slf4j.Slf4j;
import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ShellComponent
public class InfoCommands {

    @Autowired
    @Lazy
    private Terminal terminal;

    @Autowired
    private GitProperties gitProperties;

    @ShellMethod(key = "info", value = "Info", group = "Others")
    public Banner info() throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("terminal", SpringUtils.getTargetObject(this.terminal));
        model.put("git", this.gitProperties);

        return new Banner(
                "<#list 1..width as x>-</#list>\n" +
                        " HHSL\n" +
                        " Version: ${(git.version)!}\n" +
                        " Commit: ${(git.commit.id)!} (${(git.remote)!})\n" +
                        " Build: ${(git.build.time)!}\n" +
                        " Terminal\n" +
                        " - size: ${terminal.size}\n" +
                        " - class: ${terminal.kind}\n" +
                        " - type: ${terminal.type}\n" +
                        " - attributes: ${terminal.attributes}\n" +
                        "<#list 1..width as x>-</#list>\n",
                model
        );
    }
}
