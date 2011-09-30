/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.tracedrunner;

import dk.sdu.mmmi.featureous.core.ui.OutputUtil;
import dk.sdu.mmmi.featureous.explorer.FeatureousExplorer;
import java.io.File;
import javax.swing.SwingUtilities;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ao
 */
@ServiceProvider(service = AntLogger.class)
public class ExplorerUpdater extends AntLogger {

    @Override
    public boolean interestedInAllScripts(AntSession session) {
        return true;
    }

    @Override
    public boolean interestedInSession(AntSession session) {
        return true;
    }

    @Override
    public String[] interestedInTargets(AntSession session) {
        return new String[]{ActionProvider.COMMAND_RUN, ActionProvider.COMMAND_TEST};
    }

    @Override
    public void targetFinished(AntEvent event) {
        File buildImplXml = event.getScriptLocation();
        FileObject buildImpleXmlFo = FileUtil.toFileObject(buildImplXml);
//        Project proj = FileOwnerQuery.getOwner(buildImpleXmlFo);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (FeatureousExplorer.getDefault().isOpened()) {
                    FeatureousExplorer.getDefault().update();
                }
            }
        });
        OutputUtil.log("Updated traces in explorer");
    }
}
