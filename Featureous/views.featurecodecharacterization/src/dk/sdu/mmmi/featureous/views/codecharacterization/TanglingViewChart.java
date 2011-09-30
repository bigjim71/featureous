/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.views.codecharacterization;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.SelectionChangeListener;
import dk.sdu.mmmi.featureous.core.model.SelectionManager;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.ui.OutputUtil;
import dk.sdu.mmmi.featureous.core.ui.UIUtils;
import dk.sdu.mmmi.featureous.metrics.Result;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.Tangling;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
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
import org.jfree.data.general.DefaultKeyedValues2DDataset;

public class TanglingViewChart implements SelectionChangeListener{

    private ChartPanel panel;
    private JFreeChart jchart;
    private DefaultCategoryDataset data;
    private final boolean pkg;
    private final boolean sortByValue;

    public TanglingViewChart(boolean pkg, boolean sortByValue) {
        this.sortByValue = sortByValue;
        this.pkg = pkg;
        data = new DefaultKeyedValues2DDataset();
        String title = "Computational unit characterization";
        jchart = ChartFactory.createStackedBarChart(title, (pkg)?"Package":"Class", "Tangling", data, PlotOrientation.VERTICAL, true, false, false);
        CategoryPlot plot = (CategoryPlot) jchart.getPlot();
//        chart.getLegend().setPosition(RectangleEdge.RIGHT);
//        chart.getLegend().setVerticalAlignment(VerticalAlignment.TOP);
//        chart.getLegend().setHorizontalAlignment(HorizontalAlignment.LEFT);
        LegendItemCollection lic = new LegendItemCollection();
//        lic.add(new LegendItem("Infrastructural unit", "", "", "", new Rectangle(10, 10), Color.GREEN));
//        lic.add(new LegendItem("Group-feature unit", "", "", "", new Rectangle(10, 10), Color.BLUE));
//        lic.add(new LegendItem("Single-feature unit", "", "", "", new Rectangle(10, 10), Color.RED));
        plot.setFixedLegendItems(lic);
//        chart.removeLegend();
        plot.setDomainAxis(new SparselyLabeledCategoryAxis(20));
        CategoryAxis xAxis = (CategoryAxis) plot.getDomainAxis();
        xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        xAxis.setLabel((pkg)?"Package":"Class");
//        xAxis.setTickLabelInsets(new RectangleInsets(0, 0, 5, 5));
//        xAxis.setMaximumCategoryLabelLines(1);
        xAxis.setLowerMargin(0);
        xAxis.setCategoryMargin(0);
        xAxis.setUpperMargin(0);
//        xAxis.setMaximumCategoryLabelWidthRatio(20f);
        jchart.setBackgroundPaint(Color.white);

        StackedBarRenderer renderer = new StackedBarRenderer(){

            @Override
            public void drawItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset, int row, int column, int pass) {
                super.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, column, pass);
                double start = plot.getDomainAxis().getCategoryStart(column, getColumnCount(), dataArea, plot.getDomainAxisEdge());
                double end = plot.getDomainAxis().getCategoryEnd(column, getColumnCount(), dataArea, plot.getDomainAxisEdge());

                String compUnit = data.getRowKey(row).toString();

                // Calculate y coeffs
                double posBase = getBase();
//                for(int i = 0; i<row; i++){
//                    Number val = dataset.getValue(i, column);
//                    if(val!=null){
//                        posBase = posBase + val.doubleValue();
//                    }
//                }

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
        
        renderer.setToolTipGenerator(new CategoryToolTipGenerator() {

            public String generateToolTip(CategoryDataset cd, int i, int i1) {
                String key = data.getRowKey(i).toString();
//                key = key.substring(0, key.length()-1);
                return "<html>" + i+ " - " + key+ "<br>"+ Double.toString(cd.getValue(i, i1).doubleValue()) +
                        "</hmtl>";
            }
        });
        
        plot.setRenderer(renderer);

        panel = new ChartPanel(jchart);
        
        panel.getPopupMenu().setEnabled(false);//add(SVGExporter.createExportAction(chart, panel));
        
        createView();
        Controller.getInstance().getTraceSet().getSelectionManager().addSelectionListener(this);
    }

    public JFreeChart getJchart() {
        return jchart;
    }
    
    public void dispose(){
        Controller.getInstance().getTraceSet().getSelectionManager().removeSelectionListener(this);
    }

    private void createView() {
        Controller c = Controller.getInstance();
        Set<TraceModel> traces = c.getTraceSet().getFirstLevelTraces();

        List<Result> ress = new ArrayList<Result>(new Tangling(pkg).calculateAndReturnAll(traces, null));

        for(Result r : ress){
//            OutputUtil.log(r.name + ";" +r.value);
        }
        Result.sortByName(ress);
        
        if(sortByValue){
            Collections.sort(ress);
//            Collections.reverse(ress);
        }

        double max = 0;
        for(Result r : ress){
            if(r.value>max){
                max = r.value;
            }
        }

        for(Result r : ress){
            String label = "" + (ress.indexOf(r)+1);
            data.addValue(r.value, r.name , label);
//            data.addValue(max - r.value, r.name + "f", label);
        }

        double total = 0;
        for(Result r : ress){
            total += r.value;
        }

        jchart.setTitle(jchart.getTitle().getText());// + "\n" + "Total tang = " + String.format("%.5f", total));

        CategoryPlot plot = jchart.getCategoryPlot();
        ((StackedBarRenderer)plot.getRenderer()).setDrawBarOutline(true);
        plot.getRenderer().setOutlineStroke(new BasicStroke(0.1f));

        for(int i = 0; i<data.getRowCount(); i++){
            Set<Object> elems = new HashSet<Object>();
            for(ClassModel cm : c.getTraceSet().getAllClassIDs()){
                if(!pkg){
                    elems.add(cm);
                }else{
                    elems.add(cm.getPackageName());
                }
            }
            for(Object cm : elems){
                String key = data.getRowKey(i).toString();
                String name = "";
                if(!pkg){
                    name = ((ClassModel) cm).getName();
                }else{
                    name = (String)cm;
                }
                if(key!=null && key.startsWith(name)){
                    Color col = Color.pink;
                    if(!pkg){
                        col = Controller.getInstance().getAffinity().getClassAffinity(((ClassModel)cm).getName()).color;
                    }else{
                        col = Controller.getInstance().getAffinity().getPkgAffinity((String)cm).color;
                    }

                    plot.getRenderer().setSeriesPaint(i, col);
//                    if(key.endsWith("f")){
//                        plot.getRenderer().setSeriesOutlinePaint(i, col);
//                    }else{
                        plot.getRenderer().setSeriesOutlinePaint(i, Color.DARK_GRAY);
//                    }
                    break;
                }
            }
        }
//        plot.setRowRenderingOrder(SortOrder.DESCENDING);

    }
    
    public JPanel getChart() {
        panel.setVisible(true);
        return panel;
    }

    @Override
    public void featureSelectionChanged(SelectionManager tl) {
    }

    @Override
    public void compUnitSelectionChanged(SelectionManager tl) {
        panel.repaint();
    }
}
