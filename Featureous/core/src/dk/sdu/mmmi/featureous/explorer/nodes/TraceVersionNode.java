/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.explorer.nodes;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.ui.FeatureUI;
import java.awt.Image;
import java.util.Map;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author ao
 */
public class TraceVersionNode extends AbstractNode{
    private final Map.Entry<String, TraceModel> model;
    private final boolean active;

    public TraceVersionNode(Map.Entry<String, TraceModel> m, boolean active) {
        super(Children.LEAF, Lookups.singleton(m));
        this.active = active;
        this.model = m;
    }

    public String getName(){
        return model.getKey();
    }

    @Override
    public String getHtmlDisplayName() {
        Map.Entry<String, TraceModel> obj = getLookup().lookup (Map.Entry.class);
        return "<font color='!textText'>" +
                ((active)?"<b>":"") +
                obj.getKey()+
                ((active)?"</b>":"") +
                "</font>";
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
        return model.getValue().getSrcFilePathOrNull();
    }


}
