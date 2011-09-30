/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.workbench.infrastructure;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.border.AbstractBorder;

/**
 *
 * @author ao
 */
public class ShadowBorder extends AbstractBorder {

    int xoff, yoff;
    Insets insets;

    public ShadowBorder(int x, int y) {
        this.xoff = x;
        this.yoff = y;
        insets = new Insets(0, 0, xoff, yoff);

    }

    public Insets getBorderInsets(Component c) {
        return insets;
    }

    public void paintBorder(Component comp, Graphics g,
            int x, int y, int width, int height) {
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width-xoff -1, height-yoff -1);
        g.setColor(Color.GRAY);
        g.translate(x, y);
        // draw right side
        g.fillRect(width - xoff, yoff, xoff, height - yoff);
        // draw bottom side
        g.fillRect(xoff, height - yoff, width - xoff, yoff);
        g.translate(-x, -y);
    }
}