/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.views.featurecodecharacterization;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.SelectionChangeListener;
import dk.sdu.mmmi.featureous.core.model.SelectionManager;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.model.TraceSet;
import dk.sdu.mmmi.featureous.explorer.api.GranularityChooserMenu;
import dk.sdu.mmmi.featureous.metrics.Result;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.Scattering;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.Tangling;
import dk.sdu.mmmi.featureous.views.featurecodecharacterization.barchart3d.customized.PNGKeyboardSaver;
import dk.sdu.mmmi.featureous.views.featurecodecharacterization.barchart3d.customized.LabeledMouseSelector;
import dk.sdu.mmmi.featureous.views.featurecodecharacterization.barchart3d.customized.CustomMouseControl;
import dk.sdu.mmmi.featureous.views.featurecodecharacterization.barchart3d.customized.CustomKeyboardControl;
import dk.sdu.mmmi.featureous.views.featurecodecharacterization.barchart3d.BarChartBar;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.jzy3d.chart.Chart;
import org.jzy3d.colors.Color;
import org.jzy3d.global.Settings;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.DefaultDecimalTickRenderer;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.scene.Scene;
import org.jzy3d.plot3d.rendering.view.Renderer2d;
import org.jzy3d.plot3d.rendering.view.modes.ViewPositionMode;

public class Characterization3dChart implements SelectionChangeListener {

    private final String TITLE = "Feature complexity view";
    private LabeledMouseSelector mouseSelection;
    private CustomMouseControl mouseCamera;
    private List<Result> sr;
    private List<Result> tr;
    private List<Result> srAdded = new LinkedList<Result>();

    public Chart createChart(Set<String> selTraces, GranularityChooserMenu gc) {
        Settings.getInstance().setHardwareAccelerated(true);
        chart = new Chart(Quality.Nicest) {

            @Override
            public void dispose() {
                mouseCamera.removeSlaveThreadController();
//                super.dispose();
            }
        };
//        chart.setAxeDisplayed(true);
//         chart.getView().setAxeBoxDisplayed(false);
//        setupTitle();
        setupAxes();
        setupMouseNavigation();
        setupKeyboardNavigation();
        setupKeyboardSave();
        setupMouseSelection();
//        setupLegend();

        chart.getView().setMaximized(true);
        chart.getView().getCamera().setStretchToFill(true);

        TraceSet ts = Controller.getInstance().getTraceSet();


        boolean pkgGran = gc.getValue() == GranularityChooserMenu.PACKAGE_GRANULARITY;
        sr = new ArrayList<Result>(new Scattering(pkgGran).calculateAndReturnAll(ts.getFirstLevelTraces(), null));
        Result.sortByName(sr);
        tr = new ArrayList<Result>(new Tangling(pkgGran).calculateAndReturnAll(ts.getFirstLevelTraces(), null));
        Result.sortByName(tr);

        Collections.sort(sr);
        Collections.sort(tr);

        if (sr.size() == 0) {
            return chart;
        }

        if (gc.getValue() == GranularityChooserMenu.PACKAGE_GRANULARITY) {
            Set<String> pgs = new HashSet<String>();
            for (TraceModel tm : ts.getFirstLevelTraces()) {
                for (ClassModel cm : tm.getClassSet()) {
                    pgs.add(cm.getPackageName());
                }
            }

            BarChartBar.BAR_RADIUS = (float) (sr.get(0).value / pgs.size());
        } else {
            BarChartBar.BAR_RADIUS = (float) (sr.get(0).value / ts.getFirstLevelTraceByName(sr.get(0).name).getClassSet().size());
        }

        BarChartBar.BAR_FEAT_BUFFER_RADIUS = BarChartBar.BAR_RADIUS / 2f;

        Scene scene = chart.getScene();

        int addedFeats = 0;
        srAdded.clear();

        if (gc.getValue() == GranularityChooserMenu.PACKAGE_GRANULARITY) {
            for (int f = 0; f < sr.size(); f++) {
                String fName = sr.get(f).name;
                int addedCus = 0;
                if (selTraces.isEmpty() || selTraces.contains(fName)) {
                    for (int cu = 0; cu < tr.size(); cu++) {
                        String cuName = tr.get(cu).name;

                        Set<String> pkgs = new HashSet<String>();
                        for (ClassModel cm : ts.getFirstLevelTraceByName(fName).getClassSet()) {
                            pkgs.add(cm.getPackageName());
                        }

                        if (pkgs.contains(cuName)) {

                            java.awt.Color c = Controller.getInstance().getAffinity().getPkgAffinity(cuName).color;
                            Color cc = new Color(c.getRed(), c.getGreen(), c.getBlue());

                            scene.add(addBar(addedCus, cuName, addedFeats, fName, tr.get(cu).value, cc));

                            addedCus++;
                        }
                    }
                    srAdded.add(sr.get(f));
                    addedFeats++;
                }
            }
        } else {
            for (int f = 0; f < sr.size(); f++) {
                String fName = sr.get(f).name;
                int addedCus = 0;
                if (selTraces.isEmpty() || selTraces.contains(fName)) {
                    for (int cu = 0; cu < tr.size(); cu++) {
                        String cuName = tr.get(cu).name;
                        if (ts.getFirstLevelTraceByName(fName).hasClass(cuName)) {
                            ClassModel cm = ts.getFirstLevelTraceByName(fName).getClass(cuName);
                            java.awt.Color c = Controller.getInstance().getAffinity().getClassAffinity(cm.getName()).color;
                            Color cc = new Color(c.getRed(), c.getGreen(), c.getBlue());

                            scene.add(addBar(addedCus, cuName, addedFeats, fName, tr.get(cu).value, cc));

                            addedCus++;
                        }
                    }
                    srAdded.add(sr.get(f));
                    addedFeats++;
                }
            }
        }
        return chart;
    }

    private void setupMouseSelection() {
        mouseSelection = new LabeledMouseSelector(chart);
        chart.getCanvas().addKeyListener(mouseSelection);
    }

    private void setupTitle() {
        Renderer2d messageRenderer = new Renderer2d() {

            public void paint(Graphics g) {
                g.setColor(java.awt.Color.BLUE);
                g.setFont(g.getFont().deriveFont(Font.BOLD, 16));
                g.drawString(TITLE,
                        (int) (15 + 0.05d * chart.getCanvas().getRendererWidth()),
                        (int) (15 + 0.05d * chart.getCanvas().getRendererHeight()));
            }
        };
        chart.addRenderer(messageRenderer);
    }

    private void setupMouseNavigation() {
        mouseCamera = new CustomMouseControl(chart);
        mouseCamera.install();
    }

    private int getFeatureIndex(float figYCenter) {
        return (int) ((figYCenter) / (2 * (BarChartBar.BAR_FEAT_BUFFER_RADIUS + BarChartBar.BAR_RADIUS)));
    }

    private void setupAxes() {
        chart.getAxeLayout().setXAxeLabel("Scattering");
        chart.getAxeLayout().setXTickRenderer(new DefaultDecimalTickRenderer(2));

        chart.getAxeLayout().setYAxeLabel("");
        chart.getAxeLayout().setYTickRenderer(new ITickRenderer() {

            public String format(float value) {
                int idx = getFeatureIndex(value);
                if (value >= 0 && getFeatureIndex(value) >= 0 && idx < srAdded.size()) {
                    return srAdded.get(idx).name;
                } else {
                    return "";
                }
            }
        });
        chart.getAxeLayout().setYTickProvider(new DiscreteTickProvider());
        chart.getAxeLayout().setZAxeLabel("Tangling");
        chart.getAxeLayout().setZTickRenderer(new DefaultDecimalTickRenderer(2));
//        chart.getAxeLayout().setZTickRenderer( new ScientificNotationTickRenderer(2) );
//        float[] ticks = {0f, 0.5f, 1f};
//            chart.getAxeLayout().setZTickProvider(new StaticTickProvider(ticks));

        chart.getView().setViewPositionMode(ViewPositionMode.FREE);
//        chart.getView().setAxeSquared(false);
    }

    public AbstractDrawable addBar(int compUnit, String compUnitName, int feature, String featureName, double height, Color color) {
        // compUnit, feature numbered form 0!
        color.a = 1f;

        BarChartBar bar = new BarChartBar(chart, featureName, compUnitName);

        bar.setData(compUnit, feature, (float) height, color);
//        if (!a) {
//            bar.setColorMapper(new ColorMapper(new AffinityColorGen(), 0f, 2.0f));
//            bar.setLegend(new ColorbarLegend(bar, chart.getAxeLayout()));
//            bar.setLegendDisplayed(true);
//            a = true;
//        }

        return bar;
    }

    public Chart getChart() {
        return chart;
    }
    private Chart chart;

    private void setupLegend() {
        chart.addRenderer(new CustomLegendRenderer(chart.getCanvas()));
    }

    private void setupKeyboardNavigation() {
        chart.getCanvas().addKeyListener(new CustomKeyboardControl(chart));
    }

    private void setupKeyboardSave() {
        chart.getCanvas().addKeyListener(new PNGKeyboardSaver(chart));
    }

    @Override
    public void featureSelectionChanged(SelectionManager tl) {
    }

    public void dispose() {
        if (chart != null) {
            chart.getCanvas().dispose();
            chart.dispose();
        }
        chart = null;
    }

    @Override
    public void compUnitSelectionChanged(SelectionManager tl) {
        if (chart != null) {
            chart.render();
        }
    }
}
