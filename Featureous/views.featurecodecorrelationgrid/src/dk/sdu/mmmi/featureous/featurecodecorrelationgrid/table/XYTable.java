/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.featurecodecorrelationgrid.table;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

/**
 *
 * @author ao
 */
public class XYTable extends JPanel{

    private final TraceTableModel model;
    private final JTable table;
    private final JScrollPane scrollPane;

    public XYTable(TraceTableModel m, int dataColumnWidth) {
        this.setLayout(new BorderLayout());
        table = new JTable(m);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setDragEnabled(false);
        table.getTableHeader().setResizingAllowed(true);
        table.setDefaultRenderer(Object.class, m);
        table.setRowHeight(0, 150);
        table.getColumnModel().getColumn(0).setMaxWidth(450);
        table.getColumnModel().getColumn(0).setMinWidth(200);
        table.getColumnModel().getColumn(0).setPreferredWidth(300);
        for(int i = 1; i<table.getColumnModel().getColumnCount();i++){
            table.getColumnModel().getColumn(i).setMinWidth(dataColumnWidth);
            table.getColumnModel().getColumn(i).setMaxWidth(10*dataColumnWidth);
            table.getColumnModel().getColumn(i).setPreferredWidth(dataColumnWidth);
        }
        model = m;
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        table.setShowGrid(true);
        table.setGridColor(Color.gray);
    }

    public JTable getTable() {
        return table;
    }
}
