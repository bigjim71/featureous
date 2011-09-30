/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.views.codecharacterization;

import dk.sdu.mmmi.featureous.core.model.TraceListChangeListener;
import dk.sdu.mmmi.featureous.core.model.TraceSet;
import dk.sdu.mmmi.featureous.explorer.api.AbstractTraceView;
import dk.sdu.mmmi.featureous.explorer.api.GranularityChangeListener;
import dk.sdu.mmmi.featureous.explorer.api.GranularityChooserMenu;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jfreechart.SVGExporter;

/**
 *
 * @author ao
 */
public class CodeCharacterization extends JPanel implements TraceListChangeListener, GranularityChangeListener {

    private TanglingViewChart tvc;
    private final GranularityChooserMenu gc = new GranularityChooserMenu(new int[]{
                GranularityChooserMenu.CLASS_GRANULARITY,
                GranularityChooserMenu.PACKAGE_GRANULARITY,}, GranularityChooserMenu.PACKAGE_GRANULARITY, this);
    private final AbstractTraceView p;

    public CodeCharacterization(AbstractTraceView p) {
        super(new BorderLayout());
        this.p = p;

        JPanel view = createView();
        
        gc.add(SVGExporter.createExportAction(tvc.getJchart(), view));
    }

    private JPanel createView() {
        removeAll();

        JPanel view = getClassCharacterization();
        view.addMouseListener(gc.getPopupListener());
        add(view, java.awt.BorderLayout.CENTER);
        return view;
    }

    public void dispose() {
        tvc.dispose();
    }

    private JPanel getClassCharacterization() {
        tvc = new TanglingViewChart(
                gc.getValue() == GranularityChooserMenu.PACKAGE_GRANULARITY, true);
        JPanel ch = tvc.getChart();
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
