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
import fr.layer4.hhsl.store.Store;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.table.*;

import java.util.Iterator;
import java.util.Map;

import static org.springframework.shell.table.CellMatchers.at;

@Slf4j
@ShellComponent
public class EnvCommands {

    @Autowired
    private PropertyManager propertyManager;

    @Autowired
    private Store store;

    @ShellMethodAvailability(value = "*")
    public Availability availabilityAfterUnlock() {
        return Avaibilities.unlockedAndReady(store);
    }

    @ShellMethod(key = "list env", value = "List all env", group = "Env")
    public Table env() {
        log.debug("List all env");
        Map<String, String> env = propertyManager.getProperty();

        String[][] data = new String[env.size()][];

        int it = 0;
        Iterator<Map.Entry<String, String>> iterator = env.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
            data[it] = new String[]{next.getKey(), next.getValue()};
            it++;
        }
        TableModel model = new ArrayTableModel(data);
        TableBuilder tableBuilder = new TableBuilder(model);

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                tableBuilder.on(at(i, j)).addAligner(SimpleHorizontalAligner.left).addSizer(new AbsoluteWidthSizeConstraints(5));
                tableBuilder.on(at(i, j)).addAligner(SimpleVerticalAligner.middle);
            }
        }

        return tableBuilder.build();
    }

    @ShellMethod(key = "get env", value = "Get env", group = "Env")
    public String get(String key) {
        log.debug("Get env for {}", key);
        return propertyManager.getProperty(key).orElseThrow(() -> new RuntimeException("Not found")); // TODO
    }

    @ShellMethod(key = "set env", value = "Set env", group = "Env")
    public void set(String key, String value) {
        log.debug("Set env for {} with value {}", key, value);
        propertyManager.setProperty(key, value);
    }

    @ShellMethod(key = "delete env", value = "Delete env", group = "Env")
    public void del(String key) {
        log.debug("Delete env for {}", key);
        propertyManager.deleteProperty(key);
    }
}
