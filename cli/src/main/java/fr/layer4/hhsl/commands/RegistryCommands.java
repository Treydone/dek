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

import fr.layer4.hhsl.registry.RegistryConnection;
import fr.layer4.hhsl.registry.RegistryConnectionManager;
import fr.layer4.hhsl.store.Store;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.table.*;

import java.util.Iterator;
import java.util.List;

@Slf4j
@ShellComponent
public class RegistryCommands {

    @Autowired
    private RegistryConnectionManager registryConnectionManager;

    @Autowired
    private Store store;

    @ShellMethodAvailability(value = "*")
    public Availability availabilityAfterUnlock() {
        return Avaibilities.unlockedAndReady(store);
    }

    @ShellMethod(key = "list registry", value = "List all registries", group = "Registry")
    public Table listRegistries() {
        List<RegistryConnection> registries = registryConnectionManager.listRegistries();

        String[][] data = new String[registries.size() + 1][];
        data[0] = new String[]{"Name", "URI"};

        int it = 1;
        Iterator<RegistryConnection> iterator = registries.iterator();
        while (iterator.hasNext()) {
            RegistryConnection next = iterator.next();
            data[it] = new String[]{next.getName(), next.getUri().toString()};
            it++;
        }
        return CommandUtils.getTable(data);
    }

    @ShellMethod(key = "delete registry", value = "Delete a registry", group = "Registry")
    public void deleteRegistry(String name) {
        registryConnectionManager.deleteRegistry(name);
    }

    @ShellMethod(key = "add registry", value = "Add a registry", group = "Registry")
    public void addRegistry(
            String name,
            String uri) {
        registryConnectionManager.addOrUpdateRegistry(name, uri);
    }
}
