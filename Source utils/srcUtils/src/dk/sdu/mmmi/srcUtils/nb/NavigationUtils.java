/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.nb;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.java.JavaKit;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;

/**
 *
 * @author ao
 */
public class NavigationUtils {

    public static JComponent getEditorForClass(String pkgName, String className) throws FileStateInvalidException, IOException {
        Project ownerProj = null;
        FileObject cfo = null;
        final String onlyClass = className.replace(pkgName + ".", "");
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            Enumeration<? extends FileObject> fos = p.getProjectDirectory().getChildren(true);
            while (fos.hasMoreElements()) {
                FileObject fo = fos.nextElement();
                if (!fo.isFolder() && fo.hasExt("java") && fo.getName().equals(onlyClass)
                        && fo.getPath().replace("/", ".").replace("\\", ".").contains(className)) {
                    ownerProj = p;
                    cfo = fo;
                    break;
                }
            }
            if (ownerProj != null) {
                break;
            }
        }
        if (cfo == null) {
            return null;
        }

        Document doc = getDocument(cfo);
        BaseKit javaKit = BaseKit.getKit(JavaKit.class);
        javaKit.createDefaultDocument();
        JEditorPane pane = new JEditorPane();
        pane.setEditorKit(javaKit);
        pane.setDocument(doc);
        EditorUI editorUI = Utilities.getEditorUI(pane);
        
        return editorUI.getExtComponent();
    }

    public static Document getDocument(FileObject file) throws IOException {
        DataObject od = DataObject.find(file);
        EditorCookie ec = (EditorCookie) od.getCookie(EditorCookie.class);

        if (ec != null) {
            Document doc = ec.openDocument();

            doc.putProperty(Language.class, JavaTokenId.language());
            doc.putProperty("mimeType", "text/x-java");

            return doc;
        } else {
            return null;
        }
    }

    public static void openClass(String pkgName, String className) throws IOException {
        /**
         * If buggy for a long time, consider using recoder +
         *
         * File f = ...;
         *   int lineNumber = ...;
         *   FileObject fobj = FileUtil.toFileObject(f);
         *   DataObject dobj = null;
         *   try {
         *       dobj = DataObject.find(fobj);
         *   } catch (DataObjectNotFoundException ex) {
         *       ex.printStackTrace();
         *   }
         *   if (dobj != null)
         *       LineCookie lc = (LineCookie) dobj .getCookie(LineCookie.class);
         *       if (lc == null) { return;}
         *       Line l = lc.getLineSet().getOriginal(lineNumber);
         *       l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
         *   }
         *
         */
        Project ownerProj = null;
        FileObject cfo = null;
        final String onlyClass = className.replace(pkgName + ".", "");
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            Enumeration<? extends FileObject> fos = p.getProjectDirectory().getChildren(true);
            while (fos.hasMoreElements()) {
                FileObject fo = fos.nextElement();
                if (!fo.isFolder() && fo.hasExt("java") && fo.getName().equals(onlyClass)
                        && fo.getPath().replace("/", ".").replace("\\", ".").contains(className)) {
                    ownerProj = p;
                    cfo = fo;
                    break;
                }
            }
            if (ownerProj != null) {
                break;
            }
        }
        if (cfo == null) {
            return;
        }
        JavaSource src = JavaSource.forFileObject(cfo);
        if (src == null) {
            //TODO: a hack for now
            return;
        }

        src.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController p) throws Exception {
                try {
                    p.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    for (TypeElement te : p.getTopLevelElements()) {
                        if (te.getSimpleName().contentEquals(onlyClass)) {
                            ElementOpen.open(p.getClasspathInfo(), te);
                            break;
                        }
                    }
                } catch (ClassCastException cce) {
                    System.err.println("Doesn't work for now due to platform bug #192543");
                }
            }
        }, true);
    }

    public static void openMethod(String pkgName, String className, final String methodName) throws Exception {
        Project ownerProj = null;
        FileObject cfo = null;
        final String onlyClass = className.replace(pkgName + ".", "");
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            Enumeration<? extends FileObject> fos = p.getProjectDirectory().getChildren(true);
            while (fos.hasMoreElements()) {
                FileObject fo = fos.nextElement();
                if (!fo.isFolder() && fo.hasExt("java") && fo.getName().equals(onlyClass)
                        && fo.getPath().replace("/", ".").replace("\\", ".").contains(className)) {
                    ownerProj = p;
                    cfo = fo;
                    break;
                }
            }
            if (ownerProj != null) {
                break;
            }
        }

        if (cfo == null) {
            return;
        }
        JavaSource src = JavaSource.forFileObject(cfo);
        if (src == null) {
            //TODO: a hack for now
            return;
        }

        src.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController p) throws Exception {

                try {
                    p.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    for (TypeElement el : p.getTopLevelElements()) {
                        if (el.getSimpleName().contentEquals(onlyClass)) {
                            Iterator<? extends Element> i = p.getElementUtilities().getMembers(el.asType(), new ElementAcceptor() {

                                @Override
                                public boolean accept(Element e, TypeMirror type) {
                                    if (methodName.contains("." + e.getSimpleName() + "(")) {
                                        return true;
                                    }
                                    return false;
                                }
                            }).iterator();

                            if (i.hasNext()) {
                                ElementOpen.open(p.getClasspathInfo(), i.next());
                            }
                        }
                    }
                } catch (ClassCastException cce) {
                    System.err.println("Doesn't work for now due to platform bug #192543");
                }
            }
        }, true);
    }
}
