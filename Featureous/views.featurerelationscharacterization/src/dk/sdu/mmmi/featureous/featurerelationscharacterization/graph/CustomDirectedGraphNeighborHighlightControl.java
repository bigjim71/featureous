/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.featurerelationscharacterization.graph;

import dk.sdu.mmmi.featureous.core.ui.OutputUtil;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Iterator;

import prefuse.Visualization;
import prefuse.action.assignment.ColorAction;
import prefuse.controls.ControlAdapter;
import prefuse.data.Graph;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 */
public class CustomDirectedGraphNeighborHighlightControl extends ControlAdapter {

    private String m_activity = null;
    private HighlightColorAction colorAction;
    private static final String sourceGroupName = "sourceNodes";
    private static final String targetGroupName = "targetNodes";
    private static final String bothGroupName = "sourceAndTargetNdoes";
    private static final String shareGroupName = "shareAndTargetNdoes";
    TupleSet sourceTupleSet = null, targetTupleSet = null, bothTupleSet = null,
            shareTupleSet = null;

    public CustomDirectedGraphNeighborHighlightControl(int[] colorPalette) {
        this(colorPalette, null);
    }

    public CustomDirectedGraphNeighborHighlightControl(int colorPalette[],
            String activity) {

        m_activity = activity;

        colorAction = new HighlightColorAction(colorPalette);

    }

    /**
     * returns the color action to be used to highlight the neighbors
     *
     * @return the highlight color action
     */
    public ColorAction getHighlightColorAction() {
        return colorAction;
    }

    public void itemEntered(VisualItem item, MouseEvent e) {

        if (sourceTupleSet == null) {
            /*
             * this delayed initialization is done to avoid that the
             * visualization has to be given to the contructor of this
             * control;
             */

            Visualization vis = item.getVisualization();
            try {
                vis.addFocusGroup(sourceGroupName);
                vis.addFocusGroup(targetGroupName);
                vis.addFocusGroup(bothGroupName);
                vis.addFocusGroup(shareGroupName);
            } catch (Exception ex) {
                OutputUtil.log("Problems over problems while adding foucs groups to visualization "
                        + ex.getMessage());
            }

            sourceTupleSet = vis.getFocusGroup(sourceGroupName);
            targetTupleSet = vis.getFocusGroup(targetGroupName);
            bothTupleSet = vis.getFocusGroup(bothGroupName);
            shareTupleSet = vis.getFocusGroup(shareGroupName);

            colorAction.setFocusGroups(new TupleSet[]{sourceTupleSet,
                        targetTupleSet, bothTupleSet, shareTupleSet});
        }

        if (item instanceof NodeItem) {
            setNeighbourHighlight((NodeItem) item, true);
        }
    }

    public void itemExited(VisualItem item, MouseEvent e) {
        if (item instanceof NodeItem) {
            setNeighbourHighlight((NodeItem) item, false);
        }

        if (m_activity != null) {
            item.getVisualization().run(m_activity);
        }
    }

    protected void setNeighbourHighlight(NodeItem centerNode,
            boolean state) {

        HashSet source = new HashSet();
        HashSet target = new HashSet();
        HashSet both = new HashSet();
        HashSet share = new HashSet();

        Iterator iterInEdges = centerNode.inEdges();
        while (iterInEdges.hasNext()) {
            EdgeItem edge = (EdgeItem) iterInEdges.next();
            NodeItem sourceNode = edge.getSourceItem();
            if (state) {
                if (!edge.getBoolean(Keys.EDGE_DIRECTED.getName())) {
                    share.add(sourceNode);
                } else {
                    source.add(sourceNode);
                }
            }
            sourceNode.setHighlighted(state);
        }

        Iterator iterOutEdges = centerNode.outEdges();
        while (iterOutEdges.hasNext()) {
            EdgeItem edge = (EdgeItem) iterOutEdges.next();
            NodeItem targetNode = edge.getTargetItem();

            if (state) {
                if (source.contains(targetNode)) {
                    both.add(targetNode);
                } else {
                    if (!edge.getBoolean(Keys.EDGE_DIRECTED.getName())) {
                        share.add(targetNode);
                    } else {
                        target.add(targetNode);
                    }
                }
            }

            targetNode.setHighlighted(state);
        }

        if (state) {
            source.removeAll(both);

            Iterator iterSource = source.iterator();
            while (iterSource.hasNext()) {
                sourceTupleSet.addTuple((NodeItem) iterSource.next());
            }

            Iterator iterTarget = target.iterator();
            while (iterTarget.hasNext()) {
                targetTupleSet.addTuple((NodeItem) iterTarget.next());
            }

            Iterator iterBoth = both.iterator();
            while (iterBoth.hasNext()) {
                bothTupleSet.addTuple((NodeItem) iterBoth.next());
            }

            Iterator iterShare = share.iterator();
            while (iterShare.hasNext()) {
                shareTupleSet.addTuple((NodeItem) iterShare.next());
            }
        } else {
            sourceTupleSet.clear();
            targetTupleSet.clear();
            bothTupleSet.clear();
            shareTupleSet.clear();
        }

        if (m_activity != null) {
            centerNode.getVisualization().run(m_activity);
        }
    }

    private class HighlightColorAction extends ColorAction {

        private int[] colorPalette;
        private TupleSet[] focusGroups;

        public HighlightColorAction(int[] colorPalette) {
            super(Graph.NODES, VisualItem.FILLCOLOR);

            this.colorPalette = colorPalette;

        }

        public void setFocusGroups(TupleSet[] focusGroups) {
            this.focusGroups = focusGroups;
        }

        public TupleSet[] getFocusGroups() {
            return this.focusGroups;
        }

        public void setColorPalette(int[] colorPalette) {
            this.colorPalette = colorPalette;
        }

        public int[] getColorPalette() {
            return this.colorPalette;
        }

        public int getColor(VisualItem item) {
            if (item == null) {
                return 0;
            }
            if (item.isHighlighted()) {
                for (int i = 0; i < 4; i++) {
                    if (focusGroups[i].containsTuple(item)) {
                        return colorPalette[i];
                    }
                }
            }
            return 0;
        }
    }
}
