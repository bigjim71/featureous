/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.featurecodecorrelation.graph;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.explorer.api.FeatureSelectionWaiter;
import dk.sdu.mmmi.featureous.explorer.api.AbstractTraceView;
import dk.sdu.mmmi.featureous.explorer.api.GranularityChangeListener;
import dk.sdu.mmmi.featureous.explorer.api.GranularityChooserMenu;
import dk.sdu.mmmi.featureous.explorer.spi.FeatureTraceView;
import dk.sdu.mmmi.featureous.lib.prefuse_profusians.SVGExporter;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author ao
 */
@ServiceProvider(service = FeatureTraceView.class)
public class FeatureCodeCorrelationGraph extends AbstractTraceView implements GranularityChangeListener {

    private FeatureSelectionWaiter fsw = new FeatureSelectionWaiter();
    private GraphGui gg;
    private final GranularityChooserMenu gc = new GranularityChooserMenu(new int[]{
                GranularityChooserMenu.CLASS_GRANULARITY, GranularityChooserMenu.PACKAGE_GRANULARITY}, GranularityChooserMenu.PACKAGE_GRANULARITY, this);
    private Set<String> selFeats;
    private JPanel content;

    public FeatureCodeCorrelationGraph() {
        setupAttribs("Feature-code correlation graph", "Feature-code correlation graph", "opensourceicons/png/blue/connections.png");
    }

    @Override
    public void createView() {
        content = new JPanel(new BorderLayout());
        add(content, BorderLayout.CENTER);

        fsw.init(this, new Runnable() {

            @Override
            public void run() {
                constructView();
                repaintView();
            }
        });
    }

    private void constructView() {
        content.removeAll();

        if (gg != null) {
            gg.dispose();
        }

        if (selFeats == null) {
            selFeats = new HashSet<String>();
            selFeats.addAll(Controller.getInstance().getTraceSet().getSelectionManager().getSelectedFeats());
        }

        gg = new GraphGui(selFeats, gc);

        content.add(gg.getPanel(), BorderLayout.CENTER);

        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S) {
                    SVGExporter.createExportAction(gg.getView()).actionPerformed(null);
                }
            }
        });
    }

    @Override
    public void closeView() {
        fsw.dispose();
        if (gg != null) {
            gg.dispose();
        }
        removeAll();
        revalidate();
    }

    @Override
    public TopComponent createInstance() {
        return new FeatureCodeCorrelationGraph();
    }

    @Override
    public void granularityChanged(int newGranularity) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                if (gg != null) {
                    constructView();
                    repaintView();
                }
            }
        });
    }
}
