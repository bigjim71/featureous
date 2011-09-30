/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.explorer;

import dk.sdu.mmmi.featureous.explorer.inspector.TraceInspectorTopComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

public final class ShowFeatureous implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        final FeatureousExplorer exp = FeatureousExplorer.getDefault();
        Mode mode = WindowManager.getDefault().findMode("explorer");
        if (mode != null) {
            mode.dockInto(exp);
        }
        exp.open();
        exp.requestActive();
        
        TraceInspectorTopComponent ti = TraceInspectorTopComponent.getDefault();
        mode = WindowManager.getDefault().findMode("navigator");
        if (mode != null) {
            mode.dockInto(ti);
        }
        ti.open();
        ti.requestActive();
    }
}
