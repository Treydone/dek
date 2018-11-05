package fr.layer4.hhsl.commands;

import org.springframework.shell.table.*;

import static org.springframework.shell.table.CellMatchers.at;

public class CommandUtils {

    public static Table getTable(String[][] data) {
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
}
