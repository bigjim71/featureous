/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.sdm;

import dk.sdu.mmmi.srcUtils.nb.NBJavaSrcUtils;
import dk.sdu.mmmi.srcUtils.sdm.model.JDependency;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import recoder.CrossReferenceServiceConfiguration;
import recoder.ModelElement;
import recoder.abstraction.ClassType;
import recoder.abstraction.Method;
import recoder.convenience.TreeWalker;
import recoder.io.DefaultSourceFileRepository;
import recoder.io.SourceFileRepository;
import recoder.java.CompilationUnit;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.reference.TypeReference;
import recoder.service.DefaultErrorHandler;
import recoder.service.TypingException;
import recoder.util.FileCollector;

/**
 *
 * @author ao
 */
public class RecoderModelExtractor {

    private static StaticDependencyModel model;

    public static String[] collectJavaFiles(String dir) {
        FileCollector col = new FileCollector(dir);
        List<String> list = new ArrayList<String>();
        while (col.next(DefaultSourceFileRepository.JAVA_FILENAME_FILTER)) {
            String path;
            try {
                path = col.getFile().getCanonicalPath();
            } catch (IOException ioe) {
                path = col.getFile().getAbsolutePath();
            }
            list.add(path);
        }
        return list.toArray(new String[list.size()]);
    }

    public static void extractSdmAndRunAsync(final RunnableWithSdm runnable, final Project... optionalProj) {
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                Project mainProj = NBJavaSrcUtils.getMainProject();
                if (optionalProj.length > 0 && optionalProj[0] != null) {
                    mainProj = optionalProj[0];
                }
                if (mainProj != null) {
                    ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Extracting static model from " + mainProj.getProjectDirectory().getName());
                    progressHandle.start();
                    progressHandle.switchToIndeterminate();
                    try {
                        StaticDependencyModel sdm = extractDependencyModel(NBJavaSrcUtils.getSrcDirs(mainProj)[0], progressHandle);
                        if (sdm != null) {
                            sdm.cleanup();
                        }
                        runnable.run(sdm);

                        progressHandle.finish();
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    runnable.run(null);
                }
            }
        });
    }

    private static StaticDependencyModel extractDependencyModel(String projPath, ProgressHandle progressHandle) throws Exception {
        model = new StaticDependencyModel();
        CrossReferenceServiceConfiguration sc = new CrossReferenceServiceConfiguration();
//        sc.getProjectSettings().setProperty(ProjectSettings.JAVA_5, "true");
//        sc.getProjectSettings().setProperty(ProjectSettings.JDK1_4, "false");
        if (!sc.getProjectSettings().ensureSystemClassesAreInPath()) {
            System.out.println("System classes not in path!");
        }
        SourceFileRepository sfr = sc.getSourceFileRepository();
        sc.getProjectSettings().setErrorHandler(new DefaultErrorHandler() {

            @Override
            protected boolean isReferingUnavailableCode(ModelElement me) {
                // TODO: this needs to be refined
                return true;
            }

            @Override
            public void reportError(Exception e) {
                if (e instanceof TypingException) {
                    //warningMessage(e);
                    return;
                }
                //super.reportError(e);
            }
        });
        String[] files = collectJavaFiles(projPath);
        List<CompilationUnit> cul = sfr.getCompilationUnitsFromFiles(files);
        if (cul.size() < files.length) {
            System.err.println("Error loading java files from: " + projPath);
        }

        progressHandle.switchToDeterminate(cul.size());
//        progressHandle.start(cul.size());
        int progress = 0;
        //Do it
        for (CompilationUnit cu : cul) {
            for (TypeDeclaration ct : cu.getDeclarations()) {
                if (ct == null) {
                    continue;
                }
                String name = ct.getFullName();
                String pkg = "";
                if (ct.getPackage() != null) {
                    pkg = ct.getPackage().getName();
                }
                JType dmType = model.getOrAddPackageByName(pkg).getOrAddTypeByQualName(name);

                dmType.setPublicAccess(ct.isPublic());

                dmType.setTopLevel(!ct.isInner());

                for (TypeDeclaration td : ct.getTypes()) {
                    if (td.getFullName().equals(ct.getFullName())) {
                        continue;
                    }
                    JType dmNestedType = model.getOrAddPackageByName(pkg).getOrAddTypeByQualName(td.getFullName());
                    dmNestedType.setTopLevel(false);
                    dmNestedType.setProcessed(true);
                    dmType.getEnclosedTypes().add(dmNestedType);
                }


                dmType.setFieldCount(ct.getFields().size());
                dmType.setMethodCount(ct.getMethods().size());
                dmType.setConstructorCount(ct.getConstructors().size());
                int accessors = 0;
                for (Method m : ct.getMethods()) {
                    if (m.getName().startsWith("get") && m.getSignature().size() == 0) {
                        accessors++;
                    } else if (m.getName().startsWith("set") && m.getSignature().size() == 1) {
                        accessors++;
                    }
                }
                dmType.setEstAccessorCount(accessors);
                TreeWalker w = new TreeWalker(ct);
                while (w.next()) {
                    if (w.getProgramElement() instanceof TypeReference) {
                        TypeReference r = (TypeReference) w.getProgramElement();
                        if (sc.getSourceInfo().getType(r) instanceof ClassType) {
                            ClassType cc = (ClassType) sc.getSourceInfo().getType(r);
                            String typeName = cc.getFullName();
                            String pkgName = "";
                            if (cc.getPackage() != null) {
                                pkgName = cc.getPackage().getName();
                            }
                            JType dmRefType = model.getOrAddPackageByName(pkgName).getOrAddTypeByQualName(typeName);
                            JDependency dmDep = new JDependency(dmRefType);
                            dmType.getDependencies().add(dmDep);
                        }
                    }
                }
                dmType.setInterfaceType(ct.isInterface());

                //TODO handle invocations at some point
//                w = new TreeWalker(ct);
//                while(w.next()){
//                    if(w.getProgramElement() instanceof MethodInvocation){
//                        MethodInvocation r = (MethodInvocation)w.getProgramElement();
//                        if(sc.getSourceInfo().getMethod(r.get)getType(r) instanceof ClassType){
//                            ClassType cc = (ClassType)sc.getSourceInfo().getType(r);
//                            String typeName = cc.getFullName();
//                            String pkgName = "";
//                            if(cc.getPackage()!=null){
//                                pkgName = cc.getPackage().getName();
//                            }
//                            JType dmRefType = model.getOrAddPackageByName(pkg).getOrAddTypeByQualName(typeName);
//                            JDependency dmDep = new JDependency(dmRefType);
//                            dmType.getDependencies().add(dmDep);
//                        }
//                    }
//                }

                dmType.setProcessed(true);
            }
            progressHandle.progress(++progress);
        }

        //Post
//        Properties props = sc.getProjectSettings().getProperties();
//        String key;
//        key = "input.path";
//        System.out.println(key + "=" + props.getProperty(key));
//        key = "error.threshold";
//        System.out.println(key + "=" + props.getProperty(key));
//        key = "jdk1.4";
//        System.out.println(key + "=" + props.getProperty(key));
//        System.out.println();
        // System.out.println("\nAnalyzing System...");
//	sc.getChangeHistory().updateModel();
//	System.out.println("\nFiles...");
//	System.out.println(((DefaultSourceFileRepository)sc.getSourceFileRepository()).information());
//	System.out.println(((DefaultClassFileRepository)sc.getClassFileRepository()).information());

//        System.out.println("\nNames...");
//        System.out.println(((DefaultNameInfo) sc.getNameInfo()).information());
//	System.out.println("\nReferences...");
//	System.out.println(((DefaultCrossReferenceSourceInfo)sc.getCrossReferenceSourceInfo()).information());
//	System.out.println();

        long totalMem = Runtime.getRuntime().totalMemory();
        long usedMem = recoder.util.Debug.getUsedMemory();
//        System.out.println("Memory used: " + usedMem
//                + " (total: " + totalMem + ")");

        model.cleanup();
//        System.out.println("Model extracted [P, T, A]: " + model.getPackages().size() + ", " + model.getTopLevelTypesCount() + ", " + model.getAllTypesCount());

        if (model.getPackages().isEmpty()) {
            return null;
        }
        return model;
    }

    public static interface RunnableWithSdm {

        void run(StaticDependencyModel sdmOrNull);
    }
}
