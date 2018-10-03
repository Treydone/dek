package fr.layer4.hhsl.commands;

import fr.layer4.hhsl.ClusterValuesProvider;
import fr.layer4.hhsl.local.LocalStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.*;

@ShellComponent
public class MainCommands {

    @Autowired
    private LocalStore localStore;

    @ShellMethodAvailability(value = "*")
    public Availability availabilityAfterUnlock() {
        return localStore.isUnlocked()
                ? Availability.available()
                : Availability.unavailable("Secret store is not unlock");
    }

    @ShellMethod(value = "Showcase Table rendering", group = "Main")
    public Table list() {
        String[][] data = new String[3][3];
        TableModel model = new ArrayTableModel(data);
        TableBuilder tableBuilder = new TableBuilder(model);

        data[0] = new String[]{"name", "url", "user"};
        data[1] = new String[]{"PROD-HDP3.0", "http://.....", "ro"};
        data[2] = new String[]{"PROD-HDP2.6", "http://.....", "ro"};

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                tableBuilder.on(at(i, j)).addAligner(SimpleHorizontalAligner.values()[j]);
                tableBuilder.on(at(i, j)).addAligner(SimpleVerticalAligner.values()[i]);
            }
        }

        return tableBuilder.addFullBorder(BorderStyle.fancy_light).build();
    }

    public static CellMatcher at(final int theRow, final int col) {
        return (row, column, model) -> row == theRow && column == col;
    }

    @ShellMethod(value = "Get information about a cluster", group = "Main")
    public void info(@ShellOption(valueProvider = ClusterValuesProvider.class) String cluster) {
        System.out.println("You said " + cluster);
    }
}

