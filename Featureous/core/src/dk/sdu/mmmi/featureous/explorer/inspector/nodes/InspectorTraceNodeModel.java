/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.explorer.inspector.nodes;

import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.ui.PackageUI;
import dk.sdu.mmmi.featureous.icons.IconUtils;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 * @author ao
 */
public class InspectorTraceNodeModel extends Children.Keys<PackageUI> {

    private final TraceModel tm;

    public InspectorTraceNodeModel(TraceModel tm) {
        this.tm = tm;
    }

    @Override
    protected void addNotify() {
        Set<String> pkgs = new HashSet<String>();
        for (ClassModel cm : tm.getClassSet()) {
            pkgs.add(cm.getPackageName());
        }
        ArrayList<String> p = new ArrayList<String>(pkgs);
        Collections.sort(p);
        ArrayList<PackageUI> pu = new ArrayList<PackageUI>();
        for(String pp : p){
            PackageUI pui = new PackageUI();
            pui.setName(pp);
            pu.add(pui);
        }
        setKeys(pu);
    }

    @Override
    protected Node[] createNodes(final PackageUI key) {
        return new Node[]{new AbstractNode(new InspectorPkgNodeModel(tm, key), Lookups.singleton(key)) {

            @Override
            public Image getIcon(int type) {
                for (ClassModel cm : tm.getClassSet()) {
                    if (key.equals(cm.getPackageName())) {
                        for (String m : cm.getAllMethods()) {
                            if (cm.isFep(m)) {
                                return ImageUtilities.icon2Image(
                                        IconUtils.loadOverlayedIcon( new PackageUI().getIconPath(),
                                        "dk/sdu/mmmi/featureous/icons/nb/running.png"));
                            }
                        }
                    }
                }

                return ImageUtilities.icon2Image(IconUtils.loadIcon(new PackageUI().getIconPath()));
            }

            @Override
            public Image getOpenedIcon(int type) {
                return getIcon(type);
            }

            @Override
            public String getHtmlDisplayName() {
                return key.getName();
            }
        }};
    }
}
