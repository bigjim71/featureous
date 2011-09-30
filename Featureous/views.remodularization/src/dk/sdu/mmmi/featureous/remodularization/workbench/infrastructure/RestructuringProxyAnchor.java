/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.workbench.infrastructure;

import dk.sdu.mmmi.featureous.remodularization.workbench.AbstractContainerWidget;
import java.awt.Point;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.model.StateModel;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author ao
 */
public class RestructuringProxyAnchor extends Anchor implements StateModel.Listener {

    private StateModel model;
    private Anchor[] anchors;
    private int index;

    public RestructuringProxyAnchor(StateModel model, Anchor... anchors) {
        super (null);
//        assert model != null  &&  model.getMaxStates () == anchors.length;
        this.model = model;
        this.anchors = anchors;
        this.index = model.getState ();
    }

    public StateModel getModel () {
        return model;
    }

    public void replaceContainer(AbstractContainerWidget newContainer){
        for(int i = 0; i<anchors.length;i++){
            if(anchors[i].getRelatedWidget() instanceof AbstractContainerWidget){
                anchors[i] = AnchorFactory.createRectangularAnchor(newContainer);
                break;
            }
        }
    }
    
    protected void notifyEntryAdded (Entry entry) {
        anchors[index].addEntry (entry);
    }

    protected void notifyEntryRemoved (Entry entry) {
        anchors[index].removeEntry (entry);
    }

    public void setModel(StateModel model) {
        this.model.removeListener(this);
        this.model = model;
        this.model.addListener(this);
    }

    protected void notifyUsed () {
        model.addListener (this);
    }

    protected void notifyUnused () {
        model.removeListener (this);
    }

    public void stateChanged () {
        int state = getModel ().getState ();
        if (index == state)
            return;
        anchors[index].removeEntries (getEntries ());
        index = state;
        anchors[index].addEntries (getEntries ());
        revalidateDependency ();
    }

    public Point getRelatedSceneLocation () {
        return anchors[index].getRelatedSceneLocation ();
    }

    public Widget getRelatedWidget () {
        return anchors[index].getRelatedWidget();
    }

    public Anchor.Result compute (Anchor.Entry entry) {
        return anchors[index].compute (entry);
    }

}
