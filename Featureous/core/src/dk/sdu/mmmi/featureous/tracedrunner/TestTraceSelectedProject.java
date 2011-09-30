/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.tracedrunner;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Exceptions;

public final class TestTraceSelectedProject implements ActionListener {

    private final Project context;

    public TestTraceSelectedProject(Project context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        try {
            Runner r = new Runner();
            r.execute(context, ActionProvider.COMMAND_TEST);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
