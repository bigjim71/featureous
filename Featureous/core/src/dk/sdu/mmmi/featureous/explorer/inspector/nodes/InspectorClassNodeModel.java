/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.explorer.inspector.nodes;

import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.ui.MethodUI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * @author ao
 */
class InspectorClassNodeModel extends Children.Keys<MethodUI>{
    private final ClassModel cm;

    public InspectorClassNodeModel(ClassModel cm) {
        this.cm = cm;
    }

    @Override
    protected void addNotify() {
        List<MethodUI> ms = new ArrayList<MethodUI>();
        for(String m : cm.getAllMethods()){
            MethodUI mu = new MethodUI();
            mu.setName(m);
            ms.add(mu);
        }
        Collections.sort(ms, new Comparator<MethodUI>() {
            @Override
            public int compare(MethodUI o1, MethodUI o2) {
                return getMName(o1.getName()).compareTo(getMName(o2.getName()));
            }

            private String getMName(String m){
                int dotIdx = m.split("\\(")[0].lastIndexOf(".");
                return m.substring(dotIdx+1);
            }
        });
        setKeys(ms);
    }

    @Override
    protected Node[] createNodes(MethodUI key) {
        return new Node[]{new InspectorMethodNode(key, cm.getName(), cm.getPackageName(), cm.isFep(key.getName()))};
    }
}
