/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.featurerelationscharacterization;

import dk.sdu.mmmi.featureous.explorer.api.AbstractTraceView;
import dk.sdu.mmmi.featureous.explorer.spi.FeatureTraceView;
import dk.sdu.mmmi.featureous.featurerelationscharacterization.graph.GraphGui;
import dk.sdu.mmmi.featureous.lib.prefuse_profusians.SVGExporter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author ao
 */
@ServiceProvider(service=FeatureTraceView.class)
public class FeatureRelChar extends AbstractTraceView{
    private GraphGui graph;

    public FeatureRelChar() {
        setupAttribs("Feature relations characterization", "Feature relations characterization", "opensourceicons/png/blue/connected.png");
    }

    @Override
    public void createView() {
        graph = new GraphGui();
        add(graph.getPanel());
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S){
                    SVGExporter.createExportAction(graph.getView()).actionPerformed(null);
                }
            }
        });
        revalidate();
    }

    @Override
    public void closeView() {
        if(graph!=null){
            graph.dispose();
        }
        removeAll();
        revalidate();
    }

    @Override
    public TopComponent createInstance() {
        return new FeatureRelChar();
    }

    @Override
    public String getGuiMode() {
        return "properties";
    }
}
