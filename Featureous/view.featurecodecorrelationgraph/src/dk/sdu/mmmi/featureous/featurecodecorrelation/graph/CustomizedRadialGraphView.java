/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.featurecodecorrelation.graph;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.SelectionManager;
import dk.sdu.mmmi.featureous.core.model.SelectionChangeListener;
import dk.sdu.mmmi.featureous.core.ui.UIUtils;
import dk.sdu.mmmi.featureous.icons.IconUtils;
import dk.sdu.mmmi.featureous.lib.prefuse_profusians.EdgeHighlightControl;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.openide.util.ImageUtilities;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.GroupAction;
import prefuse.action.ItemAction;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.animate.PolarLocationAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.animate.VisibilityAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.HoverActionControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.query.SearchQueryBinding;
import prefuse.data.search.PrefixSearchTupleSet;
import prefuse.data.search.SearchTupleSet;
import prefuse.data.tuple.DefaultTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.ImageFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.StrokeLib;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.JSearchPanel;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.sort.TreeDepthItemSorter;
import profusians.controls.DirectedGraphNeighborHighlightControl;
import profusians.render.SmoothEdgeRenderer;

public class CustomizedRadialGraphView extends Display implements SelectionChangeListener {

    private static final String graph = "graph";
    private static final String graphNodes = "graph.nodes";
    private static final String graphEdges = "graph.edges";
    private static final String linear = "linear";
    private LabelRenderer m_nodeRenderer;
    private EdgeRenderer m_edgeRenderer;
    private String m_label = "label";

    public CustomizedRadialGraphView(Graph g, String label, final int MAX_DEP) {
        super(new Visualization());
        m_label = label;

        // -- set up visualization --
        m_vis.add(graph, g);
        m_vis.setInteractive(graphEdges, null, true);

        // -- set up renderers --
        m_nodeRenderer = new FrontLimitedLabelRenderer(label, 30);//LabelRenderer(m_label);
        m_nodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_DRAW_AND_FILL);
        m_nodeRenderer.setHorizontalAlignment(Constants.CENTER);
        m_nodeRenderer.setRoundedCorner(8, 8);
        m_nodeRenderer.setImageField(Keys.NODE_ICON.getName());
        m_nodeRenderer.setImageFactory(new ImageFactory() {

            @Override
            public Image getImage(String imageLocation) {
                return ImageUtilities.icon2Image(IconUtils.loadIcon(imageLocation));
            }
        });

        m_edgeRenderer = new SmoothEdgeRenderer();
        m_edgeRenderer.setEdgeType(Constants.EDGE_TYPE_CURVE);

        m_edgeRenderer.setArrowHeadSize(10, 10);

        m_edgeRenderer.setRenderType(EdgeRenderer.RENDER_TYPE_DRAW_AND_FILL);
        DefaultRendererFactory rf = new DefaultRendererFactory(m_nodeRenderer);

        rf.add(new InGroupPredicate(graphEdges), m_edgeRenderer);

        m_vis.setRendererFactory(rf);

        // colors
        ItemAction edgeStroke = new StrokeAction(graphEdges) {

            @Override
            public BasicStroke getStroke(VisualItem item) {
                float str = 0.3f + 7.0f * item.getFloat(Keys.EDGE_STRENGTH.getName()) / MAX_DEP;
                return StrokeLib.getDerivedStroke(item.getStroke(), str);
            }
        };

        ColorAction fillNode = new AffinityNodeColorAction(graphNodes, VisualItem.FILLCOLOR);
        ItemAction textColor = new TextColorAction(graphNodes);
        m_vis.putAction("textColor", textColor);

        ColorAction strokeEdges = new ColorAction(graphEdges, VisualItem.STROKECOLOR, ColorLib.color(UIUtils.EDGE_COLOR));
        ColorAction fillEdges = new ColorAction(graphEdges, VisualItem.FILLCOLOR, ColorLib.color(UIUtils.EDGE_COLOR));
        FontAction fonts = new FontAction(graphNodes,
                FontLib.getFont("Tahoma", 10));

        fonts.add("ingroup('_focus_')",
                FontLib.getFont(
                "Tahoma", 11));

        // recolor
        ActionList recolor = new ActionList();
        recolor.add(getNodeRenderAction(fillNode, strokeEdges, fillEdges));
        recolor.add(textColor);
        m_vis.putAction("recolor", recolor);

        // repaint
        ActionList repaint = new ActionList();
        repaint.add(recolor);
        recolor.add(edgeStroke);
        repaint.add(new RepaintAction());
        m_vis.putAction("repaint", repaint);

        // animate paint change
        ActionList animatePaint = new ActionList(400);

        animatePaint.add(new ColorAnimator(graphNodes));
        animatePaint.add(new RepaintAction());
        m_vis.putAction("animatePaint", animatePaint);

        // create the tree layout action
//        RadialTreeLayout treeLayout = new RadialTreeLayout(graph);
//        BalloonTreeLayout treeLayout = new BalloonTreeLayout(graph);
//        treeLayout.setMinRadius(100);
//        FruchtermanReingoldLayout treeLayout = new FruchtermanReingoldLayout(graph);
//        treeLayout.setMargin(0, 0, 0, 0);
        NodeLinkTreeLayout treeLayout = new NodeLinkTreeLayout(graph);
//        treeLayout.setAngularBounds(-Math.PI/2, Math.PI);
        m_vis.putAction("treeLayout", treeLayout);

//        CollapsedSubtreeLayout subLayout = new CollapsedSubtreeLayout(graph);
//        m_vis.putAction("subLayout", subLayout);

        // create the filtering and layout
        ActionList filter = new ActionList();
        filter.add(new TreeRootAction(graph));
        filter.add(fonts);
        filter.add(treeLayout);
//        filter.add(subLayout);
        filter.add(textColor);
        filter.add(fillNode);
        filter.add(strokeEdges);
        filter.add(fillEdges);
        filter.add(edgeStroke);
        m_vis.putAction("filter", filter);

        // animated transition
        ActionList animate = new ActionList(1000);

        animate.setPacingFunction(new SlowInSlowOutPacer());
        animate.add(
                new QualityControlAnimator());
        animate.add(
                new VisibilityAnimator(graph));
        animate.add(
                new PolarLocationAnimator(graphNodes, linear));
        animate.add(
                new ColorAnimator(graphNodes));
        animate.add(
                new RepaintAction());
        m_vis.putAction("animate", animate);
        m_vis.alwaysRunAfter("filter", "animate");

        // ------------------------------------------------

        // initialize the display
        setItemSorter(
                new TreeDepthItemSorter());
        addControlListener(
                new DragControl());
        addControlListener(
                ztf);
        addControlListener(
                new ZoomControl());
        addControlListener(
                new WheelZoomControl());
        addControlListener(
                new PanControl());
        addControlListener(
                fc);
        addControlListener(
                new HoverActionControl("repaint"));

        // ------------------------------------------------

        // filter graph and perform layout
        m_vis.run("filter");

        // maintain a set of items that should be interpolated linearly
        // this isn't absolutely necessary, but makes the animations nicer
        // the PolarLocationAnimator should read this set and act accordingly
        m_vis.addFocusGroup(linear, new DefaultTupleSet());
        m_vis.getGroup(Visualization.FOCUS_ITEMS).addTupleSetListener(
                new TupleSetListener() {

                    public void tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) {
                        TupleSet linearInterp = m_vis.getGroup(linear);
                        if (add.length < 1 || !(add[0] instanceof Node)) {
                            return;
                        }
                        linearInterp.clear();
                        for (Node n = (Node) add[0]; n != null; n = n.getParent()) {
                            linearInterp.addTuple(n);
                        }
                    }
                });

        PrefixSearchTupleSet search = new PrefixSearchTupleSet(false);
        search.setDelimiterString(". \t\n\r");
        m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, search);
        search.addTupleSetListener(new TupleSetListener() {

            public void tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) {
                m_vis.cancel("animatePaint");
                m_vis.run("recolor");
                m_vis.run("animatePaint");
            }
        });


        m_vis.putAction("lay", new Action() {

            @Override
            public void run(double d) {
                ztf.mouseClicked(new MouseEvent(CustomizedRadialGraphView.this,
                        0, 0, Control.RIGHT_MOUSE_BUTTON, 1, 1, 1, false));
            }
        });

        m_vis.runAfter("animate", "lay");
        Controller.getInstance().getTraceSet().getSelectionManager().addSelectionListener(this);
    }
    private final FocusControl fc = new FocusControl(1, "filter");

    private ActionList getNodeRenderAction(ColorAction strokeNodes, ColorAction strokeEdges, ColorAction fillEdges) {
        // -- set up processing actions --
        EdgeHighlightControl ehc = new EdgeHighlightControl(
                new int[]{ColorLib.rgb(ColorLib.color(UIUtils.HIGHLIGHT_COLOR),
                    ColorLib.color(UIUtils.HIGHLIGHT_COLOR),
                    ColorLib.color(UIUtils.SELECTION_COLOR))});

        int[] cols = new int[]{ColorLib.color(UIUtils.HIGHLIGHT_COLOR),
            ColorLib.color(UIUtils.HIGHLIGHT_COLOR),
            ColorLib.color(UIUtils.HIGHLIGHT_COLOR)};

        DirectedGraphNeighborHighlightControl nhc = new DirectedGraphNeighborHighlightControl(
                cols);

//        fillNodes.add(VisualItem.FIXED, ColorLib.rgb(200, 200, 255));
        // fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255,200,125));
        // MAD - here we define the colors in which the neighbour nodes should
        // be filled
        strokeNodes.add(VisualItem.HOVER, ehc.getNodeColorAction());
        strokeNodes.add(VisualItem.HIGHLIGHT, nhc.getHighlightColorAction());
//        fillNodes.add("ingroup('_search_')", ColorLib.rgb(250, 250, 150));
        // ItemAction edgeColor = new ColorAction(edges, VisualItem.FILLCOLOR,
        // ColorLib.gray(200));
        // ItemAction edgeColor2 = new ColorAction(edges,
        // VisualItem.STROKECOLOR, ColorLib.gray(200));
        ActionList draw = new ActionList();
        draw.add(strokeNodes);
        draw.add(new ColorAction(graphNodes, VisualItem.STROKECOLOR, 0));
        draw.add(new ColorAction(graphNodes, VisualItem.TEXTCOLOR, ColorLib.rgb(0, 0, 0)));

        DirectedGraphNeighborEdgeHighlightControl nehc = new DirectedGraphNeighborEdgeHighlightControl(
                cols);

        strokeEdges.add(VisualItem.FIXED, ColorLib.rgb(200, 200, 200));
        strokeEdges.add(VisualItem.HIGHLIGHT, nehc.getHighlightColorAction());
        strokeEdges.add(VisualItem.HOVER, ehc.getEdgeColorAction());

        draw.add(strokeEdges);


        fillEdges.add(VisualItem.FIXED, ColorLib.rgb(200, 200, 200));
        fillEdges.add(VisualItem.HIGHLIGHT, nehc.getHighlightColorAction());
        fillEdges.add(VisualItem.HOVER, ehc.getEdgeColorAction());

        draw.add(fillEdges);

        addControlListener(nhc);
        addControlListener(nehc);
        addControlListener(ehc);

        return draw;
    }

    // ------------------------------------------------------------------------
    public static JPanel demo(CustomizedRadialGraphView gview, final String label, int MAX_SIZE) {
        // create a new radial tree view
        Visualization vis = gview.getVisualization();
        Iterator<VisualItem> vii = vis.items(graphNodes);
        while (vii.hasNext()) {
            VisualItem vi = vii.next();
            String type = vi.getString(Keys.NODE_TYPE.getName());
            if (type.equals(Keys.TYPE_FEATURE)) {
                vi.setSize(2);
            } else {
                int size = vi.getInt(Keys.NODE_SIZE.getName());
                float mulHeight = 1f + ((float) size) / ((float) MAX_SIZE);
                vi.setSize(mulHeight);
            }
        }

        // create a search panel for the tree map
        SearchQueryBinding sq = new SearchQueryBinding(
                (Table) vis.getGroup(graphNodes), label,
                (SearchTupleSet) vis.getGroup(Visualization.SEARCH_ITEMS));
        JSearchPanel search = sq.createSearchPanel();
        search.setShowResultCount(true);
        search.setBorder(BorderFactory.createEmptyBorder(5, 5, 4, 0));
        search.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 11));

        final JFastLabel title = new JFastLabel("                 ");
        title.setPreferredSize(new Dimension(350, 20));
        title.setVerticalAlignment(SwingConstants.BOTTOM);
        title.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        title.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 12));

        gview.addControlListener(new ControlAdapter() {

            public void itemEntered(VisualItem item, MouseEvent e) {
                if (item.canGetString(label)) {
                    title.setText(item.getString(label));
                }
            }

            public void itemExited(VisualItem item, MouseEvent e) {
                title.setText(null);
            }
        });

        Box box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createHorizontalStrut(10));
        box.add(title);
        box.add(Box.createHorizontalGlue());
        box.add(search);
        box.add(Box.createHorizontalStrut(3));

        JPanel panel = new JPanel(new BorderLayout());

        Color BACKGROUND = Color.WHITE;
        Color FOREGROUND = Color.DARK_GRAY;
        UILib.setColor(panel, BACKGROUND, FOREGROUND);

        panel.add(box, BorderLayout.SOUTH);
        panel.add(gview, BorderLayout.CENTER);

        gview.addResizeChangeZoom();

        return panel;
    }

    private void addResizeChangeZoom() {
        addComponentListener(new ComponentAdapter() {

            private int first = 0;

            @Override
            public void componentResized(ComponentEvent e) {
                // This is a hack - skip first notification due to layout problems
                if (first > 1) {
                    ztf.mouseClicked(new MouseEvent(CustomizedRadialGraphView.this,
                            0, 0, Control.RIGHT_MOUSE_BUTTON, 1, 1, 1, false));
                } else {
                    first++;
                }
            }
        });
    }
    private final ZoomToFitControl ztf = new ZoomToFitControl();

    public void dispose() {
        Controller.getInstance().getTraceSet().getSelectionManager().removeSelectionListener(this);
    }

    @Override
    public void featureSelectionChanged(SelectionManager tl) {
    }

    @Override
    public void compUnitSelectionChanged(SelectionManager tl) {
        Iterator<VisualItem> i = m_vis.items();
        while (i.hasNext()) {
            VisualItem vi = i.next();
            if (!(vi instanceof Node)) {
                continue;
            }
            String name = vi.getString(Keys.NODE_NAME.getName());
            if (tl.getSelectedClasses().contains(name)) {
                vi.setHover(true);
                fc.itemClicked(vi, new MouseEvent(this, 0, 0, Control.LEFT_MOUSE_BUTTON, 1, 1, 1, false));
            } else {
                vi.setHover(false);
                fc.itemClicked(vi, new MouseEvent(this, 0, 0, 0, 1, 1, 1, false));
            }
        }
//        m_vis.cancel("animatePaint");
        m_vis.run("repaint");
//        m_vis.run("animatePaint");
    }

    // ------------------------------------------------------------------------
    /**
     * Switch the root of the tree by requesting a new spanning tree
     * at the desired root
     */
    public static class TreeRootAction extends GroupAction {

        public TreeRootAction(String graphGroup) {
            super(graphGroup);
        }

        public void run(double frac) {
            TupleSet focus = m_vis.getGroup(Visualization.FOCUS_ITEMS);
            if (focus == null || focus.getTupleCount() == 0) {
                return;
            }

            Graph g = (Graph) m_vis.getGroup(m_group);
            Object fo = null;
            Node f = null;
            Iterator tuples = focus.tuples();
            while (tuples.hasNext() && (fo = tuples.next()) != null && fo instanceof Node && !g.containsTuple(f = (Node) fo)) {
                f = null;
                fo = null;
            }
            if (f == null) {
                return;
            }
            g.getSpanningTree(f);
        }
    }

    /**
     * Set node fill colors
     */
    public static class AffinityNodeColorAction extends ColorAction {

        public AffinityNodeColorAction(String group, String target) {
            super(group, target, ColorLib.color(UIUtils.NODE_COLOR));
            add("_hover", ColorLib.color(UIUtils.HIGHLIGHT_COLOR));
            add("ingroup('_search_')", ColorLib.color(UIUtils.HIGHLIGHT_COLOR));
//            add("ingroup('_focus_')", ColorLib.rgb(198, 229, 229));
        }

        @Override
        public int getColor(VisualItem item) {
            String type = item.getString(Keys.NODE_TYPE.getName());

            if ((type.equals(Keys.TYPE_CLASS) || type.equals(Keys.TYPE_PACKAGE))
                    && !item.isHighlighted() && !item.isHover() && !item.isInGroup("_search_")) {
                Color col = (Color) item.get(Keys.NODE_AFFINITY.getName());
                return ColorLib.color(col);
            } else {
                return super.getColor(item);
            }
        }
    } // end of inner class NodeColorAction

    public static class TextColorAction extends ColorAction {

        public TextColorAction(String group) {
            super(group, VisualItem.TEXTCOLOR, ColorLib.color(Color.BLACK));
            add("_hover", ColorLib.color(Color.BLACK));
        }
    } // end of inner class TextColorAction
} // end of class RadialGraphView

