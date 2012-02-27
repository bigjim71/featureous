/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.ui.OutputUtil;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.featureous.metrics.MetricAggregator;
import dk.sdu.mmmi.featureous.metrics.Result;
import dk.sdu.mmmi.featureous.remodularization.logic.MainRemodularization;
import dk.sdu.mmmi.featureous.remodularization.logic.objectives.CohesionObjective;
import dk.sdu.mmmi.featureous.remodularization.logic.objectives.CouplingObjective;
import dk.sdu.mmmi.featureous.remodularization.logic.objectives.FeatsPerPkgObjective;
import dk.sdu.mmmi.featureous.remodularization.logic.objectives.PkgsPerFeatureObjective;
import dk.sdu.mmmi.featureous.remodularization.logic.objectives.ScatteringObjective;
import dk.sdu.mmmi.featureous.remodularization.logic.objectives.StableAbstractionsPrincipleObjective;
import dk.sdu.mmmi.featureous.remodularization.logic.objectives.StableDependenciesPrincipleObjective;
import dk.sdu.mmmi.featureous.remodularization.logic.objectives.TanglingObjective;
import dk.sdu.mmmi.featureous.remodularization.spi.RemodularizationObjectiveProvider;
import dk.sdu.mmmi.featureous.remodularization.transform.AnnotateClass;
import dk.sdu.mmmi.featureous.remodularization.transform.IncreaseVisibilityToPublic;
import dk.sdu.mmmi.featureous.remodularization.transform.MoveClassNB;
import dk.sdu.mmmi.featureous.remodularization.transform.MoveClassRecoder;
import dk.sdu.mmmi.featureous.remodularization.transform.ReadAnnotationValue;
import dk.sdu.mmmi.featureous.remodularization.workbench.RemodularizationWorkbench;
import dk.sdu.mmmi.featureous.remodularization.workbench.infrastructure.RestructuringListener;
import dk.sdu.mmmi.srcUtils.TransformUtils;
import dk.sdu.mmmi.srcUtils.nb.NBJavaSrcUtils;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import dk.sdu.mmmi.srcUtils.sdm.model.Util;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ProjectXMLManager.CyclicDependencyException;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import recoder.kit.Transformation;

/**
 *
 * @author andrzejolszak
 */
public class PostView extends JPanel {

    private MetricAggregator metricAggregator;
    private MetricAggregator remodularizationMetricAggregator;
    private final String sep = System.getProperty("file.separator");
    private final RequestProcessor rp = new RequestProcessor("featureousRemodularization", 1);
    private JCheckBox asModules;
    private StaticDependencyModel newSdm;
    private final Project proj;
    private final String srcDir;
    private final String backupDir;
    private final StaticDependencyModel orgSdm;
    private final Set<RemodularizationObjectiveProvider> selectedProviders;
    private RemodularizationWorkbench postWorkbench;
    private final JTabbedPane tabbedPane;
    private static int designNumber = 100;
    private final int population;
    private final AtomicInteger iterations;
    private final float mutation;

    public PostView(final boolean factorSingle, final Project proj, final String srcDir, final String backupDir,
            final StaticDependencyModel orgSdm, final Set<RemodularizationObjectiveProvider> selectedProviders,
            JTabbedPane tabbedPane, AtomicInteger iterations, int population, float mutation, MetricAggregator ma,
            MetricAggregator rma) {
        super(new BorderLayout());
        this.metricAggregator = ma;
        this.remodularizationMetricAggregator = rma;
        this.proj = proj;
        this.srcDir = srcDir;
        this.backupDir = backupDir;
        this.orgSdm = orgSdm;
        this.selectedProviders = selectedProviders;
        this.tabbedPane = tabbedPane;
        this.iterations = iterations;
        this.population = population;
        this.mutation = mutation;
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Featureous: constructing remodularization");

                // remodularize
                newSdm = computeNewSdm(progressHandle, factorSingle);

                if (newSdm != null) {
                    newSdm.cleanup();
                    postWorkbench = new RemodularizationWorkbench(newSdm, true);
                    postWorkbench.recolorPkgsBasedOnChildAffinity();

                    add(postWorkbench, BorderLayout.CENTER);

                    setupPostToolbar(postWorkbench, new RecoderAnnotationListener(), new RecoderRestructuringListener());

                    printDesignComparison(metricAggregator);

                    OutputUtil.log("New structure: \n" + postWorkbench.getCurrentClassToPkgMap().toString());

                } else {
                    OutputUtil.log("Problem generating new static dependency model for the project. "
                            + "Remodularization aborted.");
                }
            }
        });

    }

    private void printDesignComparison(MetricAggregator ma) {
        Set<TraceModel> tms = Controller.getInstance().getTraceSet().getFirstLevelTraces();
        StringBuilder sb = new StringBuilder("---[Designs comparison for remodularization " + designNumber + " : ]---\n");
        sb.append("metric;orgVal;newVal;\n");

        Collection<? extends RemodularizationObjectiveProvider> objectives = Lookup.getDefault().lookupAll(RemodularizationObjectiveProvider.class);
        List<RemodularizationObjectiveProvider> rops = new ArrayList<RemodularizationObjectiveProvider>(objectives);
        Collections.sort(rops, new Comparator<RemodularizationObjectiveProvider>() {

            @Override
            public int compare(RemodularizationObjectiveProvider t, RemodularizationObjectiveProvider t1) {
                return t.getObjectiveName().compareTo(t1.getObjectiveName());
            }
        });

        for (RemodularizationObjectiveProvider rop : rops) {
            AbstractMetric obj1 = rop.createObjective();
            float resOrg = obj1.calculateAndReturnRes(tms, orgSdm);

            AbstractMetric obj2 = rop.createObjective();
            float resNew = obj2.calculateAndReturnRes(tms, newSdm);

            sb.append(rop.getObjectiveName() + ";"
                    + resOrg + ";"
                    + resNew + ";\n");

            ma.insertResultForSystem(resNew, "ver" + designNumber, rop.getObjectiveName());
            for (Result r : obj2.getResults()) {
                ma.insertResult(r.value, "ver" + designNumber, rop.getObjectiveName(), r.name);
            }
        }

//        OutputUtil.log(sb.toString());
    }

    @Deprecated
    private class RemodularizationNBActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    final ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Featureous: restructuring source");
                    progressHandle.start();
                    progressHandle.switchToIndeterminate();
                    File srcFile = new File(srcDir);
                    File backup = new File(backupDir);
                    if (!backup.exists()) {
                        try {
                            backup.mkdir();
                            OutputUtil.log("Backing up the original sources to: " + backupDir);
                            copyFiles(srcFile, backup);
                            OutputUtil.log("Done.");
                        } catch (IOException ex) {
                            OutputUtil.log(ex.getMessage());
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    Map<String, String> classToPkg = postWorkbench.getCurrentClassToPkgMapNotPresentInSdm(orgSdm);
                    try {
                        OutputUtil.log("Restructuring sources");
                        Set<DataObject> daos = doRefactor(classToPkg);

                        rp.post(new Runnable() {

                            @Override
                            public void run() {
                                progressHandle.setDisplayName("Featureous: fixing imports");
                            }
                        });
                        MoveClassNB.postImportsFixTask(daos, rp);

                        if (asModules.isSelected()) {
                            rp.post(new Runnable() {

                                @Override
                                public void run() {
                                    progressHandle.setDisplayName("Featureous: moving into NB modules");
                                }
                            });

                            Set<String> featureSpecPackages = postWorkbench.getCurrentSingleFeatPkgs();
                            Set<String> corePackages = new HashSet<String>();
                            corePackages.addAll(postWorkbench.getCurrentClassToPkgMap().values());
                            corePackages.removeAll(featureSpecPackages);
                            doEncapsulateAsModules(featureSpecPackages, corePackages, srcDir);
                        }
                    } catch (DataObjectNotFoundException ex) {
                        OutputUtil.log(ex.getMessage());
                        Exceptions.printStackTrace(ex);
                    }

                    rp.post(new Runnable() {

                        @Override
                        public void run() {
                            progressHandle.finish();
                        }
                    });
                }
            });
        }
    }

    private class RecoderAnnotationListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    final ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Featureous: annotating source");
                    progressHandle.start();
                    progressHandle.switchToIndeterminate();
                    final Map<String, String> classToPkg = postWorkbench.getCurrentClassToPkgMap();
                    final String libDir = proj.getProjectDirectory().getPath() + sep + "lib";
                    OutputUtil.log("Annotating sources");

                    rp.post(new Runnable() {

                        @Override
                        public void run() {
                            List<Transformation> trans = new ArrayList<Transformation>();
                            for (Map.Entry<String, String> cToP : classToPkg.entrySet()) {
                                trans.add(new AnnotateClass(cToP.getKey(), "OrgPkg", cToP.getValue()));
                            }
                            TransformUtils.transform(trans, srcDir, srcDir + "_annotated", libDir);
                        }
                    });

                    rp.post(new Runnable() {

                        @Override
                        public void run() {
                            progressHandle.finish();
                        }
                    });
                }
            });
        }
    }

    private class RecoderRestructuringListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    final ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Featureous: restructuring source");
                    progressHandle.start();
                    progressHandle.switchToIndeterminate();
                    final Map<String, String> classToPkg = postWorkbench.getCurrentClassToPkgMap();
                    final String libDir = proj.getProjectDirectory().getPath() + sep + "lib";

                    OutputUtil.log("Restructuring sources");

                    rp.post(new Runnable() {

                        @Override
                        public void run() {

                            List<Transformation> trans = new ArrayList<Transformation>();
                            for (Map.Entry<String, String> cToP : classToPkg.entrySet()) {
                                trans.add(new ReadAnnotationValue(cToP.getKey(), "OrgPkg"));
                            }

                            TransformUtils.analyze(trans, srcDir + "_annotated", srcDir + "_annotated", libDir);

                            Map<String, String> cToPRead = new HashMap<String, String>();

                            for (Transformation t : trans) {
                                ReadAnnotationValue av = (ReadAnnotationValue) t;
                                cToPRead.put(av.getTypeName(), av.getValue());
                            }

                            for (Map.Entry<String, String> cToP : cToPRead.entrySet()) {
                                trans.add(new AnnotateClass(cToP.getKey(), "OrgPkg",
                                        dk.sdu.mmmi.srcUtils.sdm.model.Util.getTypesPackage(cToP.getKey(), orgSdm).getQualName()));
                                trans.add(new MoveClassRecoder(cToP.getKey(), cToP.getValue()));
                            }
                            trans.add(new IncreaseVisibilityToPublic());
                            TransformUtils.transform(trans, srcDir + "_annotated", srcDir + "_restructured", libDir);
                        }
                    });

                    if (asModules.isSelected()) {
                        rp.post(new Runnable() {

                            @Override
                            public void run() {
                                progressHandle.setDisplayName("Featureous: moving into NB modules");
                            }
                        });

                        Set<String> featureSpecPackages = postWorkbench.getCurrentSingleFeatPkgs();
                        Set<String> corePackages = new HashSet<String>();
                        corePackages.addAll(postWorkbench.getCurrentClassToPkgMap().values());
                        corePackages.removeAll(featureSpecPackages);
                        doEncapsulateAsModules(featureSpecPackages, corePackages, srcDir + "_restructured");
                    }

                    rp.post(new Runnable() {

                        @Override
                        public void run() {
                            progressHandle.finish();
                        }
                    });
                }
            });
        }
    }

    private StaticDependencyModel buildRestructuredSdm(Map<String, String> cToP, StaticDependencyModel orgSdm) {
        StaticDependencyModel newDm = new StaticDependencyModel();

        ArrayList<JType> added = new ArrayList<JType>();
        for (String t : orgSdm.getTopLevelTypes()) {
            JType type = orgSdm.getTypeByNameOrNull(t);
            Util.deepInsertType(newDm.getOrAddPackageByName(cToP.get(t)), type, added);
        }

        return newDm;
    }

    private Set<DataObject> doRefactor(Map<String, String> classToPkg) throws DataObjectNotFoundException {
        String[] files = NBJavaSrcUtils.findJavaFiles(srcDir);
        int prog = 0;
        Set<DataObject> daos = new HashSet<DataObject>();
        for (String file : files) {
            FileObject fo = FileUtil.toFileObject(new File(file));
            DataObject dao = DataObject.find(fo);
            daos.add(dao);
            String destPkg = findDestPkg(classToPkg, file);
            if (destPkg == null) {
                //Apparently this class does not have to be moved
                continue;
            }
            MoveClassNB moveNb = new MoveClassNB(dao, destPkg, FileUtil.toFileObject(new File(srcDir)), new HashSet<TreePathHandle>(), rp);
            moveNb.setParameters();
            moveNb.refactor();
        }

        return daos;
    }

    private void doEncapsulateAsModules(final Set<String> featurePkgs, final Set<String> corePkgs, final String newSrcDir) {

        rp.post(new Runnable() {

            public void run() {
                try {
                    String projDir = proj.getProjectDirectory().getPath();

                    final SuiteProject suite = NbModulesAPI.createSuite(projDir + sep + "modules" + sep + "Suite");

                    //Create a single module for all core packages
                    NbModuleProject coreModule = NbModulesAPI.createModule(
                            corePkgs.iterator().next().split("\\.")[0],
                            "Core", projDir + sep + "modules" + sep + "Core", suite);
                    for (String c : corePkgs) {
                        String dest = sep + c.replace(".", sep);
                        copyFiles(new File(newSrcDir + dest), new File(coreModule.getSourceDirectory().getPath() + dest));
                    }
                    NbModulesAPI.addPublicPackageDecl(coreModule, corePkgs.toArray(new String[]{}));

                    //Create single lib module for all to depend on
                    NbModuleProject libModule = NbModulesAPI.createLibraryModule("libs", "Libraries", projDir + sep + "modules" + sep + "libs",
                            suite, NBJavaSrcUtils.findJarFiles(projDir + sep + "lib"));
                    try {
                        NbModulesAPI.addDependency(coreModule, libModule);
                    } catch (CyclicDependencyException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    OutputUtil.log("Done creating core module under " + coreModule.getSourceDirectory().getPath());

                    //Create a module for each feature
                    for (String fp : featurePkgs) {
                        NbModuleProject fsModule = NbModulesAPI.createModule(
                                fp,
                                fp + " feature",
                                projDir + sep + "modules" + sep + fp, suite);
                        String dest = sep + fp.replace(".", sep);
                        copyFiles(new File(newSrcDir + dest), new File(fsModule.getSourceDirectory().getPath() + dest));
                        OutputUtil.log("Done creating feature module under " + fsModule.getSourceDirectory().getPath());
                        try {
                            NbModulesAPI.addDependency(fsModule, coreModule);
                            NbModulesAPI.addDependency(fsModule, libModule);
                            OutputUtil.log("Done adding dependencies for " + fsModule.getSourceDirectory().getPath());
                        } catch (CyclicDependencyException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }

                    ProjectManager.mutex().writeAccess(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                NbModulesAPI.setupSuiteBranding(suite);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    });
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                OutputUtil.log("Done modularizing");
            }
        });
    }

    private String findDestPkg(Map<String, String> classToPkg, String file) {
        for (String clazz : classToPkg.keySet()) {
            if (file.replaceAll("/", "").replaceAll("\\\\", "").replaceAll("\\:", "").endsWith("src" + clazz.replaceAll("\\.", "") + ".java")) {
                return classToPkg.get(clazz);
            }
        }
        return null;
    }

    class MetricLabel extends JLabel implements RestructuringListener {

        private final String msg;
        private final float org;
        private final float remod;
        private final RemodularizationWorkbench postWorkbench;
        private final RemodularizationObjectiveProvider objective;
        private final StaticDependencyModel orgSdm;
        private final StaticDependencyModel remodSdm;
        private final Set<TraceModel> tms;

        private MetricLabel(RemodularizationWorkbench postWorkbench, RemodularizationObjectiveProvider objective, StaticDependencyModel orgSdm, StaticDependencyModel remodSdm) {
            this.orgSdm = orgSdm;
            this.remodSdm = remodSdm;
            tms = Controller.getInstance().getTraceSet().getFirstLevelTraces();
            this.org = objective.createObjective().calculateAndReturnRes(tms, orgSdm);
            AbstractMetric postObj = objective.createObjective();
            this.remod = postObj.calculateAndReturnRes(tms, remodSdm);
            StringBuilder sb = new StringBuilder(postObj.getName() + ";val" + "\n");
            for (Result res : postObj.getResults()) {
                sb.append(res.name + ";" + res.value + "\n");
            }
//            OutputUtil.log(sb.toString());
            msg = objective.getObjectiveName() + ": " + org + " -> " + remod + " -> ";
            this.postWorkbench = postWorkbench;
            this.objective = objective;
            setText(msg + remod);
            setOpaque(true);
            setToolTipText(msg + remod);

            postWorkbench.addRestructuringListener(this);
        }

        @Override
        public void sceneRestructured() {
            StaticDependencyModel newSdm = buildRestructuredSdm(postWorkbench.getCurrentClassToPkgMap(), remodSdm);

            AbstractMetric m = objective.createObjective();
            float res = m.calculateAndReturnRes(tms, newSdm);
            if (res == remod) {
                setForeground(Color.BLACK);
            } else if ((res > remod && !objective.isMinimization())
                    || (res < remod && objective.isMinimization())) {
                setForeground(new Color(64, 138, 0));
            } else {
                setForeground(new Color(191, 51, 0));
            }

            setText(msg + res);
            invalidate();
            repaint();
        }
    }

    private void setupPostToolbar(RemodularizationWorkbench postWorkbench, ActionListener annotate, ActionListener restructure) {
        JPanel postToolbar = new JPanel(new BorderLayout());
        postToolbar.add(postWorkbench.getSatelliteView(), BorderLayout.WEST);
        JPanel controls = new JPanel(new GridLayout(5, 1));
        controls.add(new JLabel("Measures: [original]->[proposed]->[adjusted]"));
        MetricLabel sca = new MetricLabel(postWorkbench, new ScatteringObjective(), orgSdm, newSdm);
        controls.add(sca);
        MetricLabel tang = new MetricLabel(postWorkbench, new TanglingObjective(), orgSdm, newSdm);
        controls.add(tang);
        MetricLabel coh = new MetricLabel(postWorkbench, new CohesionObjective(), orgSdm, newSdm);
        controls.add(coh);
        MetricLabel coup = new MetricLabel(postWorkbench, new CouplingObjective(), orgSdm, newSdm);
        controls.add(coup);
        MetricLabel ins = new MetricLabel(postWorkbench, new StableDependenciesPrincipleObjective(), orgSdm, newSdm);
        controls.add(ins);
        MetricLabel dist = new MetricLabel(postWorkbench, new StableAbstractionsPrincipleObjective(), orgSdm, newSdm);
        controls.add(dist);
        MetricLabel scaC = new MetricLabel(postWorkbench, new PkgsPerFeatureObjective(), orgSdm, newSdm);
        controls.add(scaC);
        MetricLabel tangC = new MetricLabel(postWorkbench, new FeatsPerPkgObjective(), orgSdm, newSdm);
        controls.add(tangC);
        postToolbar.add(controls, BorderLayout.CENTER);

        JPanel actionPan = new JPanel(new FlowLayout());
        asModules = new JCheckBox("Put into NB modules");
        asModules.setSelected(true);
        asModules.setFocusable(false);
        actionPan.add(asModules);

        JButton applyAnnotate = new JButton("Annotate");
        applyAnnotate.setSelected(false);
        applyAnnotate.setFocusable(false);
        actionPan.add(applyAnnotate);
        applyAnnotate.addActionListener(annotate);

        JButton applyRestructure = new JButton("Restructure annotated");
        applyRestructure.setSelected(false);
        applyRestructure.setFocusable(false);
        actionPan.add(applyRestructure);
        applyRestructure.addActionListener(restructure);

        JButton md = new JButton("M");
        md.setSelected(false);
        md.setFocusable(false);
        actionPan.add(md);
        md.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                printDesignComparison(remodularizationMetricAggregator);
            }
        });

        postToolbar.add(actionPan, BorderLayout.EAST);

        add(postToolbar, BorderLayout.SOUTH);
        tabbedPane.addTab("Proposed remodularization " + ++designNumber, this);
        tabbedPane.setSelectedComponent(this);
    }

    private StaticDependencyModel computeNewSdm(ProgressHandle progressHandle, boolean factorSingle) {
        StaticDependencyModel newSdm = null;
        try {
//            if(factorSingle){
//                progressHandle.progress("Featureous: simulating clean split");
//                CleanSplitSimulation.cleanSplitCurrentTraces();
//            }
            progressHandle.progress("Featureous: remodularizing");
            boolean rename = iterations.get() > 5;
            newSdm = MainRemodularization.findNewModularization(orgSdm, progressHandle,
                    selectedProviders, factorSingle, iterations, population, mutation);

            if (rename) {
                PkgRenamer.renamePackages(newSdm, orgSdm);
            }

            if (!MainRemodularization.checkConsistency(orgSdm, newSdm)) {
                OutputUtil.log("ERROR: New static model inconsistent with the old one!");
                throw new RuntimeException("Consistency exception.");
            }

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return newSdm;
    }

    /**
     * This function will copy files or directories from one location to another.
     * note that the source and the destination must be mutually exclusive. This 
     * function can not be used to copy a directory to a sub directory of itself.
     * The function will also have problems if the destination files already exist.
     * @param src -- A File object that represents the source for the copy
     * @param dest -- A File object that represnts the destination for the copy.
     * @throws IOException if unable to copy.
     */
    public static void copyFiles(File src, File dest) throws IOException {
        //Check to ensure that the source is valid...
        if (!src.exists()) {
            throw new IOException("copyFiles: Can not find source: " + src.getAbsolutePath() + ".");
        } else if (!src.canRead()) { //check to ensure we have rights to the source...
            throw new IOException("copyFiles: No right to source: " + src.getAbsolutePath() + ".");
        }
        //is this a directory copy?
        if (src.isDirectory()) {
            if (!dest.exists()) { //does the destination already exist?
                //if not we need to make it exist if possible (note this is mkdirs not mkdir)
                if (!dest.mkdirs()) {
                    throw new IOException("copyFiles: Could not create direcotry: " + dest.getAbsolutePath() + ".");
                }
            }
            //get a listing of files...
            String list[] = src.list();
            //copy all the files in the list.
            for (int i = 0; i < list.length; i++) {
                File dest1 = new File(dest, list[i]);
                File src1 = new File(src, list[i]);
                copyFiles(src1, dest1);
            }
        } else {
            //This was not a directory, so lets just copy the file
            FileInputStream fin = null;
            FileOutputStream fout = null;
            byte[] buffer = new byte[4096]; //Buffer 4K at a time (you can change this).
            int bytesRead;
            try {
                //open the files for input and output
                fin = new FileInputStream(src);
                fout = new FileOutputStream(dest);
                //while bytesRead indicates a successful read, lets write...
                while ((bytesRead = fin.read(buffer)) >= 0) {
                    fout.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) { //Error copying file... 
                IOException wrapper = new IOException("copyFiles: Unable to copy file: "
                        + src.getAbsolutePath() + "to" + dest.getAbsolutePath() + ".");
                wrapper.initCause(e);
                wrapper.setStackTrace(e.getStackTrace());
                throw wrapper;
            } finally { //Ensure that the files are closed (if they were open).
                if (fin != null) {
                    fin.close();
                }
                if (fout != null) {
                    fout.close();
                }
            }
        }
    }
}
