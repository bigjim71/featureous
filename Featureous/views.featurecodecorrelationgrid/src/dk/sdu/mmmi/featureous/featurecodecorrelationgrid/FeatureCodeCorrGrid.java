/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.featurecodecorrelationgrid;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.explorer.api.AbstractTraceView;
import dk.sdu.mmmi.featureous.explorer.api.GranularityChangeListener;
import dk.sdu.mmmi.featureous.explorer.api.GranularityChooserMenu;
import dk.sdu.mmmi.featureous.explorer.spi.FeatureTraceView;
import dk.sdu.mmmi.featureous.featurecodecorrelationgrid.table.TraceTableModel;
import dk.sdu.mmmi.featureous.featurecodecorrelationgrid.table.XYTable;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author ao
 */
@ServiceProvider(service = FeatureTraceView.class)
public class FeatureCodeCorrGrid extends AbstractTraceView implements GranularityChangeListener {

    private final GranularityChooserMenu gc = new GranularityChooserMenu(new int[]{
                GranularityChooserMenu.CLASS_GRANULARITY,
                GranularityChooserMenu.PACKAGE_GRANULARITY,}, GranularityChooserMenu.PACKAGE_GRANULARITY, this);
    private final HashSet<String> feats;

    public FeatureCodeCorrGrid() {
        setupAttribs("Feature-code correlation grid", "Feature-code correlation grid", "dk/sdu/mmmi/featureous/icons/grid.png");
        feats = new HashSet<String>();
        feats.addAll(Controller.getInstance().getTraceSet().getSelectionManager().getSelectedFeats());
    }

    @Override
    public TopComponent createInstance() {
        return new FeatureCodeCorrGrid();
    }

    @Override
    public void createView() {
        removeAll();
        
        XYTable view = getFeatureInteractionGrid(feats, gc.getValue() == GranularityChooserMenu.PACKAGE_GRANULARITY);
        view.getTable().addMouseListener(gc.getPopupListener());
        add(view);
    }

    private XYTable getFeatureInteractionGrid(Set<String> selectedFeats, final boolean pkg) {
        final Map<String, Integer> newInstanceIds = new HashMap<String, Integer>();
        final Controller c = Controller.getInstance();
        if (selectedFeats.isEmpty()) {
            for (TraceModel tm : c.getTraceSet().getFirstLevelTraces()) {
                selectedFeats.add(tm.getName());
            }
        }
        TraceTableModel tm = new TraceTableModel(
                new HashSet<String>(selectedFeats),
                true,
                true, pkg) {

            @Override
            public Object getRelation(String cm, String fm) {
                TraceModel tmm = Controller.getInstance().getTraceSet().getFirstLevelTraceByName(fm);
                if ((!pkg && tmm.getClass(cm) == null)
                        || (pkg && getPackageString(tmm, cm).length()==0)) {
                    return "";
                }
                StringBuilder sb = null;
                if (pkg) {
                    sb = getPackageString(tmm, cm);
                } else {
                    sb = getClassString(tmm, cm);
                }
//                    sb = new StringBuilder("X");
                JLabel l = new JLabel(sb.substring(0, (sb.length() > 2) ? sb.length() - 2 : 0)) {

                    @Override
                    public String toString() {
                        return this.getText();
                    }
                };
                l.setHorizontalAlignment(JLabel.CENTER);
                l.setVerticalAlignment(JLabel.CENTER);
                if (pkg) {
                    setPkgAffinity(l, cm, tmm);
                } else {
                    setClassAffinity(tmm, cm, l);
                }
                l.setToolTipText("<html>" + l.getText() + "<br>" + fm + "<br>" + cm + "</html>");

                Font f = new Font(l.getFont().getFontName(), Font.PLAIN, 9);
                l.setFont(f);
                l.setOpaque(true);
                return l;
            }

            private void setClassAffinity(TraceModel tmm, String cm, JLabel l) {
                ClassModel cc = tmm.getClass(cm);
                l.setBackground(c.getAffinity().getClassAffinity(cc.getName()).color);
            }

            private StringBuilder getClassString(TraceModel tmm, String cm) {
                StringBuilder sb = new StringBuilder("");
                for (String ii : tmm.getClass(cm).getInstancesUsed()) {
                    if (!newInstanceIds.containsKey(ii)) {
                        Integer newVal = 0;
                        List<Integer> il = new ArrayList<Integer>();
                        il.addAll(newInstanceIds.values());
                        Collections.sort(il);
                        if (il.size() == 0) {
                            newVal = 1;
                        } else {
                            newVal = il.get(il.size() - 1) + 1;
                        }

                        newInstanceIds.put(ii, newVal);
                    }
                    String id = newInstanceIds.get(ii).toString();
                    if (tmm.hasCreated(ii)) {
                        sb.append("*");
                    }
                    sb.append(id + ", ");
                }
                return sb;
            }
        };

        return new XYTable(tm, 50);
    }

    private StringBuilder getPackageString(TraceModel fm, String p) {
        StringBuilder sb = new StringBuilder("");
        for (ClassModel cm : fm.getClassSet()) {
            if (cm.getPackageName().equals(p)) {
                sb.append("  ");
                break;
            }
        }
        return sb;
    }

    private void setPkgAffinity(JLabel l, String pkg, TraceModel fm) {
        Controller c = Controller.getInstance();
        boolean toDraw = false;
        for (ClassModel m : fm.getClassSet()) {
            if (m.getPackageName().equals(pkg)) {
                toDraw = true;
                break;
            }
        }
        if (toDraw) {
            l.setBackground(c.getAffinity().getPkgAffinity(pkg).color);
        } else {
            l.setBackground(Color.white);
        }
    }

    @Override
    public void closeView() {
        removeAll();
    }

    @Override
    public String getGuiMode() {
        return "properties";
    }

    @Override
    public void granularityChanged(int newGranularity) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createView();
                repaintView();
            }
        });
    }
}
