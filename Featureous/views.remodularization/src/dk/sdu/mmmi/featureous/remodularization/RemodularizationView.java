/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization;

import dk.sdu.mmmi.featureous.explorer.api.AbstractTraceView;
import dk.sdu.mmmi.featureous.explorer.spi.FeatureTraceView;
import dk.sdu.mmmi.featureous.metrics.MetricAggregator;
import dk.sdu.mmmi.srcUtils.nb.NBJavaSrcUtils;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private static MetricAggregator metricAggregator = new MetricAggregator();
    private static MetricAggregator remodularizationMetricAggregator = new MetricAggregator();

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
                            tabbedPane, preTab.getIterations(), preTab.getPopulation(), preTab.getMutation(),
                            metricAggregator, remodularizationMetricAggregator);
                }
            });
        }
    }

    @Override
    public void closeView() {
        List<String> verIDs = remodularizationMetricAggregator.getVersionIDs();
        Collections.sort(verIDs);
        
        List<String> mIDs = remodularizationMetricAggregator.getMetricIDs();
        
        String mpf = null;
        String mfp = null;
        String loc = null;
        for(String m : mIDs){
            if(m.contains("res/pack")){
                mpf = m;
            }
            if(m.contains("ges/feat")){
                mfp = m;
            }
            if(m.contains("LOC")){
                loc = m;
            }
        }
//        
        List<Map<String, Double>> vals = new ArrayList<Map<String, Double>>();
        for(String s : verIDs){
            vals.add(remodularizationMetricAggregator.getMetricSnapshot(s, mpf));
        }
        remodularizationMetricAggregator.printJoined(verIDs, vals);
        
        vals = new ArrayList<Map<String, Double>>();
        for(String s : verIDs){
            vals.add(remodularizationMetricAggregator.getMetricSnapshot(s, mfp));
        }
        remodularizationMetricAggregator.printJoined(verIDs, vals);

        vals = new ArrayList<Map<String, Double>>();
        for(String s : verIDs){
            vals.add(remodularizationMetricAggregator.getMetricSnapshot(s, loc));
        }
        remodularizationMetricAggregator.printJoined(verIDs, vals);
        
        vals = new ArrayList<Map<String, Double>>();
        for(String s : verIDs){
            vals.add(remodularizationMetricAggregator.getMetricsForSystem(s));
        }
        
        remodularizationMetricAggregator.printJoined(verIDs, vals);
    }
}
