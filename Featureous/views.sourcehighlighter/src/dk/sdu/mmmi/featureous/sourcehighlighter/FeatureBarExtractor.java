/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.sourcehighlighter;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.OrderedBinaryRelation;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.model.TraceSet;
import dk.sdu.mmmi.featureous.sourcehighlighter.api.FeatureCategoryMarker;
import dk.sdu.mmmi.featureous.sourcehighlighter.api.LineInfoTag;
import dk.sdu.mmmi.srcUtils.EditorAnalysis;
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
import recoder.abstraction.Constructor;
import recoder.abstraction.Field;
import recoder.abstraction.Method;
import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.declaration.FieldSpecification;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.reference.MethodReference;
import recoder.kit.ProblemReport;
import recoder.kit.Transformation;

/**
 *
 * @author ao
 */
public class FeatureBarExtractor extends Transformation implements EditorAnalysis {

    private final List<FeatureCategoryMarker> markers = new LinkedList<FeatureCategoryMarker>();
    private final List<LineInfoTag> tags = new LinkedList<LineInfoTag>();
    private JTextComponent target;
    private String targetClass = null;
    private List<CompilationUnit> scope;

    public String getTargetClass() {
        return targetClass;
    }

    public List<FeatureCategoryMarker> getMarkers() {
        return markers;
    }

    public List<LineInfoTag> getTags() {
        return tags;
    }

    @Override
    public ProblemReport execute() {
        for (int i = 0; i < scope.get(0).getTypeDeclarationCount(); i++) {
            TypeDeclaration td = scope.get(0).getTypeDeclarationAt(i);
            handleType(td);
            for (Constructor c : getCrossReferenceSourceInfo().getConstructors(td)) {
                handleMethod(c);
            }
            for (final Method m : getCrossReferenceSourceInfo().getMethods(td)) {
                handleMethod(m);
                //----
                Set<String> sel = Controller.getInstance().getTraceSet().getSelectionManager().getSelectedFeats();
                if (sel.size() == 1) {
                    final TraceModel tm = Controller.getInstance().getTraceSet().getFirstLevelTraceByName(sel.iterator().next());
                    TreeWalker tw = new TreeWalker(getSourceInfo().getMethodDeclaration(m));
                    while (tw.next()) {

                        if (tw.getProgramElement() instanceof MethodReference) {
                            MethodReference x = (MethodReference) tw.getProgramElement();
//                            OutputUtil.log(x.getName());
                            for (OrderedBinaryRelation<String, Integer> r : tm.getMethodInvocations()) {
                                if (compareAJMethodSignWithJavaMethodName(r.getFirst(), m.getName())
                                        && compareAJMethodSignWithJavaMethodName(r.getSecond(), x.getName())) {
//                                    OutputUtil.log("Feat-local: " + r.getFirst() + "->" + r.getSecond());
                                }
                            }
                        }
                    }
                }
            }

            for (final Field f : getCrossReferenceSourceInfo().getFields(td)) {
                handleField(f);
            }
        }
        return NO_PROBLEM;
    }

    private void handleType(TypeDeclaration td) {
        TraceSet ts = Controller.getInstance().getTraceSet();
        Set<String> feats = new HashSet<String>();
        Set<String> instFeats = new HashSet<String>();
        Color col = null;
        targetClass = null;
        for (ClassModel cm : ts.getAllClassIDs()) {
            if (cm.getPackageName().equals(getSourceInfo().getPackage(td).getFullName())) {
                if (cm.getName().endsWith(td.getName())) {
                    col = Controller.getInstance().getAffinity().getClassAffinity(cm.getName()).color;
                    targetClass = cm.getName();
                    for (TraceModel tm : ts.getFirstLevelTraces()) {
                        if (tm.hasClass(cm.getName())) {
                            feats.add(tm.getName());
                            if (!tm.getClass(cm.getName()).getInstancesCreated().isEmpty()) {
                                instFeats.add(tm.getName());
                            }
                        }
                    }
                    break;
                }
            }
        }

        if (col != null) {
            try {
                markers.add(new FeatureCategoryMarker(null,
                        getFoldedLineNumberByLine(target, td.getStartPosition().getLine()),
                        getFoldedLineNumberByLine(target, td.getStartPosition().getLine()),
                        feats, col));
                if (!instFeats.isEmpty()) {
                    tags.add(new LineInfoTag(getFoldedLineNumberByLine(target, td.getStartPosition().getLine()),
                            "Instantiated by:\n" + instFeats));
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public void handleMethod(Method m) {
        if (m instanceof Constructor && getSourceInfo().getConstructorDeclaration((Constructor) m) == null) {
            return;
        }
        String ms = null;
        TraceSet ts = Controller.getInstance().getTraceSet();
        int usageCount = 0;
        Set<String> feats = new HashSet<String>();
        for (TraceModel tm : ts.getFirstLevelTraces()) {
            for (ClassModel cm : tm.getClassSet()) {
                if (cm.getPackageName().equals(getSourceInfo().getPackage(m).getFullName())) {
                    if (cm.getName().endsWith(getSourceInfo().getContainingClassType(m).getName())) {
                        for (String mm : cm.getAllMethods()) {
                            boolean match = false;

                            match = compareAJMethodSignWithJavaMethodName(mm, m.getName());

                            if (match) {
                                ms = mm;
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
                if (m instanceof Constructor) {
                    if (getSourceInfo().getConstructorDeclaration((Constructor) m) != null) {
                        markers.add(new FeatureCategoryMarker(ms,
                                getFoldedLineNumberByLine(target, getSourceInfo().getConstructorDeclaration((Constructor) m).getStartPosition().getLine()),
                                getFoldedLineNumberByLine(target, getSourceInfo().getConstructorDeclaration((Constructor) m).getEndPosition().getLine()),
                                feats, mColor));
                    }
                } else {
                    markers.add(new FeatureCategoryMarker(ms,
                            getFoldedLineNumberByLine(target, getSourceInfo().getMethodDeclaration(m).getStartPosition().getLine()),
                            getFoldedLineNumberByLine(target, getSourceInfo().getMethodDeclaration(m).getEndPosition().getLine()),
                            feats, mColor));
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public void handleField(Field f) {
        if (!(f instanceof FieldSpecification)) {
            return;
        }
        String fs = null;
        TraceSet ts = Controller.getInstance().getTraceSet();
        int usageCount = 0;
        Set<String> feats = new HashSet<String>();
        for (TraceModel tm : ts.getFirstLevelTraces()) {
            for (ClassModel cm : tm.getClassSet()) {
                if (cm.getPackageName().equals(getSourceInfo().getPackage(f).getFullName())) {
                    if (cm.getName().endsWith(getSourceInfo().getContainingClassType(f).getName())) {
                        for (String mm : cm.getAllFields()) {
                            boolean match = false;

                            match = mm.endsWith(getSourceInfo().getContainingClassType(f).getName() + "." + f.getName());

                            if (match) {
                                fs = mm;
                                feats.add(tm.getName());
                                usageCount++;
                            }
                        }
                    }
                }
            }
        }

        if (usageCount != 0) {
            Color mColor = Controller.getInstance().getAffinity().getFieldAffinity(fs).color;
            try {
                markers.add(new FeatureCategoryMarker(fs,
                        getFoldedLineNumberByLine(target, ((FieldSpecification) f).getStartPosition().getLine()),
                        getFoldedLineNumberByLine(target, ((FieldSpecification) f).getEndPosition().getLine()),
                        feats, mColor));
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private boolean compareAJMethodSignWithJavaMethodName(String ajMs, String javaMn) {
        return ajMs.contains("." + javaMn + "(");
    }

    public static int getFoldedLineNumberByLine(JTextComponent document, int line) throws BadLocationException {
        int minusRows = 0;
        FoldHierarchy fh = FoldHierarchy.get(document);
        if (fh != null) {
            Fold root = fh.getRootFold();
            if (root != null) {
                for (int i = 0; i < root.getFoldCount(); i++) {
                    Fold curr = root.getFold(i);
                    if (curr.isCollapsed()) {
                        int endLine = getRawLineNumber(document, curr.getEndOffset());
                        int startLine = getRawLineNumber(document, curr.getStartOffset());
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

    @Override
    public void setScope(JTextComponent editor, List<CompilationUnit> cus) {
        this.target = editor;
        this.scope = cus;
    }
}
