/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.explorer.nodes;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import java.awt.Image;
import org.openide.nodes.AbstractNode;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author ao
 */
class TraceGroupNode extends AbstractNode{
    private final TraceModel model;

    public TraceGroupNode(TraceModel m) {
        super(new TraceGroupNodeModel(m.getChildren()), Lookups.singleton(m));
        this.model = m;
    }

    public String getName(){
        return model.getName();
    }

       @Override
    public String getHtmlDisplayName() {
        TraceModel obj = getLookup().lookup (TraceModel.class);
        return "<font color='!textText'>" + obj.getName()+ "</font>";
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("fugue/icons/documentsstack.png");
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public SystemAction[] getActions() {
        return new SystemAction[]{};
    }
}
