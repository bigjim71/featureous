/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.metricevolution;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.ui.OutputUtil;
import dk.sdu.mmmi.featureous.explorer.api.AbstractTraceView;
import dk.sdu.mmmi.featureous.explorer.spi.FeatureTraceView;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.ClassCountMetric;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.FeatureCountMetric;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.MeanClassCountMetric;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.MeanFeatCountMetric;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.MeanFeatPerClassCountMetric;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.MeanPkgCountMetric;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.PkgCountMetric;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.StaticScattering;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.StaticTangling;
import dk.sdu.mmmi.featureous.metrics.oometrics.PkgCohesionMetric;
import dk.sdu.mmmi.featureous.metrics.oometrics.PkgCouplingMetric;
import dk.sdu.mmmi.srcUtils.sdm.RecoderModelExtractor;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JToolBar;
import org.mediavirus.parvis.api.ParallelPanel;
import org.mediavirus.parvis.model.SimpleParallelSpaceModel;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author aolszak
 */
@ServiceProvider(service = FeatureTraceView.class)
public class MetricEvolutionView extends AbstractTraceView {

    private int releaseNr = 0;
    private final Map<String, Set<AbstractMetric>> releaseToResults = new HashMap<String, Set<AbstractMetric>>();
    private final List<String> releases = new ArrayList<String>();
    private ParallelPanel mp;

    public MetricEvolutionView() {
        setupAttribs("Metric evolution view", "Metric evolution view", "opensourceicons/png/blue/arrowround.png");
    }

    @Override
    public TopComponent createInstance() {
        return new MetricEvolutionView();
    }

    @Override
    public void createView() {
        JToolBar toolbar = new JToolBar();
        JButton addData = new JButton("Measure current state");
        addData.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                addData();
            }
        });
        toolbar.add(addData);

        JButton remLast = new JButton("Remove last measurement");
        remLast.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                removeLastMeasurement();
            }
        });
        toolbar.add(remLast);

        JButton csv = new JButton("Print data as CSV");
        csv.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                printAsCsv();
            }
        });
        toolbar.add(csv);

        mp = new ParallelPanel();
        add(mp, BorderLayout.CENTER);
        add(toolbar, BorderLayout.NORTH);
        updatePanel();
        revalidate();
    }

    private void addData() {
        releaseNr++;
        RecoderModelExtractor.extractSdmAndRunAsync(new RecoderModelExtractor.RunnableWithSdm() {

            @Override
            public void run(StaticDependencyModel sdm) {

                final Set<AbstractMetric> results = new HashSet<AbstractMetric>();
                AbstractMetric r = new AbstractMetric(" Measurement no.", AbstractMetric.Scope.SYSTEM) {

                    @Override
                    public float getResult() {
                        return getMeanVal();
                    }

                    @Override
                    public void calculateAll(Set<TraceModel> tms, StaticDependencyModel sdm) {
                        setResultForSubject((float) releaseNr, "system");
                    }
                };
                r.calculateAll(null, null);
                results.add(r);

                final Set<TraceModel> ftms = Controller.getInstance().getTraceSet().getFirstLevelTraces();

                r = new FeatureCountMetric();
                r.calculateAll(ftms, null);
                results.add(r);
                r = new MeanPkgCountMetric();
                r.calculateAll(ftms, null);
                results.add(r);
                r = new MeanClassCountMetric();
                r.calculateAll(ftms, null);
                results.add(r);
                r = new StaticScattering(true);
                r.calculateAll(ftms, null);
                results.add(r);
                r = new StaticTangling(true);
                r.calculateAll(ftms, null);
                results.add(r);
                r = new StaticScattering(false);
                r.calculateAll(ftms, null);
                results.add(r);
                r = new StaticTangling(false);
                r.calculateAll(ftms, null);
                results.add(r);

                if (sdm != null) {
                    OutputUtil.log("Static dependency model extracted [pkgs, types]: " + sdm.getPackages().size() + ", " + sdm.getAllTypesCount());

                    AbstractMetric r2 = new PkgCouplingMetric();
                    r2.calculateAll(ftms, sdm);
                    results.add(r2);
                    r2 = new PkgCohesionMetric();
                    r2.calculateAll(ftms, sdm);
                    results.add(r2);
                    r2 = new MeanFeatCountMetric();
                    r2.calculateAll(ftms, sdm);
                    results.add(r2);
                    r2 = new PkgCountMetric();
                    r2.calculateAll(ftms, sdm);
                    results.add(r2);
                    r2 = new ClassCountMetric();
                    r2.calculateAll(ftms, sdm);
                    results.add(r2);
                    r2 = new MeanFeatPerClassCountMetric();
                    r2.calculateAll(ftms, sdm);
                    results.add(r2);
                } else {
                    OutputUtil.log("Static dependency model could not be extracted");
                }

                String relId = "Measurement " + ((releaseNr < 10) ? "0" : "") + releaseNr;
                releaseToResults.put(relId, results);
                releases.add(relId);

                updatePanel();
            }
        });
    }

    private void printAsCsv() {
        StringBuilder csv = new StringBuilder("//--- begin CSV ---\n");
        csv.append("release;");
        List<String> metrics = getPresentMetrics(releaseToResults);
        for (String m : metrics) {
            csv.append(m + ";");
        }
        csv.append("\n");
        for (String rel : releases) {
            csv.append(rel + ";");
            for (String met : metrics) {
                for (AbstractMetric ress : releaseToResults.get(rel)) {
                    if (ress.getName().equals(met)) {
                        csv.append(ress.getResult() + ";");
                        break;
                    }
                }
            }
            csv.append("\n");
        }

        csv.append("//--- end CSV ---");

        OutputUtil.log(csv.toString());
    }

    private void removeLastMeasurement() {
        String lastId = releases.remove(releases.size() - 1);
        releases.remove(lastId);
        releaseToResults.remove(lastId);
        releaseNr--;

        updatePanel();
    }

    private void updatePanel() {
        List<String> metrics = getPresentMetrics(releaseToResults);
        if (metrics.size() < 2) {
            return;
        }

        Collections.sort(metrics);

        SimpleParallelSpaceModel spsm = new SimpleParallelSpaceModel();
        spsm.initNumDimensions(metrics.size());
        spsm.setAxisLabels(metrics.toArray(new String[]{}));

        for (String rel : releaseToResults.keySet()) {
            float[] ress = new float[metrics.size()];
            for (int i = 0; i < metrics.size(); i++) {
                Float res = null;
                for (AbstractMetric mr : releaseToResults.get(rel)) {
                    if (mr.getName().equals(metrics.get(i))) {
                        res = mr.getResult();
                        break;
                    }
                }
                if (res == null) {
                    res = 0f;
                }

                ress[i] = res;
            }
            spsm.addRecord(ress, rel);
        }

        mp.getParallelDisplay().setModel(spsm);
    }

    @Override
    public void closeView() {
    }

    private List<String> getPresentMetrics(Map<String, Set<AbstractMetric>> res) {
        Set<String> metrics = new HashSet<String>();
        for (Set<AbstractMetric> r : res.values()) {
            for (AbstractMetric mr : r) {
                metrics.add(mr.getName());
            }
        }
        return new ArrayList<String>(metrics);
    }
}
