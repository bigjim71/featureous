/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.featurerelationscharacterization.graph;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.ui.FeatureUI;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JPanel;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

/**
 *
 * @author Tom
 */
public class GraphGui {

    private JPanel panel;
    private Graph graph;
    public int MAX_DEP = 0;
    private Map<String, Node> nodes = new HashMap<String, Node>();
    private final CustomizedRadialGraphView view;

    private Edge addUniqueEdge(Node n1, Node n2) {
        Edge ee = graph.getEdge(n1, n2);
        if (ee == null) {
            return graph.addEdge(n1, n2);
        } else {
            return ee;
        }
    }

    private Graph createGraph() {
        MAX_DEP = 0;
        Table nodeTable = new Table();
        nodeTable.addColumn(Keys.ID.getName(), Keys.ID.getType());
        nodeTable.addColumn(Keys.NODE_NAME.getName(), Keys.NODE_NAME.getType());
        nodeTable.addColumn(Keys.NODE_ICON.getName(), Keys.NODE_ICON.getType());

        Table edgeTable = new Table();
        edgeTable.addColumn(Keys.ID.getName(), Keys.ID.getType());
        edgeTable.addColumn(Keys.EDGE_SOURCE.getName(), Keys.EDGE_SOURCE.getType());
        edgeTable.addColumn(Keys.EDGE_TARGET.getName(), Keys.EDGE_TARGET.getType());
        edgeTable.addColumn(Keys.EDGE_STRENGTH.getName(), Keys.EDGE_STRENGTH.getType(), 1f);
        edgeTable.addColumn(Keys.EDGE_DIRECTED.getName(), Keys.EDGE_DIRECTED.getType(), true);
        edgeTable.addColumn(Keys.EDGE_CONTENTS.getName(), Keys.EDGE_CONTENTS.getType(), "");

        graph = new Graph(nodeTable, edgeTable, true, Keys.ID.getName(),
                Keys.EDGE_SOURCE.getName(), Keys.EDGE_TARGET.getName());

        createNodes(graph);

        createDeps();

        return graph;
    }

    private void createNodes(Graph g) {
        Set<String> selFeats = Controller.getInstance().getTraceSet().getSelectionManager().getSelectedFeats();
        for (TraceModel tm : Controller.getInstance().getTraceSet().getFirstLevelTraces()) {
            if (selFeats.isEmpty() || selFeats.contains(tm.getName())) {
                Node n = g.addNode();
                n.setInt(Keys.ID.getName(), tm.getName().hashCode());
                n.setString(Keys.NODE_NAME.getName(), tm.getName());
                n.setString(Keys.NODE_ICON.getName(), new FeatureUI().getIconShadowlessPath());
                nodes.put(tm.getName(), n);
            }
        }
    }

    private void createDeps() {
        for (Node n1 : nodes.values()) {
            TraceModel tm1 = Controller.getInstance().getTraceSet().getFirstLevelTraceByName(n1.getString(Keys.NODE_NAME.getName()));
            if (tm1 == null) {
                throw new RuntimeException("Node map out of sync with the trace set!");
            }

            for (Node n2 : nodes.values()) {
                if (n1 == n2) {
                    continue;
                }

                TraceModel tm2 = Controller.getInstance().getTraceSet().getFirstLevelTraceByName(n2.getString(Keys.NODE_NAME.getName()));
                if (tm2 == null) {
                    throw new RuntimeException("Node map out of sync with the trace set!");
                }

                List<String> tm1tm2 = new ArrayList<String>(tm1.dependsOnClasswise(tm2));
                updateMaxDep(tm1tm2.size());
                List<String> tm2tm1 = new ArrayList<String>(tm2.dependsOnClasswise(tm1));
                updateMaxDep(tm2tm1.size());
                List<String> sharing = new ArrayList<String>(tm1.shareObjectsClasswise(tm2));
                updateMaxDep(sharing.size());

                Edge e = null;
                if (tm1tm2.size() > 0) {
                    e = addUniqueEdge(n1, n2);
                    e.set(Keys.EDGE_STRENGTH.getName(), tm1tm2.size());
                    StringBuilder sb = new StringBuilder();
                    Collections.sort(tm1tm2);
                    for (String s : tm1tm2) {
                        sb.append(s);
                        sb.append("<br>");
                    }
                    e.set(Keys.EDGE_CONTENTS.getName(), sb.toString());
                } else if (tm2tm1.size() > 0) {
                    e = addUniqueEdge(n2, n1);
                    e.set(Keys.EDGE_STRENGTH.getName(), tm2tm1.size());
                    StringBuilder sb = new StringBuilder();
                    Collections.sort(tm2tm1);
                    for (String s : tm2tm1) {
                        sb.append(s);
                        sb.append("<br>");
                    }
                    e.set(Keys.EDGE_CONTENTS.getName(), sb.toString());
                } else if (sharing.size() > 0) {
                    e = addUniqueEdge(n2, n1);
                    graph.removeEdge(e);
                    e = addUniqueEdge(n1, n2);
                    e.set(Keys.EDGE_DIRECTED.getName(), false);
                    e.set(Keys.EDGE_STRENGTH.getName(), sharing.size());
                    StringBuilder sb = new StringBuilder();
                    Collections.sort(sharing);
                    for (String s : sharing) {
                        sb.append(s);
                        sb.append("<br>");
                    }
                    e.set(Keys.EDGE_CONTENTS.getName(), sb.toString());
                }

                //Affinity stuff
//                if (added) {
//                    Set<ClassModel> common = new HashSet<ClassModel>();
//                    for (ClassModel c1 : vtm.getClassSet()) {
//                        for (ClassModel c2 : traceModel.getClassSet()) {
//                            if (c1.getName().equals(c2.getName())) {
//                                Set<String> instC1 = new HashSet<String>();
//                                instC1.addAll(c1.getInstancesUsed());
//                                int firstCount = instC1.size();
//                                instC1.removeAll(c2.getInstancesUsed());
//                                if (firstCount != instC1.size()) {
//                                    common.add(c2);
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                    edgeAffinityDynamic.put(id, (common.size() > 0) ? getEdgeColor(common) : Color.DARK_GRAY);
//                }
            }
        }
    }

    private void updateMaxDep(int deps) {
        if (deps > MAX_DEP) {
            MAX_DEP = deps;
        }
    }

    /** Creates a new instance of VisualizadorGrafo */
    public GraphGui() {

        Graph graph = createGraph();
        view = new CustomizedRadialGraphView(graph, "name", MAX_DEP);
        panel = new JPanel(new BorderLayout());
        panel.add(CustomizedRadialGraphView.demo(view, graph, "name", MAX_DEP), BorderLayout.CENTER);
        panel.setVisible(true);
        panel.invalidate();
//        panel.setBounds(0, 0, 800, 600);
    }

    public CustomizedRadialGraphView getView() {
        return view;
    }

    public JPanel getPanel() {
        return panel;
    }

    public void dispose(){
        view.dispose();
    }
}
