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

import fr.layer4.hhsl.PropertyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.table.Table;

import java.util.Iterator;
import java.util.Map;

@Slf4j
@ShellComponent
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EnvCommands {

    private final PropertyManager propertyManager;

    @ShellMethod(key = "list env", value = "List all env", group = "Env")
    public Table env() {
        log.debug("List all env");
        Map<String, String> env = this.propertyManager.getProperty();

        String[][] data = new String[env.size() + 1][];
        data[0] = new String[]{"Key", "Value"};

        int it = 1;
        Iterator<Map.Entry<String, String>> iterator = env.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
            data[it] = new String[]{next.getKey(), hideSensibleData(next)};
            it++;
        }
        return CommandUtils.getTable(data);
    }

    protected String hideSensibleData(Map.Entry<String, String> next) {
        if (next.getKey().contains("password")
                || next.getKey().contains("secret")
                || next.getKey().contains("private")) {
            return "********";
        }
        return next.getValue();
    }

    @ShellMethod(key = "get env", value = "Get env", group = "Env")
    public String get(String key) {
        log.debug("Get env for {}", key);
        return this.propertyManager.getProperty(key).orElseThrow(() -> new RuntimeException("Not found")); // TODO
    }

    @ShellMethod(key = "set env", value = "Set env", group = "Env")
    public void set(String key, String value) {
        log.debug("Set env for {} with value {}", key, value);
        this.propertyManager.setProperty(key, value);
    }

    @ShellMethod(key = "delete env", value = "Delete env", group = "Env")
    public void del(String key) {
        log.debug("Delete env for {}", key);
        this.propertyManager.deleteProperty(key);
    }
}
