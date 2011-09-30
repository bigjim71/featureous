/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.explorer.nodes;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import java.util.ArrayList;
import java.util.Collections;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * @author ao
 */
public class ProjectNode extends Children.Keys<TraceModel>{

    @Override
    public void addNotify() {
        Controller ctrl = Controller.getInstance();
        ArrayList<TraceModel> l = new ArrayList<TraceModel>(ctrl.getTraceSet().getFirstLevelTraces());
        Collections.sort(l);
        this.setKeys(l);
    }

    @Override
    protected Node[] createNodes(TraceModel key) {
        if(!key.hasSubTraces()){
            return new Node[]{new TraceNode(key)};
        }else{
            return new Node[]{new TraceGroupNode(key)};
        }
    }
}
