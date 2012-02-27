package dk.sdu.mmmi.srcUtils;

import dk.sdu.mmmi.srcUtils.nb.NBJavaSrcUtils;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.openide.windows.IOProvider;
import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.io.ProjectSettings;
import recoder.io.PropertyNames;
import recoder.io.SourceFileRepository;
import recoder.java.CompilationUnit;
import recoder.kit.Transformation;

/**
 *
 * @author ao
 */
public class TransformUtils {

    public static void transform(List<Transformation> transforms, String inDir, String outDir, String libDir) {
        transform(transforms, inDir, outDir, libDir, true);
    }

    public static void analyze(List<Transformation> transforms, String inDir, String outDir, String libDir) {
        transform(transforms, inDir, outDir, libDir, false);
    }

    private static void transform(List<Transformation> transforms, String inDir, String outDir, String libDir, boolean writeOutput) {
        if (!new File(inDir).isDirectory() || !new File(libDir).isDirectory()) {
            throw new RuntimeException("src/lib dirs config problem");
        }

        if (!(new File(outDir).isDirectory())) {
            if (!new File(outDir).mkdirs()) {
                throw new RuntimeException("ERROR: specified output-path is not a directory"
                        + " and could not be created either: " + outDir);
            }
        }

        CrossReferenceServiceConfiguration crsc = new CrossReferenceServiceConfiguration();

        String[] libs = NBJavaSrcUtils.findJars(new File(libDir).getAbsolutePath());
        String inputPath = inDir;
        for (String s : libs) {
            inputPath += File.pathSeparator + s;
        }

        crsc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, inputPath);
        crsc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, outDir);

        configureRecoder(crsc.getProjectSettings());

        SourceFileRepository sfr = crsc.getSourceFileRepository();

        try {
            sfr.getAllCompilationUnitsFromPath();
            sfr.getCompilationUnits();
            crsc.getChangeHistory().updateModel();
        } catch (ParserException ex) {
            IOProvider.getDefault().getStdOut().println(ex.getMessage());
        }

        List<CompilationUnit> cul = sfr.getCompilationUnits();
        for (CompilationUnit cu : cul) {
            // just to make sure...
            cu.validateAll();
        }

        //Transform part

        for (Transformation transform : transforms) {
            transform.setServiceConfiguration(crsc);
            transform.execute();
            crsc.getChangeHistory().updateModel();
        }

        crsc.getChangeHistory().updateModel();

        if (writeOutput) {
            try {
                sfr.printAll(true);
            } catch (IOException ex) {
                IOProvider.getDefault().getStdOut().println(ex.getMessage());
            }
        }
    }

    private static void configureRecoder(ProjectSettings s) {
        s.setProperty(PropertyNames.JAVA_5, "true");
//        s.setProperty(PropertyNames.JDK1_4, "false");
        s.setProperty(PropertyNames.ALIGN_LABELS, "true");
        s.setProperty(PropertyNames.TABSIZE, "4");
        if (!s.ensureSystemClassesAreInPath()) {
            System.out.println("\tWarning: Cannot find system classe (rt.jar)");
            System.out.println("\tThis will likely cause an error, unless you are");
            System.out.println("\ttrying to transform the JDK itself. Please make sure");
            System.out.println("\tthat java.home is set, or specify an rt.jar in the");
            System.out.println("\tinput classpath.");
        }
        s.ensureExtensionClassesAreInPath();
    }
}
