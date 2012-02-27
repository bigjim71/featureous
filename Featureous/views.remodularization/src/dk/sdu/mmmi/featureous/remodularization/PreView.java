/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.ui.OutputUtil;
import dk.sdu.mmmi.featureous.remodularization.logic.MainRemodularization;
import dk.sdu.mmmi.featureous.remodularization.logic.objectives.ImpactCohesionObjective;
import dk.sdu.mmmi.featureous.remodularization.logic.objectives.ImpactCouplingObjective;
import dk.sdu.mmmi.featureous.remodularization.logic.objectives.ImpactPackageCountObjective;
import dk.sdu.mmmi.featureous.remodularization.logic.objectives.ImpactScatteringObjective;
import dk.sdu.mmmi.featureous.remodularization.logic.objectives.ImpactTanglingObjective;
import dk.sdu.mmmi.featureous.remodularization.spi.RemodularizationObjectiveProvider;
import dk.sdu.mmmi.featureous.remodularization.workbench.RemodularizationWorkbench;
import dk.sdu.mmmi.srcUtils.sdm.RecoderModelExtractor;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;

/**
 *
 * @author andrzejolszak
 */
public class PreView extends JPanel {

    private ArrayList<RemodularizationObjectiveProvider> allProviders;
    private Set<RemodularizationObjectiveProvider> selectedProviders;
    private JButton computeRemod = new JButton("Compute new modularization...");
    private JCheckBox factorSingle = new JCheckBox("Enforce feature-specific packages");
    private StaticDependencyModel orgSdm;
    private final Project proj;
    private final JTabbedPane tabbedPane;
    private final AtomicInteger iters = new AtomicInteger();
    private final JSlider itersSlider;
    private final JLabel itersLabl;
    private final JSlider pop;
    private final JLabel popLabl;
    private final JSlider mut;
    private final JLabel mutLabl;

    public PreView(Project proj, final JTabbedPane tabbedPane) {
        super(new BorderLayout());
        MainRemodularization.resetPrevParetoFront();
        this.proj = proj;
        this.tabbedPane = tabbedPane;
        final JPanel preToolbar = new JPanel(new BorderLayout());
        JPanel computePanel = new JPanel(new BorderLayout());
        computePanel.add(computeRemod, BorderLayout.SOUTH);

        factorSingle.setSelected(true);
        computePanel.add(factorSingle, BorderLayout.NORTH);

        JPanel mogga = new JPanel(new GridLayout(3, 2));

        itersSlider = new JSlider(1, 2000, 100);
        itersSlider.setFocusable(false);
        itersSlider.setToolTipText("Iterations");
        itersLabl = new JLabel("100");
        iters.set(100);
        itersSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent ce) {
                itersLabl.setText("" + itersSlider.getValue());
                iters.set(itersSlider.getValue());
            }
        });
        mogga.add(itersSlider);
        mogga.add(itersLabl);

        pop = new JSlider(1, 2000, 100);
        pop.setFocusable(false);
        pop.setToolTipText("Population");
        popLabl = new JLabel("100");
        pop.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent ce) {
                popLabl.setText("" + pop.getValue());
            }
        });
        mogga.add(pop);
        mogga.add(popLabl);

        mut = new JSlider(0, 100, 3);
        mut.setFocusable(false);
        mut.setToolTipText("Mutation");
        mutLabl = new JLabel("3%");
        mut.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent ce) {
                mutLabl.setText("" + mut.getValue() + "%");
            }
        });
        mogga.add(mut);
        mogga.add(mutLabl);
        computePanel.add(mogga, BorderLayout.CENTER);

        preToolbar.add(computePanel, BorderLayout.EAST);
        RecoderModelExtractor.extractSdmAndRunAsync(new RecoderModelExtractor.RunnableWithSdm() {

            @Override
            public void run(StaticDependencyModel sdmOrNull) {
                setupObjectiveTable(preToolbar);

                orgSdm = sdmOrNull;
                if (sdmOrNull != null) {
                    OutputUtil.log("Static dependency model extracted [pkgs, types]: " + sdmOrNull.getPackages().size() + ", " + sdmOrNull.getAllTypesCount());
                } else {
                    computeRemod.setEnabled(false);
                    computeRemod.setToolTipText("To remodularize, please select the project containing the sources as Main!");
                }

                Set<TraceModel> tms = dk.sdu.mmmi.featureous.core.controller.Controller.getInstance().getTraceSet().getFirstLevelTraces();

                if (orgSdm != null) {
                    ImpactPackageCountObjective.init(tms, orgSdm);

                    ImpactCohesionObjective.init(tms, orgSdm);
                    ImpactCouplingObjective.init(tms, orgSdm);

                    ImpactScatteringObjective.init(tms, orgSdm);
                    ImpactTanglingObjective.init(tms, orgSdm);
                }

                RemodularizationWorkbench preView = new RemodularizationWorkbench(orgSdm, false);

                add(preToolbar, BorderLayout.SOUTH);
                JComponent sat = preView.getSatelliteView();
                preToolbar.add(sat, BorderLayout.WEST);


                add(preView, BorderLayout.CENTER);
                preToolbar.invalidate();
                preToolbar.validate();

                tabbedPane.addTab("Original program", PreView.this);
                tabbedPane.setSelectedComponent(PreView.this);
            }
        }, proj);

    }

    public JButton getComputeRemod() {
        return computeRemod;
    }

    private void setupObjectiveTable(JPanel preToolbar) {

        Collection<? extends RemodularizationObjectiveProvider> objectives = Lookup.getDefault().lookupAll(RemodularizationObjectiveProvider.class);
        allProviders = new ArrayList<RemodularizationObjectiveProvider>(objectives);
        Collections.sort(allProviders, new Comparator<RemodularizationObjectiveProvider>() {

            @Override
            public int compare(RemodularizationObjectiveProvider t, RemodularizationObjectiveProvider t1) {
                return t.getObjectiveName().compareTo(t1.getObjectiveName());
            }
        });

        selectedProviders = new HashSet<RemodularizationObjectiveProvider>();

        JTable objectiveTable = new JTable(new AbstractTableModel() {

            private List<RemodularizationObjectiveProvider> provs;
            private Map<RemodularizationObjectiveProvider, Boolean> checkBoxes;

            @Override
            public int getRowCount() {
                return allProviders.size();
            }

            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex == 0;
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                checkBoxes.put(provs.get(rowIndex), (Boolean) aValue);
                selectedProviders.clear();
                for (RemodularizationObjectiveProvider pr : provs) {
                    if (checkBoxes.get(pr)) {
                        selectedProviders.add(pr);
                    }
                }
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                } else {
                    return String.class;
                }
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                if (provs == null) {
                    provs = new ArrayList<RemodularizationObjectiveProvider>();
                    checkBoxes = new HashMap<RemodularizationObjectiveProvider, Boolean>();
                    provs.addAll(allProviders);
                    for (RemodularizationObjectiveProvider prov : provs) {
                        checkBoxes.put(prov, new Boolean(false));
                    }
                }
                if (columnIndex == 0) {
                    return checkBoxes.get(provs.get(rowIndex));
                } else if (columnIndex == 1) {
                    return provs.get(rowIndex).getObjectiveName();
                }
                return "Err";
            }

            @Override
            public String getColumnName(int column) {
                if (column == 0) {
                    return "Enabled";
                }
                if (column == 1) {
                    return "Objective";
                }
                return "Err";
            }
        });
        objectiveTable.getValueAt(0, 0);
        objectiveTable.setValueAt(false, 0, 0);
        objectiveTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        objectiveTable.getColumnModel().getColumn(0).setMaxWidth(40);
        objectiveTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        objectiveTable.getColumnModel().getColumn(0).setWidth(40);

        JScrollPane tablePane = new JScrollPane(objectiveTable);
//        tablePane.add(objectiveTable.getTableHeader(), BorderLayout.PAGE_START);
//        tablePane.add(objectiveTable, BorderLayout.CENTER);
        tablePane.setBorder(new LineBorder(java.awt.SystemColor.activeCaptionBorder, 1));
        tablePane.setMaximumSize(new Dimension(800, 100));
        tablePane.setPreferredSize(new Dimension(800, 100));

        preToolbar.add(tablePane, BorderLayout.CENTER);
    }

    public JCheckBox getFactorSingle() {
        return factorSingle;
    }

    public StaticDependencyModel getOrgSdm() {
        return orgSdm;
    }

    public Set<RemodularizationObjectiveProvider> getSelectedProviders() {
        return selectedProviders;
    }

    public int getPopulation() {
        return pop.getValue();
    }

    public AtomicInteger getIterations() {
        return iters;
    }

    public float getMutation() {
        return ((float) mut.getValue()) / 100f;
    }
}
