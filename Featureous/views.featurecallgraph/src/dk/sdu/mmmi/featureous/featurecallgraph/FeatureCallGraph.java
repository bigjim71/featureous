/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.featurecallgraph;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.explorer.api.AbstractTraceView;
import dk.sdu.mmmi.featureous.explorer.api.FeatureSelectionWaiter;
import dk.sdu.mmmi.featureous.explorer.spi.FeatureTraceView;
import dk.sdu.mmmi.featureous.lib.prefuse_profusians.SVGExporter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author ao
 */
@ServiceProvider(service = FeatureTraceView.class)
public class FeatureCallGraph extends AbstractTraceView {

    private GraphGui graph;
    private FeatureSelectionWaiter fsw = new FeatureSelectionWaiter();

    public FeatureCallGraph() {
        setupAttribs("Feature call graph", "Feature call graph", "opensourceicons/png/blue/tree.png");
    }

    @Override
    public void createView() {
        fsw.init(this, new Runnable() {

            @Override
            public void run() {
                constructView();
            }
        });
    }

    private void constructView() {
        Set<String> sfs = Controller.getInstance().getTraceSet().getSelectionManager().getSelectedFeats();
        graph = new GraphGui(sfs.iterator().next());
        add(graph.getPanel());
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S){
                    SVGExporter.createExportAction(graph.getView()).actionPerformed(null);
                }
            }
        });
    }

    @Override
    public void closeView() {
        fsw.dispose();
        if(graph!=null){
            graph.dispose();
        }
        removeAll();
        revalidate();
    }

    @Override
    public TopComponent createInstance() {
        return new FeatureCallGraph();
    }

    @Override
    public String getGuiMode() {
        return "output";
    }
}
