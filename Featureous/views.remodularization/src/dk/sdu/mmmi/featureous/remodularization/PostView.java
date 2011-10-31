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
import dk.sdu.mmmi.featureous.metrics.Result;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.Scattering;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.Tangling;
import dk.sdu.mmmi.featureous.remodularization.logic.MainRemodularization;
import dk.sdu.mmmi.featureous.remodularization.logic.objectives.CohesionObjective;
import dk.sdu.mmmi.featureous.remodularization.logic.objectives.CouplingObjective;
import dk.sdu.mmmi.featureous.remodularization.metrics.VirtualScatteringObjective;
import dk.sdu.mmmi.featureous.remodularization.metrics.VirtualTanglingObjective;
import dk.sdu.mmmi.featureous.remodularization.spi.RemodularizationObjectiveProvider;
import dk.sdu.mmmi.featureous.remodularization.transform.MoveClassNB;
import dk.sdu.mmmi.featureous.remodularization.workbench.RemodularizationWorkbench;
import dk.sdu.mmmi.featureous.remodularization.workbench.infrastructure.RestructuringListener;
import dk.sdu.mmmi.srcUtils.nb.NBJavaSrcUtils;
import dk.sdu.mmmi.srcUtils.sdm.model.HashList;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import dk.sdu.mmmi.srcUtils.sdm.model.Util;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
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
import org.openide.util.RequestProcessor;

/**
 *
 * @author andrzejolszak
 */
public class PostView extends JPanel {

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
    private static int number = 1;
    private final int population;
    private final int iterations;
    private final float mutation;

    public PostView(final boolean factorSingle, final Project proj, final String srcDir, final String backupDir,
            final StaticDependencyModel orgSdm, final Set<RemodularizationObjectiveProvider> selectedProviders,
            JTabbedPane tabbedPane, int iterations, int population, float mutation) {
        super(new BorderLayout());
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

                    JButton apply = setupPostToolbar(postWorkbench);

                    apply.addActionListener(new RemodularizationActionListener());
                } else {
                    OutputUtil.log("Problem generating new static dependency model for the project. "
                            + "Remodularization aborted.");
                }
            }
        });

    }

    private class RemodularizationActionListener implements ActionListener {

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
                            doEncapsulateAsModules(featureSpecPackages, corePackages);
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

    private StaticDependencyModel buildRestructuredSdm(Map<String, String> cToP, StaticDependencyModel orgSdm) {
        StaticDependencyModel newDm = new StaticDependencyModel();

        HashList<JType> added = new HashList<JType>();
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

    private void doEncapsulateAsModules(final Set<String> featurePkgs, final Set<String> corePkgs) {

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
                        copyFiles(new File(srcDir + dest), new File(coreModule.getSourceDirectory().getPath() + dest));
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
                        copyFiles(new File(srcDir + dest), new File(fsModule.getSourceDirectory().getPath() + dest));
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
            StringBuilder sb = new StringBuilder(postObj.getName()+";val"+"\n");
            for(Result res : postObj.getResults()){
                sb.append(res.name + ";"+res.value+"\n");
            }
            OutputUtil.log(sb.toString());
            msg = objective.getObjectiveName() + ": " + org + " -> " + remod + " -> ";
            this.postWorkbench = postWorkbench;
            this.objective = objective;
            setText(msg + remod);
            setOpaque(true);

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

    private JButton setupPostToolbar(RemodularizationWorkbench postWorkbench) {
        JPanel postToolbar = new JPanel(new BorderLayout());
        postToolbar.add(postWorkbench.getSatelliteView(), BorderLayout.WEST);
        JPanel controls = new JPanel(new GridLayout(5, 1));
        controls.add(new JLabel("Measures: [original]->[proposed]->[adjusted]"));
        Scattering sca1 = new Scattering(true);
        sca1.calculateAll(Controller.getInstance().getTraceSet().getFirstLevelTraces(), orgSdm);
        OutputUtil.log("orgSca: " + sca1.getResult());
        Tangling tan1 = new Tangling(true);
        tan1.calculateAll(Controller.getInstance().getTraceSet().getFirstLevelTraces(), orgSdm);
        OutputUtil.log("orgTan: " + tan1.getResult());
        MetricLabel sca = new MetricLabel(postWorkbench, new VirtualScatteringObjective(), orgSdm, newSdm);
        controls.add(sca);
        MetricLabel tang = new MetricLabel(postWorkbench, new VirtualTanglingObjective(), orgSdm, newSdm);
        controls.add(tang);
        MetricLabel coh = new MetricLabel(postWorkbench, new CohesionObjective(), orgSdm, newSdm);
        controls.add(coh);
        MetricLabel coup = new MetricLabel(postWorkbench, new CouplingObjective(), orgSdm, newSdm);
        controls.add(coup);
        postToolbar.add(controls, BorderLayout.CENTER);

        JPanel actionPan = new JPanel(new BorderLayout());
        asModules = new JCheckBox("Encapsulate as NetBeans modules");
        asModules.setSelected(true);
        asModules.setFocusable(false);
        actionPan.add(asModules, BorderLayout.NORTH);

        JButton apply = new JButton("Apply current structure");
        apply.setSelected(false);
        apply.setFocusable(false);
        actionPan.add(apply, BorderLayout.SOUTH);
        postToolbar.add(actionPan, BorderLayout.EAST);

        add(postToolbar, BorderLayout.SOUTH);
        tabbedPane.addTab("Proposed remodularization " + number++, this);
        tabbedPane.setSelectedComponent(this);
        return apply;
    }

    private StaticDependencyModel computeNewSdm(ProgressHandle progressHandle, boolean factorSingle) {
        StaticDependencyModel newSdm = null;
        try {
//            {
//                progressHandle.progress("Featureous: simulating clean split");
//                CleanSplitSimulation.cleanSplitCurrentTraces();
//            }
            progressHandle.progress("Featureous: remodularizing");
            newSdm = MainRemodularization.findNewModularization(orgSdm, progressHandle,
                    selectedProviders, factorSingle, iterations, population, mutation);
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
