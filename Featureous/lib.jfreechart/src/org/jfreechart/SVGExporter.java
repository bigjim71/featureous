/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package org.jfreechart;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.JFreeChart;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 *
 * @author ao
 */
public class SVGExporter {

    public static void exportChartAsSVG(JFreeChart chart, Rectangle bounds, File svgFile) throws IOException {
        // Get a DOMImplementation and create an XML document
        DOMImplementation domImpl =
                GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument(null, "svg", null);

        // Create an instance of the SVG Generator
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // draw the chart in the SVG generator
        chart.draw(svgGenerator, bounds);

        // Write svg file
        OutputStream outputStream = new FileOutputStream(svgFile);
        Writer out = new OutputStreamWriter(outputStream, "UTF-8");
        svgGenerator.stream(out, true /* use css */);
        outputStream.flush();
        outputStream.close();
    }

    public static Action createExportAction(final JFreeChart chart, final JPanel bounds){
        return new AbstractAction("Save as SVG...") {

            public void actionPerformed(ActionEvent e) {
                try {
                    JFileChooser fc = new JFileChooser();
                    fc.showSaveDialog(null);
                    if(fc.getSelectedFile()!=null){
                        exportChartAsSVG(chart, bounds.getBounds(), fc.getSelectedFile());
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };
    }
}
