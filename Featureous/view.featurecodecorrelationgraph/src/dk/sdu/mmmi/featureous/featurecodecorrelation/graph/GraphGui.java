/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.featurecodecorrelation.graph;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.ui.ClassUI;
import dk.sdu.mmmi.featureous.core.ui.FeatureUI;
import dk.sdu.mmmi.featureous.core.ui.PackageUI;
import dk.sdu.mmmi.featureous.explorer.api.GranularityChooserMenu;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JPanel;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

public class GraphGui {

    private JPanel panel;
    private Graph graph;
    public int MAX_DEP = 0;
    public int MAX_SIZE = 0;
    private Map<String, Node> nodes = new HashMap<String, Node>();

    private Edge addUniqueEdge(Node n1, Node n2) {
        Edge ee = graph.getEdge(n1, n2);
        if (ee == null) {
            return graph.addEdge(n1, n2);
        } else {
            return ee;
        }
    }

    private Graph createGraph(Set<String> selFeats) {
        Table nodeTable = new Table();
        nodeTable.addColumn(Keys.ID.getName(), Keys.ID.getType());
        nodeTable.addColumn(Keys.NODE_NAME.getName(), Keys.NODE_NAME.getType());
        nodeTable.addColumn(Keys.NODE_ICON.getName(), Keys.NODE_ICON.getType());
        nodeTable.addColumn(Keys.NODE_TYPE.getName(), Keys.NODE_TYPE.getType());
        nodeTable.addColumn(Keys.NODE_AFFINITY.getName(), Keys.NODE_AFFINITY.getType(), Color.CYAN);
        nodeTable.addColumn(Keys.NODE_SIZE.getName(), Keys.NODE_SIZE.getType(), 1);

        Table edgeTable = new Table();
        edgeTable.addColumn(Keys.ID.getName(), Keys.ID.getType());
        edgeTable.addColumn(Keys.EDGE_SOURCE.getName(), Keys.EDGE_SOURCE.getType());
        edgeTable.addColumn(Keys.EDGE_TARGET.getName(), Keys.EDGE_TARGET.getType());
        edgeTable.addColumn(Keys.EDGE_STRENGTH.getName(), Keys.EDGE_STRENGTH.getType(), 1f);

        graph = new Graph(nodeTable, edgeTable, true, Keys.ID.getName(),
                Keys.EDGE_SOURCE.getName(), Keys.EDGE_TARGET.getName());

        createNodes(graph, selFeats);

        createDeps();

        return graph;
    }

    private void createNodes(Graph g, Set<String> selFeats) {
        MAX_SIZE = 0;
        Set<TraceModel> tms = new HashSet<TraceModel>();
        for (TraceModel tm : Controller.getInstance().getTraceSet().getFirstLevelTraces()) {
            if (!selFeats.contains(tm.getName())) {
                continue;
            }
            tms.add(tm);
            Node n = g.addNode();
            n.setInt(Keys.ID.getName(), tm.getName().hashCode());
            n.setString(Keys.NODE_NAME.getName(), tm.getName());
            n.setString(Keys.NODE_ICON.getName(), new FeatureUI().getIconShadowlessPath());
            n.setString(Keys.NODE_TYPE.getName(), Keys.TYPE_FEATURE);
            nodes.put(tm.getName(), n);
        }

        if (gc.getValue() == GranularityChooserMenu.CLASS_GRANULARITY) {
            Set<ClassModel> cs = new HashSet<ClassModel>();
            for (TraceModel tm : tms) {
                cs.addAll(tm.getClassSet());
            }
            for (ClassModel cm : cs) {
                Node cn = g.addNode();
                cn.setInt(Keys.ID.getName(), cm.getName().hashCode());
                cn.setString(Keys.NODE_NAME.getName(), cm.getName());
                cn.setString(Keys.NODE_ICON.getName(), new ClassUI().getIconShadowlessPath());
                cn.setString(Keys.NODE_TYPE.getName(), Keys.TYPE_CLASS);
                cn.set(Keys.NODE_AFFINITY.getName(), 
                        Controller.getInstance().getAffinity().getClassAffinity(cm.getName()).color);
                Set<String> mSet = new HashSet<String>();
                for (TraceModel tmm : tms) {
                    ClassModel cmm = tmm.getClass(cm.getName());
                    if (cmm != null) {
                        mSet.addAll(cmm.getAllMethods());
                    }
                }
                cn.set(Keys.NODE_SIZE.getName(), mSet.size());
                updateMaxSize(mSet.size());
                nodes.put(cm.getName(), cn);
            }
        } else if (gc.getValue() == GranularityChooserMenu.PACKAGE_GRANULARITY) {
            Set<String> cs = new HashSet<String>();
            for (TraceModel tm : tms) {
                for(ClassModel cm : tm.getClassSet()){
                    cs.add(cm.getPackageName());
                }
            }
            for (String cm : cs) {
                Node cn = g.addNode();
                cn.setInt(Keys.ID.getName(), cm.hashCode());
                cn.setString(Keys.NODE_NAME.getName(), cm);
                cn.setString(Keys.NODE_ICON.getName(), new PackageUI().getIconShadowlessPath());
                cn.setString(Keys.NODE_TYPE.getName(), Keys.TYPE_PACKAGE);
                cn.set(Keys.NODE_AFFINITY.getName(), 
                        Controller.getInstance().getAffinity().getPkgAffinity(cm).color);
                Set<String> mSet = new HashSet<String>();
                for (TraceModel tmm : tms) {
                    for(ClassModel cmm : tmm.getClassSet()){
                        if(cmm.getPackageName().equals(cm)){
                            mSet.add(cmm.getName());
                        }
                    }
                }
                cn.set(Keys.NODE_SIZE.getName(), mSet.size());
                updateMaxSize(mSet.size());
                nodes.put(cm, cn);
            }
        }
    }

    private void createDeps() {
        MAX_DEP = 0;
        for (Node n1 : nodes.values()) {
            if (!n1.getString(Keys.NODE_TYPE.getName()).equals(Keys.TYPE_FEATURE)) {
                continue;
            }

            TraceModel tm1 = Controller.getInstance().getTraceSet().getFirstLevelTraceByName(n1.getString(Keys.NODE_NAME.getName()));
            if (tm1 == null) {
                throw new RuntimeException("Node map out of sync with the trace set!");
            }

            for (Node n2 : nodes.values()) {
                if (!n2.getString(Keys.NODE_TYPE.getName()).equals(Keys.TYPE_CLASS)
                    && !n2.getString(Keys.NODE_TYPE.getName()).equals(Keys.TYPE_PACKAGE)) {
                    continue;
                }

                if(n2.getString(Keys.NODE_TYPE.getName()).equals(Keys.TYPE_CLASS)){
                    ClassModel cm = tm1.getClass(n2.getString(Keys.NODE_NAME.getName()));
                    if (cm != null) {
                        Edge e = addUniqueEdge(n1, n2);
                        e.set(Keys.EDGE_STRENGTH.getName(), cm.getAllMethods().size());
                        updateMaxDep(cm.getAllMethods().size());
                    }
                }else if(n2.getString(Keys.NODE_TYPE.getName()).equals(Keys.TYPE_PACKAGE)){
                    int deps = 0;
                    for(ClassModel cm : tm1.getClassSet()){
                        if(cm.getPackageName().equals(n2.getString(Keys.NODE_NAME.getName()))){
                            deps++;
                        }
                    }
                    if(deps>0){
                        Edge e = addUniqueEdge(n1, n2);
                        e.set(Keys.EDGE_STRENGTH.getName(), deps);
                        updateMaxDep(deps);
                    }
                }
            }
        }
    }

    private void updateMaxDep(int deps) {
        if (deps > MAX_DEP) {
            MAX_DEP = deps;
        }
    }

    private void updateMaxSize(int size) {
        if (size > MAX_SIZE) {
            MAX_SIZE = size;
        }
    }

    public void dispose() {
        view.dispose();
    }
    private CustomizedRadialGraphView view;
    private final GranularityChooserMenu gc;

    public GraphGui(Set<String> selFeats, GranularityChooserMenu gc) {

        this.gc = gc;

        Graph graph = createGraph(selFeats);
        view = new CustomizedRadialGraphView(graph, "name", MAX_DEP);
        view.addMouseListener(gc.getPopupListener());
        panel = CustomizedRadialGraphView.demo(view, "name", MAX_SIZE);
    }

    public CustomizedRadialGraphView getView() {
        return view;
    }

    public JPanel getPanel() {
        return panel;
    }
}
