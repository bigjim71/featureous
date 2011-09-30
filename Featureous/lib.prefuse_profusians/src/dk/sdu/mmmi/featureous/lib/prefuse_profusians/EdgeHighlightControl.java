/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.lib.prefuse_profusians;

import java.awt.event.MouseEvent;

import prefuse.Visualization;
import prefuse.action.assignment.ColorAction;
import prefuse.controls.ControlAdapter;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ColorLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 * 
 */
public class EdgeHighlightControl extends ControlAdapter {

    private String m_activity = null;
    private HoverColorAction edgeColorAction;
    private HoverColorAction nodeColorAction;
    private static final String sourceNodeGroupName = "sourceNodeE";
    private static final String targetNodeGroupName = "targetNodeE";
    private static final String selectedEdge = "selectedEdge";
    TupleSet sourceNodeTupleSet = null, targetNodeTupleSet = null, selectedEdgeTupleSet = null;

    public EdgeHighlightControl(int[] colorPalette) {
        this(colorPalette, null);
    }

    public EdgeHighlightControl(int colorPalette[],
            String activity) {

        m_activity = activity;

        edgeColorAction = new HoverColorAction(colorPalette, Graph.EDGES);
        nodeColorAction = new HoverColorAction(colorPalette, Graph.NODES);

    }

    public ColorAction getEdgeColorAction() {
        return edgeColorAction;
    }

    public ColorAction getNodeColorAction() {
        return nodeColorAction;
    }

    public void itemEntered(VisualItem item, MouseEvent e) {

        if (sourceNodeTupleSet == null) {
            /*
             * this delayed initialization is done to avoid that the
             * visualization has to be given to the contructor of this
             * control;
             */

            Visualization vis = item.getVisualization();
            try {
                vis.addFocusGroup(sourceNodeGroupName);
                vis.addFocusGroup(targetNodeGroupName);
                vis.addFocusGroup(selectedEdge);
            } catch (Exception ex) {
                System.out.println("Problems over problems while adding foucs groups to visualization "
                        + ex.getMessage());
            }

            sourceNodeTupleSet = vis.getFocusGroup(sourceNodeGroupName);
            targetNodeTupleSet = vis.getFocusGroup(targetNodeGroupName);
            selectedEdgeTupleSet = vis.getFocusGroup(selectedEdge);

            edgeColorAction.setFocusGroups(new TupleSet[]{sourceNodeTupleSet,
                        targetNodeTupleSet, selectedEdgeTupleSet});
            nodeColorAction.setFocusGroups(new TupleSet[]{sourceNodeTupleSet,
                        targetNodeTupleSet, selectedEdgeTupleSet});
        }

        if (item instanceof EdgeItem) {
            setNeighbourHighlight((EdgeItem) item, true);
        }
    }

    public void itemExited(VisualItem item, MouseEvent e) {
        if (item instanceof EdgeItem) {
            setNeighbourHighlight((EdgeItem) item, false);
        }

        if (m_activity != null) {
            item.getVisualization().run(m_activity);
        }
    }

    protected void setNeighbourHighlight(EdgeItem selEdge,
            boolean state) {

        NodeItem src = selEdge.getSourceItem();
        NodeItem target = selEdge.getTargetItem();

        selEdge.setHover(state);
        src.setHover(state);
        target.setHover(state);

        if (state) {
            selectedEdgeTupleSet.addTuple(selEdge);
            sourceNodeTupleSet.addTuple(src);
            targetNodeTupleSet.addTuple(target);
        } else {
            selectedEdgeTupleSet.clear();
            sourceNodeTupleSet.clear();
            targetNodeTupleSet.clear();
        }

        if (m_activity != null) {
            selEdge.getVisualization().run(m_activity);
        }
    }

    private class HoverColorAction extends ColorAction {

        private int[] colorPalette;
        private TupleSet[] focusGroups;

        public HoverColorAction(int[] colorPalette, String target) {
            super(target, VisualItem.FILLCOLOR);

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

            if (item.isHover()) {
                for (int i = 0; i < 3; i++) {
                    if (focusGroups[i].containsTuple(item) && colorPalette.length>i) {
                        return colorPalette[i];
                    }
                }
                if(item instanceof Node){
                    return ColorLib.rgb(150, 150, 255);
                }
            }
            return 0;
        }
    }
}
