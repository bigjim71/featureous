/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.explorer.nodes;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author ao
 */
public class TraceGroupNodeModel extends Children.Keys<TraceModel>{
    private final Set<TraceModel> tms;

    public TraceGroupNodeModel(Set<TraceModel> tms) {
        this.tms = tms;
    }

    @Override
    protected void addNotify() {
        ArrayList<TraceModel> l = new ArrayList<TraceModel>(tms);
        Collections.sort(l);
        setKeys(l);
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
