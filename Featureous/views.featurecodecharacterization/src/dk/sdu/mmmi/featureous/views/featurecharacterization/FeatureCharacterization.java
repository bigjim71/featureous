/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.views.featurecharacterization;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.TraceListChangeListener;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.model.TraceSet;
import dk.sdu.mmmi.featureous.explorer.api.AbstractTraceView;
import dk.sdu.mmmi.featureous.explorer.api.GranularityChangeListener;
import dk.sdu.mmmi.featureous.explorer.api.GranularityChooserMenu;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jfreechart.SVGExporter;

/**
 *
 * @author ao
 */
public class FeatureCharacterization extends JPanel implements TraceListChangeListener, GranularityChangeListener {

    private final GranularityChooserMenu gc = new GranularityChooserMenu(new int[]{
                GranularityChooserMenu.CLASS_GRANULARITY,
                GranularityChooserMenu.PACKAGE_GRANULARITY,}, GranularityChooserMenu.PACKAGE_GRANULARITY, this);
    private final ArrayList<TraceModel> l = new ArrayList<TraceModel>();
    private FeatureViewChart featureChart;
    private final AbstractTraceView p;

    public FeatureCharacterization(AbstractTraceView p) {
        super(new BorderLayout());
        this.p = p;
        for (String s : Controller.getInstance().getTraceSet().getSelectionManager().getSelectedFeats()) {
            l.add(Controller.getInstance().getTraceSet().getFirstLevelTraceByName(s));
        }

        if (l.isEmpty()) {
            l.addAll(Controller.getInstance().getTraceSet().getFirstLevelTraces());
        }
        
        Collections.sort(l, new Comparator<TraceModel>(){

            @Override
            public int compare(TraceModel o1, TraceModel o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        JPanel view = createView();
        gc.add(SVGExporter.createExportAction(featureChart.getChart(), view));

    }

    private JPanel createView() {
        removeAll();
        JPanel view = getFcv(l);
        view.addMouseListener(gc.getPopupListener());
        add(view, java.awt.BorderLayout.CENTER);
        return view;
    }

    public void dispose() {
        featureChart.dispose();
    }

    private JPanel getFcv(ArrayList<TraceModel> l) {
        Controller c = Controller.getInstance();
        featureChart = new FeatureViewChart(l,
                gc.getValue() == GranularityChooserMenu.PACKAGE_GRANULARITY);
        final JPanel ch = featureChart.getChart(false, false);
        return ch;
    }

    @Override
    public void traceListChanged(TraceSet tl) {
        createView();
    }

    @Override
    public void granularityChanged(int newGranularity) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createView();
                p.repaintView();
            }
        });
    }
}
