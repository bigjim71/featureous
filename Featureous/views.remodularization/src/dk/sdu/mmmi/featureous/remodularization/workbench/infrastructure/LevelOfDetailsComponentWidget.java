/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.workbench.infrastructure;

import java.awt.*;
import javax.swing.JComponent;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * This is a widget with a level-of-details feature. The visibility of children is based on the zoom factor of a scene.
 * <p>
 * For <code>&lt; hardMinimalZoom</code> and <code>&gt; hardMaximalZoom</code> the children are not painted.<br>
 * For <code>&lt; softMinimalZoom</code> and <code>&gt; sortMaximalZoom</code> the children are partially painted using alpha-blending.<br>
 * Between <code>softMinimalZoom</code> and <code>softMaximalZoom</code> the children are painted normally.
 *
 * @author David Kaspar
 */
public class LevelOfDetailsComponentWidget extends Widget {

    private double hardMinimalZoom;
    private double softMinimalZoom;
    private double softMaximalZoom;
    private double hardMaximalZoom;
    private final JComponent comp;
    private final ComponentWidget compWidget;

    /**
     * Creates a level-of-details widget.
     * @param scene the scene
     * @param hardMinimalZoom the hard minimal zoom factor
     * @param softMinimalZoom the sort minimal zoom factor
     * @param softMaximalZoom the sort maximal zoom factor
     * @param hardMaximalZoom the hard maximal zoom factor
     */
    public LevelOfDetailsComponentWidget(Scene scene, JComponent comp, double hardMinimalZoom, double softMinimalZoom, double softMaximalZoom, double hardMaximalZoom) {
        super (scene);
        this.comp = comp;
        this.hardMinimalZoom = hardMinimalZoom;
        this.softMinimalZoom = softMinimalZoom;
        this.softMaximalZoom = softMaximalZoom;
        this.hardMaximalZoom = hardMaximalZoom;
        this.setLayout(LayoutFactory.createOverlayLayout());
        this.compWidget = new ComponentWidget(scene, comp);
        addChild(compWidget);
    }

    /**
     * Paints children based on the zoom factor.
     */
    public void paintChildren () {
        double zoom = getScene ().getZoomFactor();
        if (zoom <= hardMinimalZoom  ||  zoom >= hardMaximalZoom){
            comp.setVisible(false);
            comp.setFocusable(false);
            return;
        }

        comp.setVisible(true);
        comp.setFocusable(true);
        
        Graphics2D gr = getGraphics();
        Composite previousComposite = null;
        if (hardMinimalZoom < zoom  &&  zoom < softMinimalZoom) {
            double diff = softMinimalZoom - hardMinimalZoom;
            if (diff > 0.0) {
                diff = (zoom - hardMinimalZoom) / diff;
                previousComposite = gr.getComposite();
                gr.setComposite(AlphaComposite.getInstance (AlphaComposite.SRC_OVER, (float) diff));
            }
        } else if (softMaximalZoom < zoom  &&  zoom < hardMaximalZoom) {
            double diff = hardMaximalZoom - softMaximalZoom;
            if (diff > 0.0) {
                diff = (hardMaximalZoom - zoom) / diff;
                previousComposite = gr.getComposite();
                gr.setComposite(AlphaComposite.getInstance (AlphaComposite.SRC_OVER, (float) diff));
            }
        }

        super.paintChildren ();

        if (previousComposite != null)
            gr.setComposite(previousComposite);
    }

    /**
     * Checks whether a specified local location is a part of a widget based on the zoom factor.
     * @param localLocation the local location
     * @return true, it it is
     */
    public boolean isHitAt(Point localLocation) {
        double zoom = getScene().getZoomFactor();
        if (zoom < hardMinimalZoom  ||  zoom > hardMaximalZoom)
            return false;
        return super.isHitAt(localLocation);
    }
}