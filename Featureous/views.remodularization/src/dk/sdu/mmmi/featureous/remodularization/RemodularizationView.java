/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization;

import dk.sdu.mmmi.featureous.explorer.api.AbstractTraceView;
import dk.sdu.mmmi.featureous.explorer.spi.FeatureTraceView;
import dk.sdu.mmmi.srcUtils.nb.NBJavaSrcUtils;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTabbedPane;
import org.netbeans.api.project.Project;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author ao
 */
@ServiceProvider(service = FeatureTraceView.class)
public class RemodularizationView extends AbstractTraceView {

    private PreView preTab;
    private PostView postTab;

    public RemodularizationView() {
        setupAttribs("Remodularization workbench", "Remodularization workbench", "opensourceicons/png/orangeyellow/processing.png");
    }

    @Override
    public TopComponent createInstance() {
        return new RemodularizationView();
    }

    @Override
    public void createView() {
        final Project proj = NBJavaSrcUtils.getMainProject();

        final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
        this.add(tabbedPane, BorderLayout.CENTER);

        preTab = new PreView(proj, tabbedPane);

        if (proj != null) {
            preTab.getComputeRemod().addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    final String srcDir = NBJavaSrcUtils.getSrcDirs(proj)[0];
                    final String backupDir = proj.getProjectDirectory().getPath() + System.getProperty("file.separator") + "src_backup";
                    postTab = new PostView(preTab.getFactorSingle().isSelected(), proj,
                            srcDir, backupDir, preTab.getOrgSdm(), preTab.getSelectedProviders(),
                            tabbedPane, preTab.getIterations(), preTab.getPopulation(), preTab.getMutation());
                }
            });
        }
    }

    @Override
    public void closeView() {
    }
}
