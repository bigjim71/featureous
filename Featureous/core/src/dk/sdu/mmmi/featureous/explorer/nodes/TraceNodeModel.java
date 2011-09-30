/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.explorer.nodes;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Map;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author ao
 */
public class TraceNodeModel extends Children.Keys<Map.Entry<String, TraceModel>>{
    private final java.util.Map<String, TraceModel> tms;
    private final String activeProj;

    public TraceNodeModel(java.util.Map<String, TraceModel> tms, String activeProj) {
        this.tms = tms;
        this.activeProj = activeProj;
    }

    @Override
    protected void addNotify() {
        ArrayList<java.util.Map.Entry<String, TraceModel>> l =
                new ArrayList<java.util.Map.Entry<String, TraceModel>>(tms.entrySet());
        Collections.sort(l, new Comparator<java.util.Map.Entry<String, TraceModel>>(){
            @Override
            public int compare(java.util.Map.Entry<String, TraceModel> o1, java.util.Map.Entry<String, TraceModel> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        setKeys(l);
    }

    protected Node[] createNodes(java.util.Map.Entry<String, TraceModel> key) {
        boolean active = key.equals(activeProj);
        return new Node[]{new TraceVersionNode(key, active)};
    }

}
