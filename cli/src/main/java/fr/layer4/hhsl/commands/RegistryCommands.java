package fr.layer4.hhsl.commands;

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

import static org.springframework.shell.table.CellMatchers.at;

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
        TableModel model = new ArrayTableModel(data);
        TableBuilder tableBuilder = new TableBuilder(model);
        tableBuilder.addHeaderBorder(BorderStyle.fancy_double);

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                tableBuilder.on(at(i, j)).addAligner(SimpleHorizontalAligner.left).addSizer(new AbsoluteWidthSizeConstraints(5));
                tableBuilder.on(at(i, j)).addAligner(SimpleVerticalAligner.middle);
            }
        }

        return tableBuilder.build();
    }

    @ShellMethod(key = "delete registry", value = "Delete a registry", group = "Registry")
    public void deleteRegistry(String name) {
        registryConnectionManager.deleteRegistry(name);
    }

    @ShellMethod(key = "add registry", value = "Add a registry", group = "Registry")
    public void addRegistry(
            String name,
            String uri) {
        registryConnectionManager.addRegistry(name, uri);
    }
}
