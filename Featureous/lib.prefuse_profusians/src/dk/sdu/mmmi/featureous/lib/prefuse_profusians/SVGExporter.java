/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.lib.prefuse_profusians;

import com.sun.imageio.plugins.png.PNGImageWriter;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.imageio.spi.ImageWriterSpi;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.ext.awt.image.spi.ImageWriter;
import org.apache.batik.ext.awt.image.spi.ImageWriterParams;
import org.apache.batik.ext.awt.image.spi.ImageWriterRegistry;
import org.apache.batik.svggen.CachedImageHandlerBase64Encoder;
import org.apache.batik.svggen.DefaultImageHandler;
import org.apache.batik.svggen.GenericImageHandler;
import org.apache.batik.svggen.ImageHandler;
import org.apache.batik.svggen.ImageHandlerBase64Encoder;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import prefuse.Display;

/**
 *
 * @author ao
 */
public class SVGExporter {

    public static void exportChartAsSVG(Display graph, File svgFile) throws IOException {
        // Get a DOMImplementation and create an XML document
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument(null, "svg", null);

        SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);

        // Reuse our embedded base64-encoded image data.
        ImageHandler ih = new ImageHandlerBase64Encoder();
        ctx.setImageHandler(ih);

        // Create an instance of the SVG Generator
        SVGGraphics2D svgGenerator = new SVGGraphics2D(ctx, false){

            @Override
            public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
                try{
                    return super.drawImage(img, x, y, observer);
                }catch(NullPointerException npe){
                    npe.printStackTrace();
                    return false;
                }
            }
        };

        // draw the chart in the SVG generator
        graph.setDamageRedraw(false);
        graph.repaintImmediate();
        graph.paintDisplay(svgGenerator, graph.getSize());
        graph.setDamageRedraw(true);

        // Write svg file
        OutputStream outputStream = new FileOutputStream(svgFile);
        Writer out = new OutputStreamWriter(outputStream, "UTF-8");
        svgGenerator.stream(out, true /* use css */);
        outputStream.flush();
        outputStream.close();
    }

    public static Action createExportAction(final Display graph){
        return new AbstractAction("Save as SVG...") {

            public void actionPerformed(ActionEvent e) {
                try {
                    JFileChooser fc = new JFileChooser();
                    fc.showSaveDialog(null);
                    if(fc.getSelectedFile()!=null){
                        exportChartAsSVG(graph, fc.getSelectedFile());
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };
    }
}
