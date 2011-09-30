/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.tracedrunner;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

public final class TraceSelectedProject extends CookieAction {

    protected void performAction(Node[] activatedNodes) {
        Project project = activatedNodes[0].getLookup().lookup(Project.class);
//        Project project = findProjectForDataObject(dataObject);

        if (project == null) {
            throw new RuntimeException("Error looking up selected.");
        }

        try {
            Runner r = new Runner();
            r.execute(project, ActionProvider.COMMAND_RUN);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
        }
    }

    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    public String getName() {
        return "Trace Project";
    }

    protected Class[] cookieClasses() {
        return new Class[]{Project.class};
    }

    @Override
    protected String iconResource() {
        return "dk/sdu/mmmi/featureous/tracedrunner/runProject.png";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
