/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.views.featurecodecharacterization;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.explorer.api.AbstractTraceView;
import dk.sdu.mmmi.featureous.explorer.api.GranularityChangeListener;
import dk.sdu.mmmi.featureous.explorer.api.GranularityChooserMenu;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jzy3d.chart.Chart;

/**
 *
 * @author ao
 */
public class Characterization3dPanel extends JPanel implements GranularityChangeListener {

    private Chart cc;
    private final GranularityChooserMenu gc = new GranularityChooserMenu(new int[]{
                GranularityChooserMenu.CLASS_GRANULARITY,
                GranularityChooserMenu.PACKAGE_GRANULARITY,}, GranularityChooserMenu.PACKAGE_GRANULARITY, this);
    private Set<String> selFeats;
    private Characterization3dChart c3c;
    private AbstractTraceView p;

    public Characterization3dPanel(AbstractTraceView v) {
        super(new BorderLayout());
        this.p = v;
        selFeats = new HashSet<String>();
        selFeats.addAll(Controller.getInstance().getTraceSet().getSelectionManager().getSelectedFeats());

        createView();
    }

    private void createView() {
        dispose();

        c3c = new Characterization3dChart();
        cc = c3c.createChart(selFeats, gc);
        ((Component) cc.getCanvas()).setMinimumSize(new Dimension(50, 50));
        add((Component) cc.getCanvas(), BorderLayout.CENTER);
        ((Component) cc.getCanvas()).addMouseListener(gc.getPopupListener());
        Controller.getInstance().getTraceSet().getSelectionManager().addSelectionListener(c3c);
    }

    public void dispose() {
        removeAll();
        Controller.getInstance().getTraceSet().getSelectionManager().removeSelectionListener(c3c);
        if (c3c != null) {
            c3c.dispose();
        }
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
