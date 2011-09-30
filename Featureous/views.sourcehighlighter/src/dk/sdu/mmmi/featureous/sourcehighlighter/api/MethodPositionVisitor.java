/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.sourcehighlighter.api;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePathScanner;
import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.model.TraceSet;
import java.awt.Color;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.openide.util.Exceptions;

/**
 *
 * @author ao
 */
@Deprecated
public class MethodPositionVisitor extends TreePathScanner<Void, VisitorState> {

    private final SourcePositions sourcePositions;
    private final List<FeatureCategoryMarker> markers;
    private CompilationUnitTree cu;
    private final JTextComponent jt;

    public MethodPositionVisitor(SourcePositions sourcePositions, JTextComponent jt) {
        this.markers = new LinkedList<FeatureCategoryMarker>();
        this.sourcePositions = sourcePositions;
        this.jt = jt;
    }

    public List<FeatureCategoryMarker> getMarkers() {
        return markers;
    }

    @Override
    public Void visitCompilationUnit(CompilationUnitTree node, VisitorState p) {
        VisitorState vs = new VisitorState();
        vs.lastPkg = node.getPackageName().toString();
        this.cu = node;
        return super.visitCompilationUnit(node, vs);
    }

    @Override
    public Void visitClass(ClassTree node, VisitorState p) {
        long startPos = this.sourcePositions.getStartPosition(cu, node);
        for(Tree t : node.getMembers()){
        }
        VisitorState vs = new VisitorState();
        vs.lastPkg = p.lastPkg;
        vs.lastClass = node.getSimpleName().toString();
        TraceSet ts = Controller.getInstance().getTraceSet();
        Set<String> feats = new HashSet<String>();
        Color col = null;
        for (ClassModel cm : ts.getAllClassIDs()) {
            if (cm.getPackageName().equals(vs.lastPkg)) {
                if (cm.getName().endsWith(vs.lastClass)) {
                    col = Controller.getInstance().getAffinity().getClassAffinity(cm.getName()).color;
                    for (TraceModel tm : ts.getFirstLevelTraces()) {
                        if (tm.hasClass(cm.getName())) {
                            feats.add(tm.getName());
                        }
                    }
                    break;
                }
            }
        }

        if (col != null) {
            try {
                markers.add(new FeatureCategoryMarker(null,
                        getFoldedLineNumber(jt, startPos),
                        getFoldedLineNumber(jt, startPos),
                        feats, col));
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return super.visitClass(node, vs);
    }

    @Override
    public Void visitMethod(MethodTree node, VisitorState p) {
        long startPos = this.sourcePositions.getStartPosition(cu, node);
        long endPos = this.sourcePositions.getEndPosition(cu, node);
        TraceSet ts = Controller.getInstance().getTraceSet();
        String ms = null;
        int usageCount = 0;
        Set<String> feats = new HashSet<String>();
        for (TraceModel tm : ts.getFirstLevelTraces()) {
            for (ClassModel cm : tm.getClassSet()) {
                if (cm.getPackageName().equals(p.lastPkg)) {
                    if (cm.getName().endsWith(p.lastClass)) {
                        for (String m : cm.getAllMethods()) {
                            boolean match = false;
                            if (node.getName().toString().equals("<init>")) {
                                match = compareAJMethodSignWithInit(m, p.lastClass.substring(1 + p.lastClass.lastIndexOf(".")));
                            } else {
                                match = compareAJMethodSignWithJavaMethodName(m, node.getName().toString());

                            }
                            if (match) {
                                ms = m;
                                feats.add(tm.getName());
                                usageCount++;
                            }
                        }
                    }
                }
            }
        }

        if (usageCount != 0) {
            Color mColor = Controller.getInstance().getAffinity().getMethodAffinity(ms).color;
            try {
                markers.add(new FeatureCategoryMarker(null,
                        getFoldedLineNumber(jt, startPos),
                        getFoldedLineNumber(jt, endPos),
                        feats, mColor));
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    @Override
    public Void visitVariable(VariableTree vt, VisitorState p) {
        long startPos = this.sourcePositions.getStartPosition(cu, vt);
        long endPos = this.sourcePositions.getEndPosition(cu, vt);
        TraceSet ts = Controller.getInstance().getTraceSet();
        String fs = null;
        int usageCount = 0;
        Set<String> feats = new HashSet<String>();
        for (TraceModel tm : ts.getFirstLevelTraces()) {
            for (ClassModel cm : tm.getClassSet()) {
                if (cm.getPackageName().equals(p.lastPkg)) {
                    if (cm.getName().endsWith(p.lastClass)) {
                        boolean match = false;
                        for (String f : cm.getAllFields()) {
                            match = compareAJMethodSignWithJavaMethodName(f, vt.getName().toString());
                            if (match) {
                                fs = f;
                                feats.add(tm.getName());
                                usageCount++;
                            }
                        }
                    }
                }
            }
        }

        if (usageCount != 0) {
            Color fColor = Controller.getInstance().getAffinity().getFieldAffinity(fs).color;
            try {
                markers.add(new FeatureCategoryMarker(null,
                        getFoldedLineNumber(jt, startPos),
                        getFoldedLineNumber(jt, endPos),
                        feats, fColor));
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    private boolean compareAJMethodSignWithJavaMethodName(String ajMs, String javaMn) {
        return ajMs.contains("." + javaMn + "(");
    }

    private boolean compareAJMethodSignWithInit(String ajMs, String className) {
        return ajMs.contains("." + className + "(");
    }

    @Override
    public Void scan(Tree tree, VisitorState p) {
        return super.scan(tree, p);
    }

    public static int getRawLineNumber(JTextComponent document, long offset) throws BadLocationException {
        return Utilities.getRowCount(Utilities.getDocument(document), 0, (int) offset);
    }

    public static int getFoldedLineNumber(JTextComponent document, long offset) throws BadLocationException {
        int minusRows = 0;
        BaseDocument doc = Utilities.getDocument(document);
        FoldHierarchy fh = FoldHierarchy.get(document);
        if (fh != null) {
            Fold root = fh.getRootFold();
            if (root != null) {
                for (int i = 0; i < root.getFoldCount(); i++) {
                    Fold curr = root.getFold(i);
                    if (curr.isCollapsed()) {
                        if (curr.getEndOffset() < offset) {
                            minusRows += Utilities.getRowCount(doc,
                                    curr.getStartOffset(), curr.getEndOffset()) - 1;
                        } else if (curr.getStartOffset() <= offset && curr.getEndOffset() >= offset) {
                            minusRows += Utilities.getRowCount(doc,
                                    curr.getStartOffset(), (int) offset) - 1;
                        }
                    }
                }
            }
        }
        return getRawLineNumber(document, offset) - minusRows;
    }
}

class VisitorState {

    String lastPkg;
    String lastClass;
}
