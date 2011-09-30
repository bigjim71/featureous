/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.explorer.inspector.nodes;

import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.ui.PackageUI;
import java.util.ArrayList;
import java.util.Collections;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * @author ao
 */
public class InspectorPkgNodeModel extends Children.Keys<ClassModel> {
    private final TraceModel tm;
    private final String pkg;

    public InspectorPkgNodeModel(TraceModel tm, PackageUI pkg) {
        this.tm = tm;
        this.pkg = pkg.getName();
    }

    @Override
    protected void addNotify() {
        ArrayList<ClassModel> m = new ArrayList<ClassModel>();
        for(ClassModel cm : tm.getClassSet()){
            if(pkg.equals(cm.getPackageName())){
                m.add(cm);
            }
        }
        Collections.sort(m);
        setKeys(m);
    }

    @Override
    protected Node[] createNodes(ClassModel key) {
        return new Node[]{new InspectorClassNodeView(key)};
    }

}
