/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.explorer.inspector.nodes;

import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.ui.ClassUI;
import dk.sdu.mmmi.featureous.icons.IconUtils;
import dk.sdu.mmmi.srcUtils.nb.NavigationUtils;
import java.awt.Image;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * @author ao
 */
class InspectorClassNodeView extends AbstractNode {

    private final ClassModel key;
    private static OpenAction openAction;

    public InspectorClassNodeView(final ClassModel key) {
        super(new InspectorClassNodeModel(key), Lookups.singleton(key));

        setName(key.getName());
        this.key = key;
        if (openAction == null) {
            openAction = new OpenAction();
        }
    }

    public ClassModel getKey() {
        return key;
    }

    public class OpenAction extends NodeAction {

        public OpenAction() {
            super();
        }

        @Override
        public String getName() {
            return "Goto source";
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        protected void performAction(Node[] nodes) {
            try {
                if (nodes.length > 0) {
                    ClassModel key =  ((InspectorClassNodeView) nodes[0]).getKey();
                    NavigationUtils.openClass(key.getPackageName(), key.getName());
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        protected boolean enable(Node[] nodes) {
            return true;
        }
    };

    @Override
    public String getHtmlDisplayName() {
        return "<font color='!textText'>" + getName().replace(key.getPackageName() + ".", "") + "</font>";
    }

    @Override
    public Image getIcon(int type) {

        for (String m : key.getAllMethods()) {
            if (key.isFep(m)) {
                return ImageUtilities.icon2Image(
                        IconUtils.loadOverlayedIcon(new ClassUI().getIconPath(),
                        "dk/sdu/mmmi/featureous/icons/nb/running.png"));
            }
        }

        return ImageUtilities.loadImage(new ClassUI().getIconPath());
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public SystemAction[] getActions() {
        return new SystemAction[]{openAction};
    }
}
