/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.views.featurecodecharacterization.barchart3d.customized;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.views.featurecodecharacterization.barchart3d.BarChartBar;
import java.awt.Graphics2D;
import org.jzy3d.maths.IntegerCoord2d;
import org.jzy3d.plot3d.rendering.tooltips.TextTooltipRenderer;

/**
 *
 * @author ao
 */
public class ToggleTextTooltipRenderer extends TextTooltipRenderer {

    private final BarChartBar ad;
    private boolean visible = false;

    public ToggleTextTooltipRenderer(String text, final BarChartBar ad) {
        super(text, new IntegerCoord2d(), ad.getBounds().getCenter());
        this.ad = ad;
    }

    @Override
    public void render(Graphics2D g2d) {
        if (visible) {
            updateTargetCoordinate(ad.getBounds().getCenter());
            IntegerCoord2d c2d = ad.getCenterToScreenProj();
            updateScreenPosition(c2d);

            this.text = ad.getCompUnit();

            super.render(g2d);
        }
    }

    public void setVisible(boolean b) {
        visible = b;
    }
}
