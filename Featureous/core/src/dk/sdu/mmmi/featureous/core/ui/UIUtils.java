/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.ui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JComponent;

/**
 *
 * @author ao
 */
public class UIUtils {
    public static final Color SELECTION_COLOR = new Color(230, 200, 50);
    public static final Color HIGHLIGHT_COLOR = new Color(230, 230, 100);
    public static final Color NODE_COLOR = new Color(230, 230, 255);
    public static final Color EDGE_COLOR = new Color(200, 200, 200);

    public static void setupDefaultColorForAll(JComponent root){
        root.setBackground(new Color(238, 238, 238));
        root.setOpaque(true);
        for(Component c : root.getComponents()){
            if(c instanceof JComponent){
                setupDefaultColorForAll((JComponent)c);
            }
        }
    }
}
