/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.sourcehighlighter.api;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.Utilities;

/**
 *
 * @author ao
 */
public class FeatureCategoryMarker {
    public static double EDITOR_LINE_HEIGHT_MULTIPLIER_FIX = 1.21;

    private final String method;
    private final long startLine;
    private final long endLine;
    private final Set<String> features;
    private final Color color;

    public FeatureCategoryMarker(String method, long startLine, long endLLine, Set<String> features, Color c) {
        this.method = method;
        this.startLine = startLine;
        this.endLine = endLLine;
        this.features = features;
        this.color = c;
    }

    public String getMethod() {
        return method;
    }

    public long getStartLine() {
        return startLine;
    }

    public long getEndLine() {
        return endLine;
    }

    public Set<String> getFeatures() {
        return features;
    }

    public boolean containsLine(long line) {
        return startLine <= line && line <= endLine;
    }

    public void paint(Graphics g, JTextComponent target) {
        boolean selected = false;
        Set<String> selFeats = new HashSet<String>();
        selFeats.addAll(Controller.getInstance().getTraceSet().getSelectionManager().getSelectedFeats());
        for (String selFeat : selFeats) {
            if (features.contains(selFeat)) {
                selected = true;
                break;
            }
        }

        int line = Utilities.getEditorUI(target).getLineAscent();
        if (selected) {
            g.setColor(new Color(0, 0, 0));
            g.fillRoundRect(1, -2 + (int) ((startLine-1) * line *EDITOR_LINE_HEIGHT_MULTIPLIER_FIX),
                    9, 2 + 2 + (int) ((endLine - startLine+1) * line *EDITOR_LINE_HEIGHT_MULTIPLIER_FIX) ,
                    2, 2);
        }
        g.setColor(color);
        g.fillRoundRect(3, (int) ((startLine - 1) * line * EDITOR_LINE_HEIGHT_MULTIPLIER_FIX),
                5, (int) ((endLine - startLine + 1) * line * EDITOR_LINE_HEIGHT_MULTIPLIER_FIX),
                2, 2);
    }
}
