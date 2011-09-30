/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.transform;

import dk.sdu.mmmi.featureous.core.ui.OutputUtil;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Set;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.java.editor.imports.JavaFixAllImports;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.ProgressListener;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.openide.actions.SaveAllAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author ao
 */
public class MoveClassNB {

    private MoveRefactoring refactoring;
    private final String targetPkg;
    private final FileObject rootFolder;
    private final DataObject javaObject;
    private final RequestProcessor rp;

    public MoveClassNB(DataObject javaObject, String targetPkg, FileObject rootFolder, Collection<TreePathHandle> handles, RequestProcessor rp) {
        this.rp = rp;
        this.targetPkg = targetPkg;
        this.rootFolder = rootFolder;
        this.javaObject = javaObject;
        this.refactoring = new MoveRefactoring(Lookups.fixed(javaObject.getPrimaryFile()));
        this.refactoring.getContext().add(RetoucheUtils.getClasspathInfoFor(javaObject.getPrimaryFile()));
    }

    public Problem setParameters() {
        URL url = URLMapper.findURL(rootFolder, URLMapper.EXTERNAL);
        try {
            refactoring.setTarget(Lookups.singleton(new URL(url.toExternalForm() + URLEncoder.encode(targetPkg.replace('.', '/'), "utf-8")))); // NOI18N
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }

        return refactoring.checkParameters();
    }
    RefactoringSession rs = null;

    public void setRs(RefactoringSession rs) {
        this.rs = rs;
    }

    public RefactoringSession getRs() {
        return rs;
    }

    public void refactor() {

        try {
            rp.post(
                    new Runnable() {

                        public void run() {
                            try {
                                refactoring.preCheck();
                            } catch (RuntimeException e) {
                                throw e;
                            }
                            setRs(RefactoringSession.create("Move class"));
                            try {
                                refactoring.prepare(getRs());
                            } finally {
                            }

                            RefactoringSession session = getRs();
                            if (session != null) {
                                session.addProgressListener(new ProgressListener() {

                                    @Override
                                    public void start(ProgressEvent pe) {
                                    }

                                    @Override
                                    public void step(ProgressEvent pe) {
                                    }

                                    @Override
                                    public void stop(ProgressEvent pe) {
                                        OutputUtil.log("Done moving classes for " + javaObject.getName());
                                    }
                                });
                                session.doRefactoring(true);
                            }

                        }
                    });
//                    });
        } catch (Exception ex) {
        }
    }

    public static void postImportsFixTask(final Set<DataObject> daos, RequestProcessor rp) {
        rp.post(
                new Runnable() {

                    public void run() {
                        OutputUtil.log("Fixing imports");
                        for (DataObject dao : daos) {
                            FileObject fo = dao.getPrimaryFile();
                            JavaFixAllImports.getDefault().fixAllImports(fo);
                            JavaFixAllImports.getDefault().fixAllImports(fo);
                            OutputUtil.log("Fixing imports for: " + fo.getPath());
                        }

                        SystemAction.get(SaveAllAction.class).performAction();

                        OutputUtil.log("Done fixing imports");
                    }
                });
    }
}
