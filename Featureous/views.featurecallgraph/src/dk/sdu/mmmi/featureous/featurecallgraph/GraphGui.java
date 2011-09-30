/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.featurecallgraph;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.OrderedBinaryRelation;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.ui.FeatureUI;
import dk.sdu.mmmi.featureous.core.ui.MethodUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.JPanel;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.visual.VisualItem;

/**
 *
 * @author Tom
 */
public class GraphGui {

    private JPanel panel;
    private Graph graph;
    public int MAX_DEP = 0;
    private Map<String, Node> nodes = new HashMap<String, Node>();
    private final CustomTreeView view;

    private Edge addUniqueEdge(Node n1, Node n2) {
        Edge ee = graph.getEdge(n1, n2);
        if (ee == null) {
            return graph.addEdge(n1, n2);
        } else {
            return ee;
        }
    }

    private Graph createGraph(String feature) {
        Table nodeTable = new Table();
        nodeTable.addColumn(Keys.ID.getName(), Keys.ID.getType());
        nodeTable.addColumn(Keys.NODE_NAME.getName(), Keys.NODE_NAME.getType());
        nodeTable.addColumn(Keys.NODE_ICON.getName(), Keys.NODE_ICON.getType());
        nodeTable.addColumn(Keys.NODE_TYPE.getName(), Keys.NODE_TYPE.getType());
        nodeTable.addColumn(Keys.NODE_CLASS.getName(), Keys.NODE_CLASS.getType());
        nodeTable.addColumn(Keys.NODE_AFFINITY.getName(), Keys.NODE_AFFINITY.getType(), Color.CYAN);
        nodeTable.addColumn(Keys.NODE_SIZE.getName(), Keys.NODE_SIZE.getType(), 1);
        nodeTable.addColumn(Keys.NODE_CONSTR.getName(), Keys.NODE_CONSTR.getType());

        Table edgeTable = new Table();
        edgeTable.addColumn(Keys.ID.getName(), Keys.ID.getType());
        edgeTable.addColumn(Keys.EDGE_SOURCE.getName(), Keys.EDGE_SOURCE.getType());
        edgeTable.addColumn(Keys.EDGE_TARGET.getName(), Keys.EDGE_TARGET.getType());
        edgeTable.addColumn(Keys.EDGE_STRENGTH.getName(), Keys.EDGE_STRENGTH.getType(), 1f);

        graph = new Graph(nodeTable, edgeTable, true, Keys.ID.getName(),
                Keys.EDGE_SOURCE.getName(), Keys.EDGE_TARGET.getName());

        createNodes(graph, feature);

        createDeps(feature);

        return graph;
    }

    public void dispose() {
        view.dispose();
    }

    private void createNodes(Graph g, String feature) {
        TraceModel tm = Controller.getInstance().getTraceSet().getFirstLevelTraceByName(feature);
        if (tm == null) {
            return;
        }

        Node n = g.addNode();
        n.setInt(Keys.ID.getName(), tm.getName().hashCode());
        n.setString(Keys.NODE_NAME.getName(), tm.getName());
        n.setString(Keys.NODE_CLASS.getName(), "");
        n.setString(Keys.NODE_ICON.getName(), new FeatureUI().getIconShadowlessPath());
        n.setString(Keys.NODE_TYPE.getName(), Keys.TYPE_FEATURE);
        nodes.put(tm.getName(), n);

        for (ClassModel cm : tm.getClassSet()) {
            for (String m : cm.getAllMethods()) {
                Node cn = g.addNode();
                cn.setInt(Keys.ID.getName(), m.hashCode());
                cn.setString(Keys.NODE_NAME.getName(), m);
                cn.setString(Keys.NODE_CLASS.getName(), cm.getName());
                cn.setString(Keys.NODE_ICON.getName(), new MethodUI().getIconShadowlessPath());
                cn.setString(Keys.NODE_TYPE.getName(), Keys.TYPE_METHOD);
                cn.set(Keys.NODE_AFFINITY.getName(), Controller.getInstance().getAffinity().getMethodAffinity(m).color);
                cn.set(Keys.NODE_SIZE.getName(), 1);
                cn.setBoolean(Keys.NODE_CONSTR.getName(), cm.isConstructor(m));
                nodes.put(m, cn);
            }
        }
    }

    private void createDeps(String feature) {
        MAX_DEP = 0;
        TraceModel tm1 = Controller.getInstance().getTraceSet().getFirstLevelTraceByName(feature);
        if (tm1 == null) {
            throw new RuntimeException("Node map out of sync with the trace set!");
        }

        Set<Node> nodess = new HashSet<Node>();
        nodess.addAll(nodes.values());

        for (Node n1 : nodess) {

            for (Node n2 : nodess) {
                if (n1 == n2) {
                    continue;
                }
                if (n1.getString(Keys.NODE_TYPE.getName()).equals(Keys.TYPE_FEATURE)
                        && n2.getString(Keys.NODE_TYPE.getName()).equals(Keys.TYPE_METHOD)) {

                    for (ClassModel cm : tm1.getClassSet()) {
                        if (cm.isFep(n2.getString(Keys.NODE_NAME.getName()))) {
                            Edge e = addUniqueEdge(n1, n2);
                            e.set(Keys.EDGE_STRENGTH.getName(), 1);
                            break;
                        }
                    }

                    continue;
                }
                if (n1.getString(Keys.NODE_TYPE.getName()).equals(Keys.TYPE_METHOD)
                        && n2.getString(Keys.NODE_TYPE.getName()).equals(Keys.TYPE_METHOD)) {
                    String s1 = n1.getString(Keys.NODE_NAME.getName());
                    String s2 = n2.getString(Keys.NODE_NAME.getName());
                    for (OrderedBinaryRelation<String, Integer> br : tm1.getMethodInvocations()) {
                        if (s1.equals(br.getFirst()) && s2.equals(br.getSecond())) {
                            Edge e = addUniqueEdge(n1, n2);
                            e.set(Keys.EDGE_STRENGTH.getName(), 1);
                            n1.setString(Keys.NODE_ICON.getName(), "dk/sdu/mmmi/featureous/icons/nb/methodCalling.png");
                            break;
                        }
                    }
                    continue;
                }
            }
        }
    }

    private void updateMaxDep(int deps) {
        if (deps > MAX_DEP) {
            MAX_DEP = deps;
        }
    }

    /** Creates a new instance of VisualizadorGrafo */
    public GraphGui(String feature) {

        Graph graph = createGraph(feature);
        view = new CustomTreeView(graph, "name") {

            public void paintDisplay(Graphics2D g2D, Dimension d) {
                // if double-locking *ALWAYS* lock on the visualization first
                synchronized (m_vis) {
                    synchronized (this) {

                        if (m_clip.isEmpty()) {
                            return; // no damage, no render
                        }
                        // map the screen bounds to absolute coords
                        m_screen.setClip(0, 0, d.width + 1, d.height + 1);
                        m_screen.transform(m_itransform);

                        // compute the approximate size of an "absolute pixel"
                        // values too large are OK (though cause unnecessary rendering)
                        // values too small will cause incorrect rendering
                        double pixel = 1.0 + 1.0 / getScale();

                        if (m_damageRedraw) {
                            if (m_clip.isInvalid()) {
                                // if clip is invalid, we clip to the entire screen
                                m_clip.setClip(m_screen);
                            } else {
                                // otherwise intersect damaged region with display bounds
                                m_clip.intersection(m_screen);
                            }

                            // expand the clip by the extra pixel margin
                            m_clip.expand(pixel);

                            // set the transform, rendering keys, etc
                            prepareGraphics(g2D);

                            // now set the actual rendering clip
                            m_rclip.setFrameFromDiagonal(
                                    m_clip.getMinX(), m_clip.getMinY(),
                                    m_clip.getMaxX(), m_clip.getMaxY());
                            g2D.setClip(m_rclip);

                            // finally, we want to clear the region we'll redraw. we clear
                            // a slightly larger area than the clip. if we don't do this,
                            // we sometimes get rendering artifacts, possibly due to
                            // scaling mismatches in the Java2D implementation
                            m_rclip.setFrameFromDiagonal(
                                    m_clip.getMinX() - pixel, m_clip.getMinY() - pixel,
                                    m_clip.getMaxX() + pixel, m_clip.getMaxY() + pixel);

                        } else {
                            // set the background region to clear
                            m_rclip.setFrame(m_screen.getMinX(), m_screen.getMinY(),
                                    m_screen.getWidth(), m_screen.getHeight());

                            // set the item clip to the current screen
                            m_clip.setClip(m_screen);

                            // set the transform, rendering keys, etc
                            prepareGraphics(g2D);
                        }

                        // now clear the region
                        clearRegion(g2D, m_rclip);

                        // -- render ----------------------------
                        // the actual rendering  loop

                        // copy current item bounds into m_rclip, reset item bounds
                        getItemBounds(m_rclip);
                        m_bounds.reset();

                        // fill the rendering and picking queues
                        m_queue.clear();   // clear the queue
                        Iterator items = m_vis.items(m_predicate);
                        for (m_visibleCount = 0; items.hasNext(); ++m_visibleCount) {
                            VisualItem item = (VisualItem) items.next();
                            Rectangle2D bounds = item.getBounds();
                            m_bounds.union(bounds); // add to item bounds

                            if (m_clip.intersects(bounds, pixel)) {
                                m_queue.addToRenderQueue(item);
                            }
                            if (item.isInteractive()) {
                                m_queue.addToPickingQueue(item);
                            }
                        }

                        // sort the rendering queue
                        m_queue.sortRenderQueue();

                        // render each visual item
                        for (int i = 0; i < m_queue.rsize; ++i) {
                            m_queue.ritems[i].render(g2D);
                        }

                        // no more damage so reset the clip
                        if (m_damageRedraw) {
                            m_clip.reset();
                        }

                        // fire bounds change, if appropriate
                        checkItemBoundsChanged(m_rclip);

                    }
                } // end synchronized block
            }
        };
        panel = new JPanel(new BorderLayout());
        panel.add(CustomTreeView.demo(view, graph, "name"), BorderLayout.CENTER);
        panel.setVisible(true);
        panel.invalidate();
//        panel.setBounds(0, 0, 800, 600);
//        panel.addComponentListener(new ComponentAdapter() {
//
//            @Override
//            public void componentResized(ComponentEvent e) {
//                view.getM_vis().run("filter");
//            }
//        });
    }

    public CustomTreeView getView() {
        return view;
    }

    public JPanel getPanel() {
        return panel;
    }
}
