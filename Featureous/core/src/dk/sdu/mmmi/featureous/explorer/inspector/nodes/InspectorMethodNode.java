/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.explorer.inspector.nodes;

import dk.sdu.mmmi.featureous.core.ui.MethodUI;
import dk.sdu.mmmi.featureous.icons.IconUtils;
import dk.sdu.mmmi.srcUtils.nb.NavigationUtils;
import java.awt.Image;
import java.util.Enumeration;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
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
public class InspectorMethodNode extends AbstractNode {

    private final String mName;
    private final String encClass;
    private final String pack;
    private static OpenAction openAction;
    private final boolean isFep;

    public InspectorMethodNode(final MethodUI mName, final String enclosingClass, final String pack, boolean isFep) {
        super(Children.LEAF, Lookups.singleton(mName));
        this.isFep = isFep;
        this.mName = mName.getName();
        this.encClass = enclosingClass;
        this.pack = pack;
        setName(mName.getName());
        if (openAction == null) {
            openAction = new OpenAction();
        }
        setShortDescription(this.mName);
    }

    public class OpenAction extends NodeAction {

        private OpenAction() {
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
                    InspectorMethodNode node = ((InspectorMethodNode) nodes[0]);
                    NavigationUtils.openMethod(node.pack, node.encClass, node.mName);
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
        int dotIdx = mName.split("\\(")[0].lastIndexOf(".");
        String dispName = mName.substring(dotIdx + 1);
        String m = dispName.substring(0, dispName.indexOf("("));
        String mParams = dispName.substring(dispName.indexOf("("));
        return "<font color='!textText'><b>" + m + "</b>" + mParams + "</font>";
    }

    @Override
    public String getShortDescription() {
        return mName;
    }

    @Override
    public SystemAction getDefaultAction() {
        return openAction;
    }

    @Override
    public Image getIcon(int type) {
        if (isFep) {
            return ImageUtilities.icon2Image(IconUtils.loadOverlayedIcon(new MethodUI().getIconPath(),
                    "dk/sdu/mmmi/featureous/icons/nb/running.png"));
        } else {
            return ImageUtilities.loadImage(new MethodUI().getIconPath());
        }
    }

    @Override
    public SystemAction[] getActions() {
        return new SystemAction[]{};
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    private String findFilePath(Project p, String fileExtName) {
        Enumeration<? extends FileObject> e = p.getProjectDirectory().getChildren(true);
        while (e.hasMoreElements()) {
            FileObject f = e.nextElement();
            if (f.getNameExt().equals(fileExtName)) {
                return f.getPath();
            }
        }
        return null;
    }
}
