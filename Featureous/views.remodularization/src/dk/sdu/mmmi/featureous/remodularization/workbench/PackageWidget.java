/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.workbench;

import dk.sdu.mmmi.featureous.core.affinity.Affinity;
import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Utilities;

/**
 *
 * @author ao
 */
public class PackageWidget extends AbstractContainerWidget {

    public PackageWidget(Scene scene, String name, boolean editable) {
        super(scene, name, editable);
        setAffinity(Color.lightGray);
    }

    public void setAffinity(Color col) {
        setBackground(col);
    }

    public void recolorBasedOnChildAffinity() {
        Affinity aff = getCurrentAffinityOrNull();
        if (aff!=null) {
            this.setAffinity(aff.color);
        } else {
            this.setAffinity(Color.lightGray);
        }
    }

    public Affinity getCurrentAffinityOrNull() {
        List<Affinity> childAffs = new ArrayList<Affinity>();
        for (Widget w : this.getChildren()) {
            if (w instanceof UMLClassWidget) {
                UMLClassWidget cl = (UMLClassWidget) w;
                if (cl.getAffinity() != null) {
                    childAffs.add(cl.getAffinity());
                }
            }
        }
        Collections.sort(childAffs);
        Collections.reverse(childAffs);
        if (childAffs.size() > 0) {
            return childAffs.get(0);
        } else {
            return null;
        }
    }

    @Override
    protected Image getIcon() {
        return Utilities.loadImage("dk/sdu/mmmi/featureous/remodularization/workbench/icons/package.png"); // NOI18N
    }
}
