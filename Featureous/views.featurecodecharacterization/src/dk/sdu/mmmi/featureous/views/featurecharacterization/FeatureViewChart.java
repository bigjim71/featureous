/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.views.featurecharacterization;


import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.SelectionChangeListener;
import dk.sdu.mmmi.featureous.core.model.SelectionManager;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.ui.UIUtils;
import dk.sdu.mmmi.featureous.metrics.Result;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.StaticScattering;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class FeatureViewChart implements SelectionChangeListener{

    private ChartPanel panel;
    private DefaultCategoryDataset data;
    private final List<Result> scattering;
    private final List<TraceModel> ftms;
    private final CategoryPlot plot;
    private final boolean pkg;
    private final JFreeChart chart;

    public FeatureViewChart(List<TraceModel> ftms, final boolean pkg) {
        this.pkg = pkg;
        data = new DefaultCategoryDataset();
        chart = ChartFactory.createStackedBarChart("Feature characterization", "Feature", "Scattering", data, PlotOrientation.VERTICAL, true, false, false);
        plot = (CategoryPlot) chart.getPlot();
        CategoryAxis xAxis = (CategoryAxis) plot.getDomainAxis();
        xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        xAxis.setMaximumCategoryLabelLines(2);
//        chart.getLegend().setPosition(RectangleEdge.RIGHT);
//        chart.getLegend().setVerticalAlignment(VerticalAlignment.TOP);
//        chart.getLegend().setHorizontalAlignment(HorizontalAlignment.LEFT);
        LegendItemCollection lic = new LegendItemCollection();
//        lic.add(new LegendItem("Infrastructural unit", "", "", "", new Rectangle(10, 10), Color.GREEN));
//        lic.add(new LegendItem("Group-feature unit", "", "", "", new Rectangle(10, 10), Color.BLUE));
//        lic.add(new LegendItem("Single-feature unit", "", "", "", new Rectangle(10, 10), Color.RED));
        plot.setFixedLegendItems(lic);
//        chart.removeLegend();
        panel = new ChartPanel(chart);
        chart.setBackgroundPaint(Color.white);
        this.ftms = ftms;
        scattering = new ArrayList<Result>(new StaticScattering(pkg).calculateAndReturnAll(new HashSet<TraceModel>(ftms), null));
        Result.sortByName(scattering);
        for(Result r : scattering){
//            OutputUtil.log(r.name + ";" +r.value);
        }
        panel.getPopupMenu().setEnabled(false);//add(SVGExporter.createExportAction(chart, panel));
        StackedBarRenderer r2 = new StackedBarRenderer(){

            @Override
            public void drawItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset, int row, int column, int pass) {
                super.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, column, pass);
                double start = plot.getDomainAxis().getCategoryStart(column, getColumnCount(), dataArea, plot.getDomainAxisEdge());
                double end = plot.getDomainAxis().getCategoryEnd(column, getColumnCount(), dataArea, plot.getDomainAxisEdge());

                String compUnit = data.getRowKey(row).toString();

                // Calculate y coeffs
                double posBase = getBase();
                for(int i = 0; i<row; i++){
                    Number val = dataset.getValue(i, column);
                    if(val!=null){
                        posBase = posBase + val.doubleValue();
                    }
                }

                Number value = dataset.getValue(row, column);
                if(value==null){
                    return;
                }
                double val = value.doubleValue();

                double translatedBase = plot.getRangeAxis().valueToJava2D(posBase, dataArea, plot.getRangeAxisEdge());
                double translatedValue = plot.getRangeAxis().valueToJava2D(posBase + val, dataArea, plot.getRangeAxisEdge());

                if(Controller.getInstance().getTraceSet().getSelectionManager().getSelectedClasses().contains(compUnit)
                        ||Controller.getInstance().getTraceSet().getSelectionManager().getSelectedPkgs().contains(compUnit)){
                    g2.setPaint(UIUtils.SELECTION_COLOR);
                    g2.setStroke(new BasicStroke(3f));
                    Line2D l2d = new Line2D.Double(start, translatedBase, start, translatedValue);
                    g2.draw(l2d);
                    l2d = new Line2D.Double(end, translatedBase, end, translatedValue);
                    g2.draw(l2d);
                    l2d = new Line2D.Double(start, translatedBase, end, translatedBase);
                    g2.draw(l2d);
                    l2d = new Line2D.Double(start, translatedValue, end, translatedValue);
                    g2.draw(l2d);
                }
            }
        };
        plot.setRenderer(r2, true);
        StackedBarRenderer r = (StackedBarRenderer) plot.getRenderer();
        r.setDrawBarOutline(true);
        plot.getRenderer().setOutlineStroke(new BasicStroke(0.1f));
        Controller.getInstance().getTraceSet().getSelectionManager().addSelectionListener(this);
    }

    public JFreeChart getChart() {
        return chart;
    }

    public void dispose(){
        Controller.getInstance().getTraceSet().getSelectionManager().removeSelectionListener(this);
    }

    public JPanel getChart(boolean sortByName, boolean uniformSize){
        if(sortByName){
            Collections.sort(scattering, new Comparator<Result>() {

                public int compare(Result o1, Result o2) {
                    return o1.name.compareTo(o2.name);
                }
            });
        }else{
            // Sort by scattering
            Collections.sort(scattering, new Comparator<Result>() {

                public int compare(Result o1, Result o2) {
                    return o2.value.compareTo(o1.value);
                }
            });
        }

        // Enforce sorting
        for(Result r : scattering){

            int specific = 0;
            TraceModel tm = null;
            for(TraceModel t : ftms){
                if(t.getName().equals(r.name)){
                    tm = t;
                    break;
                }
            }

            Controller c = Controller.getInstance();

            data.addValue(0, "", r.name);
        }

        if(!pkg){
            doItForClass(uniformSize);
        }else{
            doItForPkg(uniformSize);
        }
        plot.getRenderer().setToolTipGenerator(new CategoryToolTipGenerator() {
            public String generateToolTip(CategoryDataset cd, int i, int i1) {
                return cd.getRowKey(i).toString();
            }
        });

        panel.setVisible(true);
        return panel;
    }

    private void doItForClass(boolean uniformSize) {
        // Sort according to category rank
        Set<ClassModel> cmsSet = new HashSet<ClassModel>();
        List<ClassModel> cms = new ArrayList<ClassModel>();
        for (TraceModel tm : ftms) {
            cmsSet.addAll(tm.getClasses().values());
        }
        cms.addAll(cmsSet);
        Collections.sort(cms, new Comparator<ClassModel>() {

            public int compare(ClassModel o1, ClassModel o2) {
                Integer o1r = Controller.getInstance().getAffinity().getClassAffinity(o1.getName()).weigth;
                Integer o2r = Controller.getInstance().getAffinity().getClassAffinity(o2.getName()).weigth;
                return o1r.compareTo(o2r);
            }
        });
        // Insert data
        for (ClassModel cm : cms) {
            for (Result r : scattering) {
                int specific = 0;
                TraceModel tm = null;
                for (TraceModel t : ftms) {
                    if (t.getName().equals(r.name)) {
                        tm = t;
                        break;
                    }
                }
                if (!tm.hasClass(cm.getName())) {
                    continue;
                }
                Controller c = Controller.getInstance();
                double val = 0d;
                if (uniformSize) {
                    val = 1f / tm.getClassSet().size();
                } else {
                    val = r.value / tm.getClassSet().size();
                }
                data.addValue(val, cm.getName(), r.name);
            }
        }
        Controller c = Controller.getInstance();
        for (int i = 0; i < data.getRowCount(); i++) {
            for (ClassModel cm : c.getTraceSet().getAllClassIDs()) {
                if (cm.getName().equals(data.getRowKey(i).toString())) {
                    Color col = Controller.getInstance().getAffinity().getClassAffinity(cm.getName()).color;
                    plot.getRenderer().setSeriesPaint(i, col);
                    plot.getRenderer().setSeriesOutlinePaint(i, col);
                    break;
                }
            }
        }
    }

    private void doItForPkg(boolean uniformSize) {
        // Sort according to category rank
        Set<String> pkgsSet = new HashSet<String>();
        List<String> pkgss = new ArrayList<String>();
        for (TraceModel tm : ftms) {
            for(ClassModel cm : tm.getClassSet()){
                pkgsSet.add(cm.getPackageName());
            }
        }
        pkgss.addAll(pkgsSet);
        Collections.sort(pkgss, new Comparator<String>() {

            public int compare(String o1, String o2) {
                Integer o1r = Controller.getInstance().getAffinity().getPkgAffinity(o1).weigth;
                Integer o2r = Controller.getInstance().getAffinity().getPkgAffinity(o2).weigth;
                return o1r.compareTo(o2r);
            }
        });
        // Insert data
        for (String cm : pkgss) {
            for (Result r : scattering) {
                TraceModel tm = null;
                for (TraceModel t : ftms) {
                    if (t.getName().equals(r.name)) {
                        tm = t;
                        break;
                    }
                }
                Set<String> pkgs = new HashSet<String>();
                for(ClassModel c : tm.getClassSet()){
                    pkgs.add(c.getPackageName());
                }

                if (!pkgs.contains(cm)) {
                    continue;
                }
                Controller c = Controller.getInstance();
                double val = 0d;
                if (uniformSize) {
                    val = 1f / pkgs.size();
                } else {
                    val = r.value / pkgs.size();
                }
                data.addValue(val, cm, r.name);
            }
        }
        Controller c = Controller.getInstance();
        for (int i = 0; i < data.getRowCount(); i++) {
            for (String pk : pkgss) {
                if (pk.equals(data.getRowKey(i).toString())) {
                    Color col = Controller.getInstance().getAffinity().getPkgAffinity(pk).color;
                    plot.getRenderer().setSeriesPaint(i, col);
                    plot.getRenderer().setSeriesOutlinePaint(i, col);
                    break;
                }
            }
        }
    }

    @Override
    public void featureSelectionChanged(SelectionManager tl) {
    }

    @Override
    public void compUnitSelectionChanged(SelectionManager tl) {
        panel.repaint();
    }
}
