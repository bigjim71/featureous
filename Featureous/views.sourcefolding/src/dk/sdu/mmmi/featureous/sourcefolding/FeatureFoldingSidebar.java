/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.sourcefolding;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.SelectionChangeListener;
import dk.sdu.mmmi.featureous.core.model.SelectionManager;
import dk.sdu.mmmi.featureous.sourcehighlighter.FeatureBarExtractor;
import dk.sdu.mmmi.featureous.sourcehighlighter.FeatureColoringSidebar;
import dk.sdu.mmmi.featureous.sourcehighlighter.FeatureColoringSidebarFactory;
import dk.sdu.mmmi.featureous.sourcehighlighter.api.FeatureCategoryMarker;
import java.awt.Dimension;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.java.JavaFoldManager;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

/**
 *
 * @author ao
 */
public class FeatureFoldingSidebar extends TopComponent {

    private final JTextComponent target;
    private int BAR_WIDTH = 0;

    public FeatureFoldingSidebar(JTextComponent target) throws IOException {
        setToolTipText("");
        this.target = target;
        Controller.getInstance().getTraceSet().getSelectionManager().addSelectionListener(scl);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        update();
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }

    @Override
    public void removeNotify() {
        Controller.getInstance().getTraceSet().getSelectionManager().removeSelectionListener(scl);
    }

    private void update() {
        Set<Fold> toFocus = new HashSet<Fold>();
        Set<String> feats = Controller.getInstance().getTraceSet().getSelectionManager().getSelectedFeats();
        BaseDocument doc = Utilities.getDocument(target);
        FoldHierarchy fh = FoldHierarchy.get(target);
        if (fh != null) {
            Fold root = fh.getRootFold();
            if (root != null) {
                for (int i = 0; i < root.getFoldCount(); i++) {
                    Fold curr = root.getFold(i);
                    FeatureColoringSidebar csb = FeatureColoringSidebarFactory.getMapping().get(target);
                    for (FeatureCategoryMarker fcm : csb.getMarkers()) {
                        try {
                            int foldS = FeatureBarExtractor.getFoldedLineNumber(target, curr.getStartOffset());
                            int foldE = FeatureBarExtractor.getFoldedLineNumber(target, curr.getEndOffset());
                            int colS = (int) fcm.getStartLine();
                            int colE = (int) fcm.getEndLine();
                            if (colS <= foldS && colE >= foldE) {
                                for (String fs : feats) {
                                    if (fcm.getFeatures().contains(fs)) {
                                        toFocus.add(curr);
                                        break;
                                    }
                                }
                                break;
                            }
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }

//        FoldUtilities.collapseAll(fh);
        Fold root = fh.getRootFold();
        for (int i = 0; i < root.getFoldCount(); i++) {
            Fold curr = root.getFold(i);
            if (curr.getType().equals(JavaFoldManager.CODE_BLOCK_FOLD_TYPE)) {
                if (toFocus.contains(curr) || feats.isEmpty()) {
                    fh.expand(curr);
                } else {
                    fh.collapse(curr);
                }
            }
        }
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension dim = target.getSize();
        dim.width = BAR_WIDTH;
        return dim;
    }

    private SelectionChangeListener scl = new SelectionChangeListener() {

        @Override
        public void featureSelectionChanged(SelectionManager tl) {
            update();
        }

        @Override
        public void compUnitSelectionChanged(SelectionManager tl) {
        }
    };
}
