/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils;

import dk.sdu.mmmi.srcUtils.nb.NBJavaSrcUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.project.Project;
import org.openide.windows.IOProvider;
import recoder.CrossReferenceServiceConfiguration;
import recoder.ModelElement;
import recoder.ParserException;
import recoder.ServiceConfiguration;
import recoder.io.PropertyNames;
import recoder.io.SourceFileRepository;
import recoder.java.CompilationUnit;
import recoder.kit.Transformation;
import recoder.kit.TwoPassTransformation;
import recoder.service.DefaultErrorHandler;
import recoder.service.UnresolvedBytecodeReferenceException;
import recoder.service.UnresolvedReferenceException;

/**
 *
 * @author ao
 */
public class AnalysisUtils {

    private static final String sep = System.getProperty("file.separator");

    public static void analyzeCompiledProject(Project p, SrcAnalysis sa) throws Exception {

        CrossReferenceServiceConfiguration crsc = new CrossReferenceServiceConfiguration();

        // TODO we can detect this later is needed...
        File srcDir = new File(p.getProjectDirectory().getPath() + sep +"src");
        File libDir = new File(p.getProjectDirectory().getPath() + sep+ "lib");
        if (!srcDir.isDirectory() || !libDir.isDirectory()) {
            throw new RuntimeException("src/lib dirs config problem");
        }
//        if (p instanceof J2SEProject) {
//            J2SEProject jproj = (J2SEProject) p;
//            FileObject buildScript = J2SEProjectUtil.getBuildXml(jproj);
//        }

        String[] libs = NBJavaSrcUtils.findJars(libDir.getAbsolutePath());

        String inputPath = srcDir.getAbsolutePath();
        for (String s : libs) {
            inputPath += File.pathSeparator + s;
        }

        crsc.getProjectSettings().setProperty("PROJ_DIRECTORY", p.getProjectDirectory().getPath());
        crsc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, inputPath);
        crsc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, p.getProjectDirectory().getPath());
        crsc.getProjectSettings().setProperty(PropertyNames.JAVA_5, "true");
        crsc.getProjectSettings().setProperty(PropertyNames.JDK1_4, "false");
        crsc.getProjectSettings().setProperty(PropertyNames.TABSIZE, "4");
        if (!crsc.getProjectSettings().ensureSystemClassesAreInPath()) {
            System.out.println("\tWarning: Cannot find system classe (rt.jar)");
            System.out.println("\tThis will likely cause an error, unless you are");
            System.out.println("\ttrying to transform the JDK itself. Please make sure");
            System.out.println("\tthat java.home is set, or specify an rt.jar in the");
            System.out.println("\tinput classpath.");
        }
        crsc.getProjectSettings().ensureExtensionClassesAreInPath();
        SourceFileRepository sfr = crsc.getSourceFileRepository();

        try {
//            sfr.getAllCompilationUnitsFromPath();
            List<CompilationUnit> cus = sfr.getCompilationUnitsFromFiles(NBJavaSrcUtils.findJavaFiles(srcDir.getAbsolutePath()));
            crsc.getChangeHistory().updateModel();
            // consider using: org.openide.text.NbDocument.runAtomic(doc, runnable)
            sa.setScope(cus);
            ((Transformation) sa).setServiceConfiguration(crsc);
            ((Transformation) sa).execute();
        } catch (Exception ex) {
            IOProvider.getDefault().getStdOut().println(ex.getMessage());
        }
    }

    public static void analyzeParsedProject(Project p, SrcAnalysis sa) throws Exception {

        CrossReferenceServiceConfiguration crsc = new CrossReferenceServiceConfiguration();

        // TODO we can detect this later is needed...
        File srcDir = new File(p.getProjectDirectory().getPath() + sep + "src");
        if (!srcDir.isDirectory()) {
            throw new RuntimeException("src/lib dirs config problem");
        }

        String inputPath = srcDir.getAbsolutePath();

        SourceFileRepository sfr = crsc.getSourceFileRepository();

        setupErrorHandling(crsc);

        if (!crsc.getProjectSettings().ensureSystemClassesAreInPath()) {
            System.out.println("\tWarning: Cannot find system classe (rt.jar)");
            System.out.println("\tThis will likely cause an error, unless you are");
            System.out.println("\ttrying to transform the JDK itself. Please make sure");
            System.out.println("\tthat java.home is set, or specify an rt.jar in the");
            System.out.println("\tinput classpath.");
        }

        List<CompilationUnit> cus = sfr.getCompilationUnitsFromFiles(NBJavaSrcUtils.findJavaFiles(srcDir.getAbsolutePath()));
        // consider using: org.openide.text.NbDocument.runAtomic(doc, runnable)
        ((Transformation) sa).setServiceConfiguration(crsc);
        sa.setScope(cus);
        ((Transformation) sa).execute();
    }

    public static void transformCompiledEditor(JTextComponent editor, Project p, EditorAnalysis sa) throws Exception {

        CrossReferenceServiceConfiguration crsc = new CrossReferenceServiceConfiguration();

        // TODO we can detect this later is needed...
        File srcDir = new File(p.getProjectDirectory().getPath() + sep+ "src");
        File libDir = new File(p.getProjectDirectory().getPath() + sep+ "lib");
        if (!srcDir.isDirectory() || !libDir.isDirectory()) {
            throw new RuntimeException("src/lib dirs config problem");
        }
//        if (p instanceof J2SEProject) {
//            J2SEProject jproj = (J2SEProject) p;
//            FileObject buildScript = J2SEProjectUtil.getBuildXml(jproj);
//        }

        String[] libs = NBJavaSrcUtils.findJars(libDir.getAbsolutePath());

        String inputPath = srcDir.getAbsolutePath();
        for (String s : libs) {
            inputPath += File.pathSeparator + s;
        }

        crsc.getProjectSettings().setProperty("PROJ_DIRECTORY", p.getProjectDirectory().getPath());
        crsc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, inputPath);
        crsc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, p.getProjectDirectory().getPath());
        crsc.getProjectSettings().setProperty(PropertyNames.JAVA_5, "true");
        crsc.getProjectSettings().setProperty(PropertyNames.JDK1_4, "false");
        crsc.getProjectSettings().setProperty(PropertyNames.TABSIZE, "4");
        if (!crsc.getProjectSettings().ensureSystemClassesAreInPath()) {
            System.out.println("\tWarning: Cannot find system classe (rt.jar)");
            System.out.println("\tThis will likely cause an error, unless you are");
            System.out.println("\ttrying to transform the JDK itself. Please make sure");
            System.out.println("\tthat java.home is set, or specify an rt.jar in the");
            System.out.println("\tinput classpath.");
        }
        crsc.getProjectSettings().ensureExtensionClassesAreInPath();
        SourceFileRepository sfr = crsc.getSourceFileRepository();

        try {
            List<CompilationUnit> cus = sfr.getCompilationUnitsFromFiles(NBJavaSrcUtils.findJavaFiles(srcDir.getAbsolutePath()));
            crsc.getChangeHistory().updateModel();
            // consider using: org.openide.text.NbDocument.runAtomic(doc, runnable)
            sa.setScope(editor, cus);
            ((Transformation) sa).setServiceConfiguration(crsc);
            ((Transformation) sa).execute();
        } catch (Exception ex) {
            IOProvider.getDefault().getStdOut().println(ex.getMessage());
        }
    }

//    public static void analyzeParsedActiveEditorContent(SrcAnalysis sa) throws ParserException {
//        JTextComponent editor = Utilities.getFocusedComponent();
//        analyzeParsedEditorContent(editor, sa);
//    }
    public static void analyzeParsedEditorContent(JTextComponent editor, EditorAnalysis sa) throws ParserException {
        CrossReferenceServiceConfiguration sc = new CrossReferenceServiceConfiguration();
//        sc.getProjectSettings().setProperty(ProjectSettings.JAVA_5, "true");
//        sc.getProjectSettings().setProperty(ProjectSettings.JDK1_4, "false");
        if (!sc.getProjectSettings().ensureSystemClassesAreInPath()) {
            throw new RuntimeException("System classes are not in path for Recoder library!");
        }
        SourceFileRepository sfr = sc.getSourceFileRepository();

        setupErrorHandling(sc);

        String text;
        try {
            if (editor.getDocument().getLength() > 0) {
                text = editor.getDocument().getText(0, editor.getDocument().getLength());
                CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(text);
                List<CompilationUnit> cus = new ArrayList<CompilationUnit>();
                cus.add(cu);
                text = null;
                sa.setScope(editor, cus);
                ((Transformation) sa).setServiceConfiguration(sc);
                ((Transformation) sa).execute();
            }
        } catch (Exception ex) {
            //Silent
        }
    }

    public static void setupErrorHandling(final ServiceConfiguration sc) {
        sc.getProjectSettings().setErrorHandler(new DefaultErrorHandler() {

            @Override
            protected boolean isReferingUnavailableCode(ModelElement me) {
                // TODO: this needs to be refined
                return true;
            }

            @Override
            public void reportError(Exception e) {
                if (!(e instanceof UnresolvedReferenceException)
                        && !(e instanceof UnresolvedBytecodeReferenceException)) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static ProgressHandle registerProgressHandle(String dispName) {
        ProgressExposer ex = new ProgressExposer();
        ProgressUtils.showProgressDialogAndRun(ex, dispName, false);
        return ex.ph;
    }

    private static class ProgressExposer implements ProgressRunnable<Object> {

        public ProgressHandle ph;

        @Override
        public Object run(ProgressHandle ph) {
            this.ph = ph;
            return null;
        }
    }
}
