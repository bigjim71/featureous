/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.sourcehighlighter;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.Trees;
import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.SelectionChangeListener;
import dk.sdu.mmmi.featureous.core.model.SelectionManager;
import dk.sdu.mmmi.featureous.core.model.TraceListChangeListener;
import dk.sdu.mmmi.featureous.core.model.TraceSet;
import dk.sdu.mmmi.featureous.core.ui.OutputUtil;
import dk.sdu.mmmi.featureous.sourcehighlighter.api.FeatureCategoryMarker;
import dk.sdu.mmmi.featureous.sourcehighlighter.api.LineInfoTag;
import dk.sdu.mmmi.featureous.sourcehighlighter.api.MethodPositionVisitor;
import dk.sdu.mmmi.srcUtils.AnalysisUtils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.Utilities;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import recoder.ParserException;

/**
 *
 * @author ao
 */
public class FeatureColoringSidebar extends TopComponent {

    private final JTextComponent target;
    private final List<FeatureCategoryMarker> markers;
    private final List<LineInfoTag> tags;
    private int BAR_WIDTH = 10;
    private String targetClass = null;

    public FeatureColoringSidebar(JTextComponent target) throws IOException {
        markers = new ArrayList<FeatureCategoryMarker>();
        tags = new ArrayList<LineInfoTag>();
        setToolTipText("");
        this.target = target;

        Controller.getInstance().getTraceSet().addChangeListener(tcl);
        Controller.getInstance().getTraceSet().getSelectionManager().addSelectionListener(scl);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        //Adding a bunch of document listeners
        Utilities.getDocument(target).addDocumentListener(dl);//PostModificationDocumentListener(dl);
        FoldHierarchy fh = FoldHierarchy.get(target);
        fh.addFoldHierarchyListener(fhl);
        target.addFocusListener(focList);
        target.addCaretListener(carList);
        update();
        this.addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent mwe) {
                FeatureCategoryMarker.EDITOR_LINE_HEIGHT_MULTIPLIER_FIX += 0.001*mwe.getWheelRotation();
                repaint();
            }
        });
    }

    public List<FeatureCategoryMarker> getMarkers() {
        return markers;
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }

    @Override
    public void removeNotify() {
        Utilities.getDocument(target).removeDocumentListener(dl);
        Controller.getInstance().getTraceSet().removeChangeListener(tcl);
        Controller.getInstance().getTraceSet().getSelectionManager().removeSelectionListener(scl);
        FoldHierarchy fh = FoldHierarchy.get(target);
        fh.removeFoldHierarchyListener(fhl);
        target.removeFocusListener(focList);
        target.removeCaretListener(carList);
    }

    private void update() {
//        // TODO:
//        // WARNING [org.netbeans.modules.parsing.impl.TaskProcessor]: ParserManager.parse called in AWT event thread by: dk.sdu.mmmi.featureous.sourcehighlighter.diff.CategorizationSidebar$1.run(CategorizationSidebar.java:83)
//        JavaSource src = JavaSource.forDocument(target.getDocument());
//
//        if (src == null) {
//            return;
//        }
//        try {
//            src.runUserActionTask((Task<CompilationController>) new ColoringTask(), true);
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }

        if (Controller.getInstance().getTraceSet().getFirstLevelTraces().isEmpty()) {
            markers.clear();
            tags.clear();
            return;
        }
        FeatureBarExtractor fbe = new FeatureBarExtractor();
        try {
            AnalysisUtils.analyzeParsedEditorContent(target, fbe);
            markers.clear();
            markers.addAll(fbe.getMarkers());
            tags.clear();
            tags.addAll(fbe.getTags());

            this.targetClass = fbe.getTargetClass();
            updateClassFocus();
        } catch (ParserException pe) {
            // Silent fail
        }
    }

    private void updateClassFocus() {
        SelectionManager selMan = Controller.getInstance().getTraceSet().getSelectionManager();
        if(targetClass == null){
            selMan.getSelectedClasses().clear();
            selMan.notifyUnitSelectionChanged();
        }
        if (targetClass != null && !selMan.getSelectedClasses().contains(targetClass)) {
            selMan.getSelectedClasses().clear();
            Set<String> sel = new HashSet<String>();
            sel.add(targetClass);
            selMan.addClassSelection(sel);
        }
    }

    private void updateMethodFocus(String mm) {
        SelectionManager selMan = Controller.getInstance().getTraceSet().getSelectionManager();
        if(mm == null){
            selMan.getSelectedExecs().clear();
            selMan.notifyUnitSelectionChanged();
        }
        if (mm != null && !selMan.getSelectedExecs().contains(mm)) {
            selMan.getSelectedExecs().clear();
            Set<String> sel = new HashSet<String>();
            sel.add(mm);
            selMan.addExecSelection(sel);
        }
    }

    @Deprecated
    public class ColoringTask implements CancellableTask<CompilationController> {

        public void run(CompilationController p) throws Exception {


            p.toPhase(JavaSource.Phase.PARSED);
            CompilationUnitTree cuTree = p.getCompilationUnit();
//            SourcePositions sp = p.getTrees().getSourcePositions();
//            for (Tree tr : cuTree.getTypeDecls()) {
//                TypeElement te = (TypeElement) p.getTrees().getElement(p.getTrees().getPath(cuTree, tr));
//                if (te != null) {
//                    long tstart = sp.getStartPosition(cuTree, p.getTrees().getTree(te));
//                    markersTmp.add(new FeatureCategoryMarker(
//                            cuTree.getLineMap().getLineNumber(tstart),
//                            cuTree.getLineMap().getLineNumber(tstart) + 1, fts, Color.PINK));
//                    for (Element el : te.getEnclosedElements()) {
//                        if (el.getKind().equals(ElementKind.METHOD)
//                                || el.getKind().equals(ElementKind.CONSTRUCTOR)) {
//                            long start = sp.getStartPosition(cuTree, p.getTrees().getTree(el));
//                            long end = sp.getEndPosition(cuTree, p.getTrees().getTree(el));
//                            if (start <= end) {
//                                markersTmp.add(new FeatureCategoryMarker(
//                                        cuTree.getLineMap().getLineNumber(start),
//                                        cuTree.getLineMap().getLineNumber(end),
//                                        fts, Color.cyan));
//                            }
//                        }
//                    }
//                }
//            }

            Trees trees = p.getTrees();
            MethodPositionVisitor posVis = new MethodPositionVisitor(trees.getSourcePositions(),
                    target);

            posVis.scan(cuTree, null);
            markers.clear();
//                markers.addAll(markersTmp);
            markers.addAll(posVis.getMarkers());


            repaint();
        }

        @Override
        public void cancel() {
            OutputUtil.log("Feature sidebar scan cancelled");
        }
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        List<String> feats = getFeatsMarksAt(event);
        Collections.sort(feats);
        StringBuilder msg = new StringBuilder("<html>Features: ");
        for (String s : feats) {
            msg.append("<br>- ");
            msg.append(s);
        }
        return (feats.size() > 0) ? msg.toString() : null;
    }

    private int getLineFromMouseEvent(MouseEvent e) {
        int line = -1;
        if (Utilities.getEditorUI(target) != null) {
            try {
                JTextComponent component = Utilities.getEditorUI(target).getComponent();
                if (component != null) {
                    BaseTextUI textUI = (BaseTextUI) component.getUI();
                    int clickOffset = textUI.viewToModel(component, new Point(0, e.getY()));
                    line = Utilities.getLineOffset(Utilities.getEditorUI(target).getDocument(), clickOffset);
                    line = getUnfoldedLineNumberByFoldedLine(component, line);
                }

            } catch (BadLocationException ble) {
                Logger.getLogger(FeatureColoringSidebar.class.getName()).log(Level.WARNING, "getLineFromMouseEvent", ble); // NOI18N
            }
        }
        return line;
    }
    
        public static int getUnfoldedLineNumberByFoldedLine(JTextComponent document, int line) throws BadLocationException {
        int minusRows = 0;
        FoldHierarchy fh = FoldHierarchy.get(document);
        if (fh != null) {
            Fold root = fh.getRootFold();
            if (root != null) {
                for (int i = 0; i < root.getFoldCount(); i++) {
                    Fold curr = root.getFold(i);
                    if (curr.isCollapsed()) {
                        int endLine = MethodPositionVisitor.getRawLineNumber(document, curr.getEndOffset());
                        int startLine = MethodPositionVisitor.getRawLineNumber(document, curr.getStartOffset());
                        if (endLine < line) {
                            minusRows += endLine - startLine;
                        } else if (startLine <= line && endLine >= line) {
                            minusRows += line - startLine;
                        }
                    }
                }
            }
        }
        return line - minusRows;
    }
    
    private List<String> getFeatsMarksAt(MouseEvent event) {
        List<String> feats = new ArrayList<String>();
        int line = getLineFromMouseEvent(event);
        if (line == -1) {
            return feats;
        }
        for (FeatureCategoryMarker m : findFeatureMarkersForLine(line + 1)) {
            feats.addAll(m.getFeatures());
        }
        return feats;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension dim = target.getSize();
        dim.width = BAR_WIDTH;
        return dim;
    }

    //TODO: from line-by-line this has to be changed into marker-by-marker since we assume no markers are overlapping now!
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Rectangle clip = g.getClipBounds();
        if (clip.y >= 16) {
            // compensate for scrolling: marks on bottom/top edges are not drawn completely while scrolling
            clip.y -= 16;
            clip.height += 16;
        }

        JTextComponent component = target;
        if (component == null) {
            return;
        }

        BaseTextUI textUI = (BaseTextUI) component.getUI();
        View rootView = Utilities.getDocumentView(component);
        if (rootView == null) {
            return;
        }

        g.setColor(Color.white);
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

//            int startPos = textUI.getPosFromY(clip.y);
//            int startViewIndex = rootView.getViewIndex(startPos, Position.Bias.Forward);
//            int rootViewCount = rootView.getViewCount();

//            if (startViewIndex >= 0 && startViewIndex < rootViewCount) {
        List<FeatureCategoryMarker> ms = new ArrayList<FeatureCategoryMarker>();
        ms.addAll(markers);
        for (FeatureCategoryMarker mark : ms) {
            mark.paint(g, target);
        }

//        for(LineInfoTag lit : tags){
//            OutputUtil.log("Tag: "+ lit.getLine() + lit.getMsg());
//        }
//            }
    }

    private List<FeatureCategoryMarker> findFeatureMarkersForLine(long line) {
        List<FeatureCategoryMarker> ms = new ArrayList<FeatureCategoryMarker>();
        Set<FeatureCategoryMarker> marks = new HashSet<FeatureCategoryMarker>();
        marks.addAll(markers);
        for (FeatureCategoryMarker m : marks) {
            if (m.containsLine(line)) {
                ms.add(m);
            }
        }
        return ms;
    }
    private final DocumentListener dl = new DocumentListener() {

        public void insertUpdate(DocumentEvent e) {
            update();
        }

        public void removeUpdate(DocumentEvent e) {
            update();
        }

        public void changedUpdate(DocumentEvent e) {
            return;
        }
    };
    private TraceListChangeListener tcl = new TraceListChangeListener() {

        public void traceListChanged(TraceSet tl) {
            update();
        }
    };
    private SelectionChangeListener scl = new SelectionChangeListener() {

        @Override
        public void featureSelectionChanged(SelectionManager tl) {
            repaint();
        }

        @Override
        public void compUnitSelectionChanged(SelectionManager tl) {
        }
    };
    private FoldHierarchyListener fhl = new FoldHierarchyListener() {

        @Override
        public void foldHierarchyChanged(FoldHierarchyEvent fhe) {
            FoldHierarchy fh = FoldHierarchy.get(target);
            if (fh != null && fh.getRootFold() != null) {
                update();
            }
        }
    };

    private FocusListener focList = new FocusAdapter() {

        @Override
        public void focusGained(FocusEvent e) {
            updateClassFocus();
            carList.caretUpdate(null);
        }
    };

    private CaretListener carList = new CaretListener() {

        @Override
        public void caretUpdate(CaretEvent e) {
            try {
                int caretLine = Utilities.getRowCount(Utilities.getDocument(target), 0, target.getCaretPosition());
                updateMethodFocus(null);
                for(FeatureCategoryMarker fcm : findFeatureMarkersForLine(
                        FeatureBarExtractor.getFoldedLineNumberByLine(target, caretLine))){
                    updateMethodFocus(fcm.getMethod());
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    };
}
