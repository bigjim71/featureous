/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.featurecallgraph;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.SelectionChangeListener;
import dk.sdu.mmmi.featureous.core.model.SelectionManager;
import dk.sdu.mmmi.featureous.core.ui.UIUtils;
import dk.sdu.mmmi.featureous.icons.IconUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.openide.util.ImageUtilities;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.ItemAction;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.animate.LocationAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.animate.VisibilityAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.filter.FisheyeTreeFilter;
import prefuse.action.layout.CollapsedSubtreeLayout;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.controls.FocusControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.search.PrefixSearchTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.ImageFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.display.Clip;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.JSearchPanel;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.sort.TreeDepthItemSorter;

/**
 */
public class CustomTreeView extends Display implements SelectionChangeListener {

    private static final String tree = "tree";
    private static final String treeNodes = "tree.nodes";
    private static final String treeEdges = "tree.edges";
    private LabelRenderer m_nodeRenderer;
    private EdgeRenderer m_edgeRenderer;
    private String m_label = "label";
    private int m_orientation = Constants.ORIENT_LEFT_RIGHT;
    private final FisheyeTreeFilter ftf;

    public Visualization getM_vis() {
        return m_vis;
    }

    public Clip getM_clip() {
        return m_clip;
    }

    public void setM_clip(Clip m_clip) {
        this.m_clip = m_clip;
    }

    public CustomTreeView(Graph t, String label) {
        super(new Visualization());
        m_label = label;
        m_vis.add(tree, t);

        m_nodeRenderer = new LabelRenderer(m_label) {

            @Override
            protected String getText(VisualItem item) {
                if (item.getString(Keys.NODE_TYPE.getName()).equals(Keys.TYPE_METHOD)) {
                    String name = item.getString(Keys.NODE_NAME.getName());
                    int lastPar = name.indexOf("(");
                    name = name.substring(0, lastPar);
                    int lastDot = name.lastIndexOf(".");
                    String tmp = name.substring(0, lastDot);
                    int preLastDot = tmp.lastIndexOf(".");
                    if(item.getBoolean(Keys.NODE_CONSTR.getName())){
                        name = "new " + name.substring(lastDot + 1);
                    }else{
                        name = name.substring(preLastDot + 1);
                    }
//                    name = name.replace(".", "\n");
//                    name = name.substring(lastDot + 1);
                    return name + "(..)";
                } else {
                    return super.getText(item);
                }
            }
        };
        m_nodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
        m_nodeRenderer.setHorizontalAlignment(Constants.LEFT);
        m_nodeRenderer.setRoundedCorner(8, 8);
        m_nodeRenderer.setImageField(Keys.NODE_ICON.getName());
        m_nodeRenderer.setImageFactory(new ImageFactory() {

            @Override
            public Image getImage(String imageLocation) {
                return ImageUtilities.icon2Image(IconUtils.loadIcon(imageLocation));
            }
        });
        m_edgeRenderer = new EdgeRenderer(Constants.EDGE_TYPE_CURVE);

        DefaultRendererFactory rf = new DefaultRendererFactory(m_nodeRenderer);
        rf.add(new InGroupPredicate(treeEdges), m_edgeRenderer);
        m_vis.setRendererFactory(rf);

        // colors
        ItemAction nodeColor = new NodeColorAction(treeNodes);
        ItemAction textColor = new ColorAction(treeNodes,
                VisualItem.TEXTCOLOR, ColorLib.rgb(0, 0, 0));
        m_vis.putAction("textColor", textColor);

        ItemAction edgeColor = new ColorAction(treeEdges,
                VisualItem.STROKECOLOR, ColorLib.color(UIUtils.EDGE_COLOR));

        // quick repaint
        ActionList repaint = new ActionList();
        repaint.add(nodeColor);
        repaint.add(new RepaintAction());
        m_vis.putAction("repaint", repaint);

        // full paint
        ActionList fullPaint = new ActionList();
        fullPaint.add(nodeColor);
        m_vis.putAction("fullPaint", fullPaint);

        // animate paint change
        ActionList animatePaint = new ActionList(400);
        animatePaint.add(new ColorAnimator(treeNodes));
        animatePaint.add(new RepaintAction());
        m_vis.putAction("animatePaint", animatePaint);

        // create the tree layout action
        NodeLinkTreeLayout treeLayout = new NodeLinkTreeLayout(tree,
                m_orientation, 50, 5, 8);
//        treeLayout.setLayoutAnchor(new Point2D.Double(25, 300));
        m_vis.putAction("treeLayout", treeLayout);

        CollapsedSubtreeLayout subLayout =
                new CollapsedSubtreeLayout(tree, m_orientation);
        m_vis.putAction("subLayout", subLayout);

        AutoPanAction autoPan = new AutoPanAction();

        // create the filtering and layout
        ActionList filter = new ActionList();
        ftf = new FisheyeTreeFilter(tree, 1);
        filter.add(ftf);
        filter.add(new FontAction(treeNodes, FontLib.getFont("Tahoma", 16)));
        filter.add(treeLayout);
        filter.add(subLayout);
        filter.add(textColor);
        filter.add(nodeColor);
        filter.add(edgeColor);
        m_vis.putAction("filter", filter);

        // animated transition
        ActionList animate = new ActionList(500);
        animate.setPacingFunction(new SlowInSlowOutPacer());
        animate.add(autoPan);
        animate.add(new QualityControlAnimator());
        animate.add(new VisibilityAnimator(tree));
        animate.add(new LocationAnimator(treeNodes));
        animate.add(new ColorAnimator(treeNodes));
        animate.add(new RepaintAction());
        m_vis.putAction("animate", animate);
        m_vis.alwaysRunAfter("filter", "animate");

        // create animator for orientation changes
        ActionList orient = new ActionList(1000);
        orient.setPacingFunction(new SlowInSlowOutPacer());
        orient.add(autoPan);
        orient.add(new QualityControlAnimator());
        orient.add(new LocationAnimator(treeNodes));
        orient.add(new RepaintAction());
        m_vis.putAction("orient", orient);

        // ------------------------------------------------

        // initialize the display
        setItemSorter(new TreeDepthItemSorter());
        addControlListener(ztf);
        addControlListener(new ZoomControl());
        addControlListener(new WheelZoomControl());
        addControlListener(new PanControl());
        addControlListener(fc);

        // ------------------------------------------------

        // filter graph and perform layout
        setOrientation(m_orientation);
        m_vis.run("filter");

        PrefixSearchTupleSet search = new PrefixSearchTupleSet();
        search.setDelimiterString(search.getDelimiterString() + ".");
        m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, search);
        search.addTupleSetListener(new TupleSetListener() {

            public void tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) {
                m_vis.cancel("animatePaint");
                m_vis.run("fullPaint");
                m_vis.run("animatePaint");
            }
        });
        Controller.getInstance().getTraceSet().getSelectionManager().addSelectionListener(this);
        m_vis.putAction("lay", new Action() {

            @Override
            public void run(double d) {
                ztf.mouseClicked(new MouseEvent(CustomTreeView.this,
                        0, 0, Control.RIGHT_MOUSE_BUTTON, 1, 1, 1, false));
            }
        });

        m_vis.runAfter("animate", "lay");
        addResizeChangeZoom();
    }

    private void addResizeChangeZoom() {
        addComponentListener(new ComponentAdapter() {

            private boolean first = false;

            @Override
            public void componentShown(ComponentEvent ce) {
                super.componentResized(ce);
            }

            @Override
            public void componentResized(ComponentEvent e) {
                // This is a hack - skip first notification due to layout problems
                if (first) {
                    ztf.mouseClicked(new MouseEvent(CustomTreeView.this,
                            0, 0, Control.RIGHT_MOUSE_BUTTON, 1, 1, 1, false));
                } else {
                    first = true;
                }
            }
        });
    }
    private final ZoomToFitControl ztf = new ZoomToFitControl();
    private final FocusControl fc = new FocusControl(1, "filter");

    public void dispose() {
        Controller.getInstance().getTraceSet().getSelectionManager().removeSelectionListener(this);
    }

    // ------------------------------------------------------------------------
    public void setOrientation(int orientation) {
        NodeLinkTreeLayout rtl = (NodeLinkTreeLayout) m_vis.getAction("treeLayout");
        CollapsedSubtreeLayout stl = (CollapsedSubtreeLayout) m_vis.getAction("subLayout");
        m_nodeRenderer.setHorizontalAlignment(Constants.LEFT);
        m_edgeRenderer.setHorizontalAlignment1(Constants.RIGHT);
        m_edgeRenderer.setHorizontalAlignment2(Constants.LEFT);
        m_edgeRenderer.setVerticalAlignment1(Constants.CENTER);
        m_edgeRenderer.setVerticalAlignment2(Constants.CENTER);
        m_orientation = orientation;
        rtl.setOrientation(orientation);
        stl.setOrientation(orientation);
    }

    public int getOrientation() {
        return m_orientation;
    }

    // ------------------------------------------------------------------------
    public static JComponent demo(CustomTreeView view, Graph graph, final String label) {
        Color BACKGROUND = Color.WHITE;
        Color FOREGROUND = Color.BLACK;

        // create a new treemap
        view.setBackground(BACKGROUND);
        view.setForeground(FOREGROUND);

        // create a search panel for the tree map
        JSearchPanel search = new JSearchPanel(view.getVisualization(),
                treeNodes, Visualization.SEARCH_ITEMS, label, true, true);
        search.setShowResultCount(true);
        search.setBorder(BorderFactory.createEmptyBorder(5, 5, 4, 0));
        search.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 11));
        search.setBackground(BACKGROUND);
        search.setForeground(FOREGROUND);

        final JFastLabel title = new JFastLabel("                 ");
        title.setPreferredSize(new Dimension(350, 20));
        title.setVerticalAlignment(SwingConstants.BOTTOM);
        title.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        title.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 12));
        title.setBackground(BACKGROUND);
        title.setForeground(FOREGROUND);

        view.addControlListener(new ControlAdapter() {

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
        box.setBackground(BACKGROUND);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.setForeground(FOREGROUND);
        panel.add(view, BorderLayout.CENTER);
        panel.add(box, BorderLayout.SOUTH);

        return panel;
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
            //Method sel
            String name = vi.getString(Keys.NODE_NAME.getName());
            boolean isMethodSel = tl.getSelectedExecs().contains(name);
            int ms = tl.getSelectedExecs().size();

            //ClassSel
            String cName = vi.getString(Keys.NODE_CLASS.getName());
            boolean isClassSel = tl.getSelectedClasses().contains(cName);
            int cs = tl.getSelectedClasses().size();

            String pkg = "";
            for (ClassModel cm : Controller.getInstance().getTraceSet().getAllClassIDs()) {
                if (cm.getName().equals(cName)) {
                    pkg = cm.getPackageName();
                    break;
                }
            }
            boolean isPkgSel = tl.getSelectedPkgs().contains(pkg);
            int ps = tl.getSelectedPkgs().size();

            if (isPkgSel && cs == 0 && ms == 0) {
//                fc.itemClicked(vi, new MouseEvent(this, 0, 0, Control.LEFT_MOUSE_BUTTON, 1, 1, 1, false));
                vi.setHighlighted(true);
            } else if (isClassSel && ms == 0) {
//                fc.itemClicked(vi, new MouseEvent(this, 0, 0, Control.LEFT_MOUSE_BUTTON, 1, 1, 1, false));
                vi.setHighlighted(true);
            } else if (isMethodSel) {
//                fc.itemEntered(vi, new MouseEvent(this, 0, 0, Control.LEFT_MOUSE_BUTTON, 1, 1, 1, false));
                if (!m_vis.isInGroup(vi, Visualization.FOCUS_ITEMS)) {
                    fc.itemClicked(vi, new MouseEvent(this, 0, 0, Control.LEFT_MOUSE_BUTTON, 1, 1, 1, false));
                    expandIfNeeded();
                }
//                fc.itemExited(vi, new MouseEvent(this, 0, 0, Control.LEFT_MOUSE_BUTTON, 1, 1, 1, false));
//                vi.setHighlighted(true);
            } else {
                if (m_vis.isInGroup(vi, Visualization.FOCUS_ITEMS)) {
                    fc.itemClicked(vi, new MouseEvent(this, 0, 0, 0, 1, 1, 1, false));
                }
                vi.setHighlighted(false);
            }
        }
//        m_vis.cancel("animatePaint");
        m_vis.run("fullPaint");
//        m_vis.run("animatePaint");
    }

    private void expandIfNeeded() {
        SelectionManager sm = Controller.getInstance().getTraceSet().getSelectionManager();
        if (sm.getSelectedClasses().isEmpty() && sm.getSelectedPkgs().isEmpty() && sm.getSelectedExecs().isEmpty()) {
            if (ftf.getDistance() > 2) {
                ftf.setDistance(2);
            }
        } else {
            if (ftf.getDistance() < 100) {
                ftf.setDistance(100);
            }
        }
        m_vis.run("filter");
    }

    // ------------------------------------------------------------------------
    public class AutoPanAction extends Action {

        private Point2D m_start = new Point2D.Double();
        private Point2D m_end = new Point2D.Double();
        private Point2D m_cur = new Point2D.Double();
        private int m_bias = 150;

        public void run(double frac) {
            TupleSet ts = m_vis.getFocusGroup(Visualization.FOCUS_ITEMS);
            if (ts.getTupleCount() == 0) {
                return;
            }

            if (frac == 0.0) {
                int xbias = 0, ybias = 0;
                switch (m_orientation) {
                    case Constants.ORIENT_LEFT_RIGHT:
                        xbias = m_bias;
                        break;
                    case Constants.ORIENT_RIGHT_LEFT:
                        xbias = -m_bias;
                        break;
                    case Constants.ORIENT_TOP_BOTTOM:
                        ybias = m_bias;
                        break;
                    case Constants.ORIENT_BOTTOM_TOP:
                        ybias = -m_bias;
                        break;
                }

                VisualItem vi = (VisualItem) ts.tuples().next();
                m_cur.setLocation(getWidth() / 2, getHeight() / 2);
                getAbsoluteCoordinate(m_cur, m_start);
                m_end.setLocation(vi.getX() + xbias, vi.getY() + ybias);
            } else {
                m_cur.setLocation(m_start.getX() + frac * (m_end.getX() - m_start.getX()),
                        m_start.getY() + frac * (m_end.getY() - m_start.getY()));
                panToAbs(m_cur);
            }
        }
    }

    public static class NodeColorAction extends ColorAction {

        public NodeColorAction(String group) {
            super(group, VisualItem.FILLCOLOR, ColorLib.color(UIUtils.NODE_COLOR));
        }

        public int getColor(VisualItem item) {
            if (m_vis.isInGroup(item, Visualization.SEARCH_ITEMS)) {
                return ColorLib.color(UIUtils.HIGHLIGHT_COLOR);
            } else if (item.isHighlighted()) {
                return ColorLib.color(UIUtils.HIGHLIGHT_COLOR);
            } else if (m_vis.isInGroup(item, Visualization.FOCUS_ITEMS)) {
                return ColorLib.color(UIUtils.SELECTION_COLOR);
            } else if (item.getString(Keys.NODE_TYPE.getName()).equals(Keys.TYPE_METHOD)) {
                return ColorLib.color((Color) item.get(Keys.NODE_AFFINITY.getName()));
            }
            return ColorLib.color(UIUtils.NODE_COLOR);
        }
    } // end of inner class TreeMapColorAction
} // end of class TreeMap

