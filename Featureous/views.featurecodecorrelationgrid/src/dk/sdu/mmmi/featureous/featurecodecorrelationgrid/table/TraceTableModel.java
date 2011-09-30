/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.featurecodecorrelationgrid.table;

import dk.sdu.mmmi.featureous.core.affinity.AffinityProvider;
import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.ClassModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author ao
 */
public abstract class TraceTableModel extends DefaultTableModel implements TableCellRenderer {

    private final List<String> traces;
    private List<String> yAxis;
    private boolean pkg;

    public TraceTableModel(Set<String> traces, boolean compareClassesByRank, boolean compareFeaturesByScattering, boolean pkg) {
        this.pkg = pkg;
        List<String> ts = new ArrayList<String>(traces);
        if (compareFeaturesByScattering) {
            Collections.sort(ts, tcSca);
        } else {
            Collections.sort(ts, tc);
        }

        this.traces = ts;

        Set<ClassModel> typeSet = new HashSet<ClassModel>();
        for (String tm : this.traces) {
            for (ClassModel cm : Controller.getInstance().getTraceSet().getFirstLevelTraceByName(tm).getClassSet()) {
                typeSet.add(cm);
            }
        }
        List<ClassModel> cl = new ArrayList<ClassModel>(typeSet);
        if (compareClassesByRank) {
            Collections.sort(cl, cc);
        }
        this.yAxis = new ArrayList<String>();
        for (ClassModel cm : cl) {
            yAxis.add(cm.getName());
        }
        if (!compareClassesByRank) {
            Collections.sort(this.yAxis);
        }

        if (pkg) {

            Set<String> pkgSet = new HashSet<String>();
            for (String tm : this.getTraces()) {
                for (ClassModel cm : Controller.getInstance().getTraceSet().getFirstLevelTraceByName(tm).getClassSet()) {
                    pkgSet.add(cm.getPackageName());
                }
            }
            ArrayList<String> pkgs = new ArrayList<String>(pkgSet);
            if (!compareClassesByRank) {
                Collections.sort(pkgs);
            } else {
                Collections.sort(pkgs, new Comparator<String>() {

                    public int compare(String o1, String o2) {
                        Integer r1 = Controller.getInstance().getAffinity().getPkgAffinity(o1).weigth;
                        Integer r2 = Controller.getInstance().getAffinity().getPkgAffinity(o2).weigth;
                        return r2.compareTo(r1);
                    }
                });
            }
            this.setyAxis(pkgs);
        }
    }

    @Override
    public String getColumnName(int column) {
        return "";
    }

    public void setyAxis(List<String> yAxis) {
        this.yAxis = yAxis;
    }

    public List<String> getTraces() {
        return traces;
    }

    public List<String> getYAxis() {
        return yAxis;
    }
    private Comparator<String> tc = new Comparator<String>() {

        public int compare(String o1, String o2) {
            return Controller.getInstance().getTraceSet().getFirstLevelTraceByName(o1).getName().compareTo(Controller.getInstance().getTraceSet().getFirstLevelTraceByName(o2).getName());
        }
    };
    private Comparator<String> tcSca = new Comparator<String>() {

        public int compare(String o1, String o2) {
            return new Integer(
                    Controller.getInstance().getTraceSet().getFirstLevelTraceByName(o2).getClassSet().size()).compareTo(Controller.getInstance().getTraceSet().getFirstLevelTraceByName(o1).getClassSet().size());
        }
    };
    private Comparator<ClassModel> cc = new Comparator<ClassModel>() {

        public int compare(ClassModel o1, ClassModel o2) {
            AffinityProvider cc = Controller.getInstance().getAffinity();
            return new Integer(cc.getClassAffinity(o2.getName()).weigth).compareTo(cc.getClassAffinity(o1.getName()).weigth);
        }
    };

    @Override
    public int getColumnCount() {
        if (traces == null) {
            return 0;
        }
        return traces.size() + 1;
    }

    @Override
    public int getRowCount() {
        if (yAxis == null) {
            return 0;
        }
        return yAxis.size() + 1;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row == 0 && column == 0) {
            return "";
        }
        if (traces == null || yAxis == null) {
            return "";
        }
        if (row == 0) {
            return traces.get(column - 1);
        }
        if (column == 0) {
            return yAxis.get(row - 1);
        }

        return getRelation(yAxis.get(row - 1), traces.get(column - 1));
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof String) {
            JLabel label = new JLabel(value.toString());
            if (row == 0 ^ column == 0) {
                if (row == 0) {
                    label = new Turn90Label(value.toString());
                    label.setHorizontalAlignment(JLabel.CENTER);
                    label.setVerticalAlignment(JLabel.BOTTOM);
                } else {
                    label.setVerticalAlignment(JLabel.CENTER);
                }
                label.setBackground(new Color(255, 254, 227));
            } else {
                label.setBackground(Color.white);
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setVerticalAlignment(JLabel.CENTER);
            }

            if (value instanceof JLabel) {
                label = (JLabel) value;
            }

            if (label.getText().length() > 1) {
                label.setToolTipText(value.toString());
            }
            label.setOpaque(true);
            return label;
        }
        
        return (JLabel)value;
    }

    public class Turn90Label extends JLabel {

        public Turn90Label(String s) {
            super(s);
            setPreferredSize(new Dimension(300, 150));
            setMinimumSize(new Dimension(300, 150));
            setToolTipText(s);
        }

        public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(getBackground());
            g2d.fillRect(0, 0, 300, 150);
            g2d.translate(14.0, 145.0);
            g2d.rotate(-Math.PI / 2f);
            g2d.setPaint(getForeground());
            g2d.drawString(getText(), 0, 0);
        }
    }

    public abstract Object getRelation(String cm, String fm);
}
