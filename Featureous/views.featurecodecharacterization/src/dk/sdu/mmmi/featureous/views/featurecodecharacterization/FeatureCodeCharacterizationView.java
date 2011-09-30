/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.views.featurecodecharacterization;

import dk.sdu.mmmi.featureous.explorer.api.AbstractTraceView;
import dk.sdu.mmmi.featureous.explorer.spi.FeatureTraceView;
import dk.sdu.mmmi.featureous.views.codecharacterization.CodeCharacterization;
import dk.sdu.mmmi.featureous.views.featurecharacterization.FeatureCharacterization;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author ao
 */
@ServiceProvider(service = FeatureTraceView.class)
public class FeatureCodeCharacterizationView extends AbstractTraceView {

    private static int instanceCount = 0;
    private Characterization3dPanel p3d;
    private FeatureCharacterization fc;
    private CodeCharacterization cc;

    public FeatureCodeCharacterizationView() {
        setupAttribs("Feature-code characterization view",
                "Feature-code characterization view",
                "opensourceicons/png/blue/barchart.png");
    }

    @Override
    public void createView() {
        // This is a hack, see: http://netbeans.org/bugzilla/show_bug.cgi?id=127935
        java.beans.Beans.setDesignTime(false);
        instanceCount++;
        
        this.setLayout(new BorderLayout());

        JTabbedPane tp = new JTabbedPane();
        
        try {
            p3d = new Characterization3dPanel(this);
            tp.addTab("Feature-code 3d characterization", p3d);
        } catch (Throwable ex) {
            tp.addTab("Feature-code 3d characterization [disabled]", new JLabel("Error finding JOGL native libraries for your system. 3D view disabled.\n" + ex.getMessage()));
        }

        fc = new FeatureCharacterization(this);
        tp.addTab("Feature characterization", fc);

        cc = new CodeCharacterization(this);
        tp.addTab("Code characterization", cc);

        this.add(tp, BorderLayout.CENTER);

//        Controller.getInstance().getTraceSet().addChangeListener(fc);
//        Controller.getInstance().getTraceSet().addChangeListener(cc);
    }

    @Override
    public void closeView() {
//        Controller.getInstance().getTraceSet().removeChangeListener(fc);
//        Controller.getInstance().getTraceSet().removeChangeListener(cc);
        this.removeAll();
        fc.dispose();
        cc.dispose();
        if (p3d != null) {
            p3d.dispose();
            p3d = null;
        }
        instanceCount--;
        if(instanceCount<=0){
            java.beans.Beans.setDesignTime(true);
        }
    }

    @Override
    public TopComponent createInstance() {
        return new FeatureCodeCharacterizationView();
    }

    @Override
    public String getGuiMode() {
        return "navigator";
    }
}
