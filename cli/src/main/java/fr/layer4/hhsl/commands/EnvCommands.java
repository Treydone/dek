package fr.layer4.hhsl.commands;

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
