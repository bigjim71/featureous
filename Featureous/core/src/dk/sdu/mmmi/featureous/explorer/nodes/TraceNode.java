/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.explorer.nodes;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.ui.FeatureUI;
import java.awt.Image;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author ao
 */
public class TraceNode extends AbstractNode{
    
    private final TraceModel model;

    public TraceNode(TraceModel m) {
        super(Children.LEAF, Lookups.singleton(m));
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
        return ImageUtilities.loadImage(new FeatureUI().getIconPath());
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public SystemAction[] getActions() {
        return new SystemAction[]{};
    }

    @Override
    public String getShortDescription() {
        return model.getSrcFilePathOrNull();
    }
}
