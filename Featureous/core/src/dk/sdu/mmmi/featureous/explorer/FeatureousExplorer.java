/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.explorer;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.TraceSet;
import dk.sdu.mmmi.featureous.core.model.TraceListChangeListener;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.staticlocation.ControlFlowExtractor;
import dk.sdu.mmmi.featureous.core.staticlocation.TracePostProcessor;
import dk.sdu.mmmi.featureous.core.ui.OutputUtil;
import dk.sdu.mmmi.featureous.explorer.api.AbstractTraceView;
import dk.sdu.mmmi.featureous.explorer.nodes.ProjectNode;
import dk.sdu.mmmi.featureous.explorer.spi.FeatureTraceView;
import dk.sdu.mmmi.featureous.explorer.spi.LicenseProvider;
import dk.sdu.mmmi.featureous.icons.IconUtils;
import dk.sdu.mmmi.srcUtils.AnalysisUtils;
import dk.sdu.mmmi.srcUtils.nb.NBJavaSrcUtils;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar.Separator;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor.InputLine;
import org.openide.NotifyDescriptor.Message;
import org.openide.util.Exceptions;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.Mode;

@TopComponent.Description(preferredID = "Featureous explorer",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@ActionID(category = "Window", id = "dk.sdu.mmmi.featureous.core.FeatureousExplorer")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@ServiceProvider(service = FeatureousExplorer.class)
public final class FeatureousExplorer extends TopComponent implements ExplorerManager.Provider, LookupListener {

    private static FeatureousExplorer instance;
    /** path to the icon used by the component and its open action */
//        private static final Icon REFRESH_ICON =
//            loadIcon("toolbarButtonGraphics/general/Refresh16.gif");
    static final String APP_ICON = "dk/sdu/mmmi/featureous/explorer/app_icon_small.png";
    static final String ADD_TRACES_ICON_PATH = "fugue/icons/documentplus.png";
    static final String AUTO_UPDATE_ICON_PATH = "fugue/icons/arrowcircledouble.png";
    static final String STATIC_ICON_PATH = "fugue/icons/arrowcircle.png";
    static final String REMOVE_TRACES_ICON_PATH = "fugue/icons/documentminus.png";
    static final String GROUP_TRACES_ICON_PATH = "fugue/icons/documentsstack.png";
    static final String UNGROUP_TRACES_ICON_PATH = "fugue/icons/documentview.png";
    static final String FOCUS_ICON_PATH = "fugue/icons/documentsub.png";
    private File traceFolder;
    private static final String PREFERRED_ID = FeatureousExplorer.class.getName();
    private final TraceFileChooser traceFileChooser;
    private final Result<TraceModel> selection;
    private final Result<FeatureTraceView> views;

    public FeatureousExplorer() {
        this.traceFileChooser = new TraceFileChooser();
        initComponents();
        customInitComponents();
        setName("Feature explorer");
        setToolTipText("Featureous main screen");
        setIcon(ImageUtilities.loadImage(APP_ICON));

        Lookup l = ExplorerUtils.createLookup(mgr, getActionMap());
        associateLookup(l);

        selection = this.getLookup().lookup(new Lookup.Template<TraceModel>(TraceModel.class));
        views = Lookup.getDefault().lookupResult(FeatureTraceView.class);

        tll.traceListChanged(Controller.getInstance().getTraceSet());
        OpenProjects.getDefault().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                update.setEnabled(NBJavaSrcUtils.getMainProject() != null);
            }
        });
        update.setEnabled(NBJavaSrcUtils.getMainProject() != null);
    }

    public Result<TraceModel> getSelection() {
        return selection;
    }
    private JButton update;
    private JButton postprocess;
    private JButton addTraces;
    private JButton removeTraces;
    private JButton groupTraces;
    private JButton ungroupTraces;

    private void customInitComponents() {
        update = new JButton();
        update.setIcon(ImageUtilities.loadImageIcon(AUTO_UPDATE_ICON_PATH, false));
        update.setToolTipText("Update traces of current main project");
        update.addActionListener(au);
        jToolBar1.add(update);

        postprocess = new JButton();
        postprocess.setIcon(ImageUtilities.loadImageIcon(STATIC_ICON_PATH, false));
        postprocess.setToolTipText("Postprocess current traces");
        postprocess.addActionListener(post);
        jToolBar1.add(postprocess);

        addTraces = new JButton();
        addTraces.setIcon(IconUtils.loadIcon(ADD_TRACES_ICON_PATH));
        addTraces.setToolTipText("Add feature traces...");
        addTraces.addActionListener(atl);
        jToolBar1.add(addTraces);

        removeTraces = new JButton();
        removeTraces.setIcon(ImageUtilities.loadImageIcon(REMOVE_TRACES_ICON_PATH, false));
        removeTraces.setToolTipText("Remove feature traces");
        removeTraces.addActionListener(rtl);
        jToolBar1.add(removeTraces);

        groupTraces = new JButton();
        groupTraces.setIcon(ImageUtilities.loadImageIcon(GROUP_TRACES_ICON_PATH, false));
        groupTraces.setToolTipText("Group feature traces");
        groupTraces.addActionListener(gtl);
        jToolBar1.add(groupTraces);

        ungroupTraces = new JButton();
        ungroupTraces.setIcon(ImageUtilities.loadImageIcon(UNGROUP_TRACES_ICON_PATH, false));
        ungroupTraces.setToolTipText("Ungroup feature traces");
        ungroupTraces.addActionListener(ugtl);
        jToolBar1.add(ungroupTraces);

        jToolBar1.add(new Separator());

        jToolBar2.setLayout(new WrapLayout(WrapLayout.LEFT, 0, 0));

        jLabel1.setText("Packages:");
        jLabel2.setText("Classes:");
        jLabel3.setText("Methods:");
    }

    private void createViewMenu() {

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                synchronized (jToolBar2) {
                    jToolBar2.removeAll();
                    List<FeatureTraceView> ftvs = new ArrayList<FeatureTraceView>(views.allInstances());
                    Collections.sort(ftvs, new Comparator<FeatureTraceView>() {

                        public int compare(FeatureTraceView o1, FeatureTraceView o2) {
                            if (o1.getInstance() == null || o1.getInstance().getName() == null) {
                            }
                            if (o2.getInstance() == null || o2.getInstance().getName() == null) {
                            }
                            return o1.getInstance().getName().compareTo(o2.getInstance().getName());
                        }
                    });
                    for (FeatureTraceView v : ftvs) {
                        JButton b = new JButton();
                        b.setToolTipText(v.getInstance().getName());
                        b.setIcon(ImageUtilities.image2Icon(v.getBigIcon()));

                        final FeatureTraceView fv = v;
                        b.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                AbstractTraceView fcv = (AbstractTraceView) fv.createInstance();
                                Mode mode = WindowManager.getDefault().findMode(fcv.getGuiMode());
                                if (mode != null) {
                                    mode.dockInto(fcv);
                                }
                                fcv.open();
                                fcv.requestActive();
                            }
                        });
                        jToolBar2.add(b);
                    }
                    jToolBar2.revalidate();
                    jToolBar2.repaint();
                }
            }
        });
    }

    public Set<TraceModel> getSelectedTraces() {
//        ArrayList<TraceModel> tm = new ArrayList<TraceModel>();
//        for (Node n : mgr.getSelectedNodes()) {
//            tm.addAll(n.getLookup().lookupAll(TraceModel.class));
//        }
//        return tm;
        Set<TraceModel> m = new HashSet<TraceModel>(selection.allInstances());
        return m;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new BeanTreeView();
        jToolBar2 = new javax.swing.JToolBar();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        jToolBar1.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.SystemColor.activeCaptionBorder));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setMaximumSize(new java.awt.Dimension(30, 30));
        jToolBar1.setMinimumSize(new java.awt.Dimension(20, 30));
        jToolBar1.setPreferredSize(new java.awt.Dimension(20, 30));
        add(jToolBar1, java.awt.BorderLayout.PAGE_START);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(null);
        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jToolBar2.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.SystemColor.activeCaptionBorder));
        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);
        jToolBar2.setMaximumSize(new java.awt.Dimension(300, 155));
        jToolBar2.setMinimumSize(new java.awt.Dimension(30, 30));
        jToolBar2.setPreferredSize(new java.awt.Dimension(300, 82));
        jPanel1.add(jToolBar2, java.awt.BorderLayout.SOUTH);

        add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.GridLayout(3, 2));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(FeatureousExplorer.class, "FeatureousExplorer.jLabel1.text")); // NOI18N
        jPanel2.add(jLabel1);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(FeatureousExplorer.class, "FeatureousExplorer.jLabel4.text")); // NOI18N
        jPanel2.add(jLabel4);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(FeatureousExplorer.class, "FeatureousExplorer.jLabel2.text")); // NOI18N
        jPanel2.add(jLabel2);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(FeatureousExplorer.class, "FeatureousExplorer.jLabel5.text")); // NOI18N
        jPanel2.add(jLabel5);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(FeatureousExplorer.class, "FeatureousExplorer.jLabel3.text")); // NOI18N
        jPanel2.add(jLabel3);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(FeatureousExplorer.class, "FeatureousExplorer.jLabel6.text")); // NOI18N
        jPanel2.add(jLabel6);

        add(jPanel2, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents
    ExplorerManager mgr = new ExplorerManager();
    private String traceRoot = "";

    private void createTree() {
//        if(Controller.getInstance().getTraceSet().getAllTraces().size()==0){
//            return;
//        }
        AbstractNode root = new AbstractNode(new ProjectNode()) {

            @Override
            public String getName() {
                return traceRoot;
            }

            @Override
            public String getHtmlDisplayName() {
                return getName();
            }

            @Override
            public String getDisplayName() {
                return getName();
            }
        };

        if (NBJavaSrcUtils.getMainProject() != null) {
            root.setDisplayName(ProjectUtils.getInformation(NBJavaSrcUtils.getMainProject()).getDisplayName());
        } else {
            root.setDisplayName("");
        }
        mgr.setRootContext(root);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized FeatureousExplorer getDefault() {
        if (instance == null) {
            instance = new FeatureousExplorer();
        }
        return instance;
    }

    /**
     * Obtain the FeatureousExplorerTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized FeatureousExplorer findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(FeatureousExplorer.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof FeatureousExplorer) {
            return (FeatureousExplorer) win;
        }
        Logger.getLogger(FeatureousExplorer.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        Controller.getInstance().getTraceSet().addChangeListener(tll);
        selection.addLookupListener(this);
        selection.allItems();
        resultChanged(null);
        views.addLookupListener(vl);
        views.allInstances();
        vl.resultChanged(null);
        resultChanged(null);
//        au.actionPerformed(null);
//        UIUtils.setupDefaultColorForAll(this);
//        checkLicensing();
    }

    @Override
    public void componentClosed() {
        Controller.getInstance().getTraceSet().removeChangeListener(tll);
        selection.removeLookupListener(this);
        views.removeLookupListener(vl);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        FeatureousExplorer singleton = FeatureousExplorer.getDefault();
        singleton.readPropertiesImpl(p);
        return singleton;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public ExplorerManager getExplorerManager() {
        return mgr;
    }

    private void addTracesClicked() {
        int returnVal = traceFileChooser.showOpenDialog(this);
        Controller controller = Controller.getInstance();
        if (returnVal == TraceFileChooser.APPROVE_OPTION) {
            File[] chosenFiles = traceFileChooser.getSelectedFiles();
            String errorMessage = controller.addTraces(chosenFiles);
            if (errorMessage != null) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        "Error while loading traces", NotifyDescriptor.ERROR_MESSAGE));
            }
        }
    }

    private void removeTracesClicked() {
        Set<TraceModel> tm = getSelectedTraces();
        Controller controller = Controller.getInstance();
        controller.removeTraces(tm);
    }

    private void groupTracesClicked() {
        Controller controller = Controller.getInstance();
        NotifyDescriptor.InputLine il = new NotifyDescriptor.InputLine("Type the name for the new group:", "Group name");
        DialogDisplayer.getDefault().notify(il);
        String newTraceName = il.getInputText();
        if ((newTraceName != null) && (newTraceName.length() > 0)) {
            if (!controller.traceExists(newTraceName)) {
                controller.mergeTraces(newTraceName, getSelectedTraces());
            } else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        "A trace with the name \"" + newTraceName + "\" already exists.",
                        NotifyDescriptor.ERROR_MESSAGE));
            }
        } else {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("You must enter a valid trace name.",
                    NotifyDescriptor.ERROR_MESSAGE));
        }
    }

    private void ungroupTracesClicked() {
        Controller controller = Controller.getInstance();
        for (TraceModel m : getSelectedTraces()) {
            if (m.hasSubTraces()) {
                controller.splitTrace(m);
            }
        }
    }
    private ActionListener au = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Featureous: updating traces");
                    progressHandle.start();
                    progressHandle.switchToIndeterminate();

                    resultChanged(null);
                    update();
                    Controller.getInstance().getAffinity().getPkgAffinity("");

                    progressHandle.finish();
                }
            });
        }
    };
    private ActionListener post = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Featureous: postprocessing traces");
                    progressHandle.start();
                    progressHandle.switchToIndeterminate();

                    doPostProcessing();
                    Controller.getInstance().getTraceSet().notifyChanged();
                    resultChanged(null);

                    progressHandle.finish();
                }
            });
        }

        private void doPostProcessing() {
            if (NBJavaSrcUtils.getMainProject() == null) {
                StatusDisplayer.getDefault().setStatusText("Please set a Java project as Main Project");
                return;
            }
            Project p = NBJavaSrcUtils.getMainProject();
            TracePostProcessor cfe = new TracePostProcessor();
            try {
                AnalysisUtils.analyzeParsedProject(p, cfe);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    };
    private ActionListener stat = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Featureous: generating static traces");
                    progressHandle.start();
                    progressHandle.switchToIndeterminate();

                    generateStaticTraces();
                    resultChanged(null);

                    update();

                    Controller.getInstance().getAffinity().getPkgAffinity("");

                    progressHandle.finish();
                }
            });
        }

        private void generateStaticTraces() {
            if (NBJavaSrcUtils.getMainProject() == null) {
                StatusDisplayer.getDefault().setStatusText("Please set a Java project as Main Project");
                return;
            }
            Project p = NBJavaSrcUtils.getMainProject();
            ControlFlowExtractor cfe = new ControlFlowExtractor(p.getProjectDirectory().getPath());
            try {
                AnalysisUtils.analyzeParsedProject(p, cfe);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    };

    public void update() {
        if (NBJavaSrcUtils.getMainProject() == null) {
            StatusDisplayer.getDefault().setStatusText("Please set a JavaSE project as Main Project for auto update");
            return;
        }
        String path = NBJavaSrcUtils.getMainProject().getProjectDirectory().getPath();
        traceFolder = new File(path + System.getProperty("file.separator") + "FeatureTraces");
        if (traceFolder != null && traceFolder.isDirectory()) {
            List<File> traceFolders = new ArrayList<File>();
            for (File f : traceFolder.listFiles()) {
                if (f.isDirectory()) {
                    traceFolders.add(f);
                }
            }

            Collections.sort(traceFolders, new Comparator<File>() {

                @Override
                public int compare(File o1, File o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            Map<String, File> traces = new HashMap<String, File>();
            Controller.getInstance().removeTraces(Controller.getInstance().getTraceSet().getAllTraces());
//            for (File t : traceFolders) {
            File t = traceFolders.get(traceFolders.size() - 1);
            for (File f : t.listFiles()) {
                if (f.isFile() && f.getName().endsWith(".ftf")) {
                    traces.put(f.getName(), f);
                }
            }
//            }
            traceRoot = t.getName();
            Controller.getInstance().addTraces(traces.values().toArray(new File[]{}));
        }
    }
    private ActionListener atl = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            addTracesClicked();
        }
    };
    private ActionListener rtl = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            removeTracesClicked();
        }
    };
    private ActionListener gtl = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            groupTracesClicked();
        }
    };
    private ActionListener ugtl = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            ungroupTracesClicked();
        }
    };
    private TraceListChangeListener tll = new TraceListChangeListener() {

        @Override
        public void traceListChanged(TraceSet tl) {
            createTree();
        }
    };

    public void resultChanged(LookupEvent ev) {
        Set<TraceModel> tmc = getSelectedTraces();
        boolean modificationLock = false;//focusOn
        boolean groupSelected = false;
        boolean subtraceSelected = false;
        for (TraceModel tm : tmc) {
            groupSelected |= tm.hasSubTraces();
            subtraceSelected |= !Controller.getInstance().getTraceSet().getFirstLevelTraces().contains(tm);
        }

        postprocess.setEnabled(!modificationLock);

        addTraces.setEnabled(!modificationLock);
        //remove
        removeTraces.setEnabled(tmc.size() > 0 && !subtraceSelected && !modificationLock);

        //group
        groupTraces.setEnabled(tmc.size() > 1 && !groupSelected && !subtraceSelected && !modificationLock);

        //ungroup
        boolean selectedGroups = tmc.size() > 0;
        for (TraceModel m : tmc) {
            selectedGroups &= m.hasSubTraces();
        }
        ungroupTraces.setEnabled(selectedGroups && !modificationLock);

        Set<String> classes = new HashSet<String>();
        Set<String> pkgs = new HashSet<String>();
        Set<String> methods = new HashSet<String>();
        for (TraceModel m : tmc) {
            for (ClassModel cm : m.getClassSet()) {
                classes.add(cm.getName());
                pkgs.add(cm.getPackageName());
                methods.addAll(cm.getAllMethods());
            }
        }
        jLabel4.setText("" + pkgs.size());
        jLabel5.setText("" + classes.size());
        jLabel6.setText("" + methods.size());
        Set<String> feats = new HashSet<String>();
        for (TraceModel tm : tmc) {
            feats.add(tm.getName());
        }
        Controller.getInstance().getTraceSet().getSelectionManager().clearAllSelections(false);
        Controller.getInstance().getTraceSet().getSelectionManager().addFeatureSelection(feats);

    }
    private LookupListener vl = new LookupListener() {

        @Override
        public void resultChanged(LookupEvent ev) {
            createViewMenu();
        }
    };
    
     private void checkLicensing() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    LicenseProvider license = Lookup.getDefault().lookup(LicenseProvider.class);
                    if (license == null) {
                        return;
                    }
                    OutputUtil.log("License valid? " + license.isValid());
                    if (!license.isValid()) {
                        String msg = "<html>We'd like to keep track of the number of people using our tool.<br>"
                                + "Please help us do so by getting a free license. After you click OK, you should be transported to the registration website:</html>";
                        String link = "http://featureous.org/reg/register.php?mid=" + license.getMachineIdString();
                        
                        JPanel pan = new JPanel(new BorderLayout());
                        pan.add(new JLabel(msg), BorderLayout.NORTH);
                        JTextField f = new JTextField(link);
                        f.setEditable(false);
                        pan.add(f, BorderLayout.SOUTH);

                        Message q = new DialogDescriptor.Message(pan);
                        DialogDisplayer.getDefault().notify(q);
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().browse(new URI("http://featureous.org/reg/register.php?mid=" + license.getMachineIdString()));
                            } catch (Exception ex) {
                            }
                        }
                        InputLine input = new DialogDescriptor.InputLine("Please paste your confirmation code:", "Register Featureous");
                        DialogDisplayer.getDefault().notify(input);


                        String confirmString = input.getInputText();

                        if (confirmString != null) {
                            try {
                                license.downloadLicense(confirmString);
                            } catch (Exception e) {
                            }

                            license.refreshLicense();

                            if (license.isValid()) {
                                DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message("<html>Thanks for registering!</html>"));
                            } else {
                                DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message("<html>Featureous license is <b>not</b> valid. Some views will be disabled...</html>"));
                                for (Component c : jToolBar2.getComponents()) {
                                    c.setEnabled(new Random().nextBoolean());
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    //Silent
                }
            }
        });
    }
}
