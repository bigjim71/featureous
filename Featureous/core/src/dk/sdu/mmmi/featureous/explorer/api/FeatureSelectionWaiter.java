/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.explorer.api;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.SelectionManager;
import dk.sdu.mmmi.featureous.core.model.SelectionChangeListener;
import java.awt.BorderLayout;
import javax.swing.JLabel;

/**
 */
public class FeatureSelectionWaiter implements SelectionChangeListener{

    private AbstractTraceView view;
    private Runnable action;
    private boolean selected;

    public void init(AbstractTraceView gcv, Runnable action){
        selected = false;
        view = gcv;
        this.action = action;
        if(Controller.getInstance().getTraceSet().getSelectionManager().getSelectedFeats().size()>0){
            selected = true;
            action.run();
        }else{
            gcv.add(new JLabel("Please select target feature in the feature explorer."), BorderLayout.SOUTH);
        }
        Controller.getInstance().getTraceSet().getSelectionManager().addSelectionListener(this);
    }

    public void dispose(){
        Controller.getInstance().getTraceSet().getSelectionManager().removeSelectionListener(this);
        view = null;
    }

    @Override
    public void featureSelectionChanged(SelectionManager tl) {
        if(!selected && tl.getSelectedFeats().size()>0){
            selected = true;
            view.removeAll();
            view.revalidate();
            action.run();
        }
    }

    public void compUnitSelectionChanged(SelectionManager tl) {
    }

}
