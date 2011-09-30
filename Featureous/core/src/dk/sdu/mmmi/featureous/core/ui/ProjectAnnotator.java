/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.ui;

import dk.sdu.mmmi.srcUtils.nb.NBJavaSrcUtils;
import java.awt.Image;
import java.io.File;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectIconAnnotator;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author andrzejolszak
 */
@ServiceProvider(service = ProjectIconAnnotator.class)
public class ProjectAnnotator implements ProjectIconAnnotator {

    private final ChangeSupport pcs = new ChangeSupport(this);

    public @Override
    Image annotateIcon(Project p, Image orig, boolean openedNode) {
        String srcDir = NBJavaSrcUtils.getSrcDirs(p)[0];
        File aopXml = new File(srcDir + System.getProperty("file.separator") + "META-INF"
                + System.getProperty("file.separator") + "aop.xml");
        if (aopXml.isFile()) {
            return ImageUtilities.mergeImages(ImageUtilities.addToolTipToImage(orig, "Featureous-enabled project"),
                    ImageUtilities.loadImage("dk/sdu/mmmi/featureous/core/ui/featureous_badge.png"), 16, 0);
        } else {
            return orig;
        }
    }

    public @Override
    void addChangeListener(ChangeListener listener) {
        pcs.addChangeListener(listener);
    }

    public @Override
    void removeChangeListener(ChangeListener listener) {
        pcs.removeChangeListener(listener);
    }

    void setEnabled(boolean enabled) {
        pcs.fireChange();
    }
}
