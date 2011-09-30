/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.workbench;

import dk.sdu.mmmi.featureous.core.affinity.Affinity;
import dk.sdu.mmmi.featureous.core.affinity.AffinityProvider;
import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.OrderedBinaryRelation;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.ui.OutputUtil;
import dk.sdu.mmmi.featureous.remodularization.workbench.infrastructure.RestructuringListener;
import dk.sdu.mmmi.featureous.remodularization.workbench.infrastructure.SceneSupport;
import dk.sdu.mmmi.featureous.remodularization.workbench.infrastructure.StringGraphScene;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.EditProvider;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.graph.layout.GridGraphLayout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.router.DirectRouter;

/**
 *
 * @author ao
 */
public class RemodularizationWorkbench extends JPanel {

    private final StringGraphScene scene;
    private final StaticDependencyModel sdm;
    private final boolean editable;
    private final HashMap<String, PackageWidget> pkgToWidget;
    private final HashMap<String, UMLClassWidget> classToWidget;

    public RemodularizationWorkbench(StaticDependencyModel sdm, boolean editable) {
        this.setLayout(new BorderLayout());

        this.sdm = sdm;
        this.editable = editable;

        scene = new StringGraphScene();
        scene.setVisible(false);

        GridGraphLayout<String, String> graphLayout = new GridGraphLayout<String, String>();
        final SceneLayout sceneGraphLayout = LayoutFactory.createSceneGraphLayout(scene, graphLayout);
        sceneGraphLayout.invokeLayout();

        scene.getActions().addAction(ActionFactory.createEditAction(new EditProvider() {

            public void edit(Widget widget) {
                sceneGraphLayout.invokeLayoutImmediately();
            }
        }));

        scene.getActions().addAction(ActionFactory.createPanAction());
        scene.getActions().addAction(ActionFactory.createMouseCenteredZoomAction(1.1));
        scene.getPriorActions().addAction(new SceneSupport.ShiftKeySwitchToolAction(scene, editable));

        Set<TraceModel> firstLevelTraces = Controller.getInstance().getTraceSet().getFirstLevelTraces();

        pkgToWidget = new HashMap<String, PackageWidget>();
        classToWidget = new HashMap<String, UMLClassWidget>();
        if (sdm != null) {
            addSdmPackages(sdm, pkgToWidget);
            addSdmClasses(pkgToWidget, classToWidget, sdm);
        }
        addFtmPackages(firstLevelTraces, pkgToWidget, classToWidget);
        addFtmClasses(pkgToWidget, firstLevelTraces, classToWidget);
        addFtmCalls(classToWidget, firstLevelTraces);
        final JComponent sceneView = scene.createView();
        this.add(new JScrollPane(sceneView), BorderLayout.CENTER);
        scene.setVisible(true);
        scene.revalidate(false);
        this.revalidate();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                for (UMLClassWidget cw : classToWidget.values()) {
                    cw.stateChanged();
                }
            }
        });
//        this.add(sceneView, BorderLayout.CENTER);
    }

    public StaticDependencyModel getSdm() {
        return sdm;
    }

    public void addRestructuringListener(RestructuringListener listener) {
        scene.addRestructuringListener(listener);
    }

    public Map<String, String> getCurrentClassToPkgMapNotPresentInSdm(StaticDependencyModel orgSdm) {
        Map<String, String> classToPkg = getCurrentClassToPkgMap();
        OutputUtil.log("Pre size" + classToPkg.size());

        for (JPackage p : orgSdm.getPackages()) {
            for (JType t : p.getTopLevelTypes()) {
                if (classToPkg.get(t.getQualName()).equals(p.getQualName())) {
                    classToPkg.remove(t.getQualName());
                }
            }
        }

        OutputUtil.log("Post size" + classToPkg.size());

        return classToPkg;
    }

    public Map<String, String> getCurrentClassToPkgMap() {
        Map<String, String> classToPkg = new HashMap<String, String>();

        for (UMLClassWidget cw : classToWidget.values()) {
            PackageWidget pkg = (PackageWidget) cw.getParentWidget();
            classToPkg.put(cw.getFullName(), pkg.getName());
        }

        return classToPkg;
    }

    public Set<String> getCurrentSingleFeatPkgs() {
        Set<String> singlePkgs = new HashSet<String>();

        for (PackageWidget pkg : pkgToWidget.values()) {
            if (Affinity.SINGLE_FEATURE.equals(pkg.getCurrentAffinityOrNull())) {
                singlePkgs.add(pkg.getName());
            }
        }

        return singlePkgs;
    }

    public JComponent getSatelliteView() {
        JComponent sat = scene.createSatelliteView();
        sat.setBorder(new LineBorder(java.awt.SystemColor.activeCaptionBorder, 1));
        sat.setMaximumSize(new Dimension(sat.getPreferredSize().width, 80));
        sat.setPreferredSize(new Dimension(sat.getPreferredSize().width, 80));
        sat.invalidate();
        return sat;
    }

    public void recolorPkgsBasedOnChildAffinity() {
        for (PackageWidget pkg : pkgToWidget.values()) {
            pkg.recolorBasedOnChildAffinity();
        }
    }

    private void addFtmPackages(Set<TraceModel> firstLevelTraces, Map<String, PackageWidget> pkgs, Map<String, UMLClassWidget> classes) {
        AffinityProvider af = Controller.getInstance().getAffinity();

        for (TraceModel tm : firstLevelTraces) {
            Set<ClassModel> classSet = tm.getClassSet();
            for (ClassModel classModel : classSet) {
                String pkg = classModel.getPackageName();
                PackageWidget pkgWidget = pkgs.get(pkg);
                if (pkgWidget == null && classes.get(classModel.getName()) == null) {
                    pkgWidget = new PackageWidget(scene, pkg, editable);
                    Color affinityColor = af.getPkgAffinity(pkg).color;
                    pkgWidget.setAffinity(affinityColor);
                    scene.setNextConstructedNode(pkgWidget);
                    scene.addNode(pkgWidget.getName());
                    pkgs.put(pkg, pkgWidget);
                } else {
                    Color affinityColor = af.getPkgAffinity(pkg).color;
                    if (pkgWidget != null) {
                        pkgWidget.setAffinity(affinityColor);
                    }
                }
            }
        }
    }

    private void addSdmPackages(StaticDependencyModel sdm, Map<String, PackageWidget> pkgWidgets) {

        for (JPackage pack : sdm.getPackages()) {
            String pkg = pack.getQualName();
            PackageWidget pkgWidget = pkgWidgets.get(pkg);
            if (pkgWidget == null) {
                pkgWidget = new PackageWidget(scene, pkg, editable);
                scene.setNextConstructedNode(pkgWidget);
                scene.addNode(pkgWidget.getName());
                pkgWidgets.put(pkg, pkgWidget);
            }
        }
    }

    private void addFtmClasses(Map<String, PackageWidget> pkgToWidget, Set<TraceModel> firstLevelTraces, Map<String, UMLClassWidget> classes) {
        AffinityProvider af = Controller.getInstance().getAffinity();

        for (TraceModel pack : firstLevelTraces) {
            Set<ClassModel> classSet = pack.getClassSet();
            for (ClassModel classModel : classSet) {
                String clazz = classModel.getName();
                UMLClassWidget classWidget = classes.get(clazz);
                String pkg = classModel.getPackageName();
                PackageWidget pkgWidget = pkgToWidget.get(pkg);

                if (classWidget == null) {
                    classWidget = new UMLClassWidget(scene, clazz, false);
                    classWidget.setAffinity(af.getClassAffinity(clazz));
                    classes.put(clazz, classWidget);
                    pkgWidget.addClass(classWidget);
                } else {
                    classWidget.setAffinity(af.getClassAffinity(clazz));
                }

                addFields(classWidget, classModel);
                addMethods(classWidget, classModel);
            }
        }
    }

    private void addSdmClasses(Map<String, PackageWidget> pkgToWidget, Map<String, UMLClassWidget> classToWidget, StaticDependencyModel sdm) {

        for (JPackage pack : sdm.getPackages()) {
            for (JType type : pack.getTopLevelTypes()) {
                String clazz = type.getQualName();
                UMLClassWidget classWidget = classToWidget.get(clazz);
                String pkg = pack.getQualName();
                PackageWidget pkgWidget = pkgToWidget.get(pkg);

                if (classWidget == null) {
                    classWidget = new UMLClassWidget(scene, clazz, type.isInterfaceType());
                    classToWidget.put(clazz, classWidget);
                    pkgWidget.addClass(classWidget);
                }
            }
        }
    }

    private void addFtmCalls(Map<String, UMLClassWidget> classToWidget, Set<TraceModel> firstLevelTraces) {
        Set<String> edgeIDs = new HashSet<String>();
        //Add inter-class call dependencies
        for (TraceModel tm : firstLevelTraces) {
            for (OrderedBinaryRelation<String, Integer> rel : tm.getInterTypeInvocations()) {
                UMLClassWidget first = classToWidget.get(rel.getFirst());
                UMLClassWidget second = classToWidget.get(rel.getSecond());
                if (!edgeIDs.contains(getEdgeID(first, second))) {
                    addClassToClassDependency(first, second, scene);
                    edgeIDs.add(getEdgeID(first, second));
                }
            }
        }
    }

    private static ConnectionWidget addClassToClassDependency(UMLClassWidget from, UMLClassWidget to, StringGraphScene scene) {
        String id = getEdgeID(from, to);
        ConnectionWidget edge = (ConnectionWidget) scene.addEdge(id);

        scene.setEdgeSource(id, ((PackageWidget)from.getParentWidget()).getName());
        scene.setEdgeTarget(id, ((PackageWidget)to.getParentWidget()).getName());

        edge.setSourceAnchor(from.getAnchor());
        edge.setTargetAnchor(to.getAnchor());

        edge.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
        edge.setRouter(new DirectRouter() {

            @Override
            public List<Point> routeConnection(ConnectionWidget widget) {
                if (widget.getSourceAnchor().getRelatedWidget() != widget.getTargetAnchor().getRelatedWidget()) {
                    return super.routeConnection(widget);
                } else {
                    return Collections.emptyList();
                }
            }
        });

        edge.setLineColor(Color.GRAY);
        edge.setStroke(new BasicStroke(0.1f));
        return edge;
    }

    private static String getEdgeID(UMLClassWidget from, UMLClassWidget to) {
        return from.getFullName() + "->" + to.getFullName();
    }

    private void addMethods(UMLClassWidget classWidget, ClassModel classModel) {
        for (String method : classModel.getAllMethods()) {
            boolean contains = containsMethod(classWidget, method);
            if (!contains) {
                Color col = Controller.getInstance().getAffinity().getMethodAffinity(method).color;
                Widget op = classWidget.createOperation(method, col);
                classWidget.addOperation(op);
            }
        }
    }

    private boolean containsMethod(UMLClassWidget classWidget, String op) {
        for (Widget f : classWidget.getOperations()) {
            if (f.toString().equals(op)) {
                return true;
            }
        }
        return false;
    }

    private void addFields(UMLClassWidget classWidget, ClassModel classModel) {
        for (String field : classModel.getAllFields()) {
            boolean contains = containsField(classWidget, field);
            if (!contains) {
                Color col = Controller.getInstance().getAffinity().getFieldAffinity(field).color;
                Widget op = classWidget.createMember(field, col);
                classWidget.addMember(op);
            }
        }
    }

    private boolean containsField(UMLClassWidget classWidget, String field) {
        for (Widget f : classWidget.getMembers()) {
            if (f.toString().equals(field)) {
                return true;
            }
        }
        return false;
    }
}
