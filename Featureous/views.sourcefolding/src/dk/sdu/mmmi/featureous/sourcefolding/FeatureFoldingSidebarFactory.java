/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.sourcefolding;

import java.io.IOException;
import org.netbeans.editor.SideBarFactory;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import org.openide.util.Exceptions;

public class FeatureFoldingSidebarFactory implements SideBarFactory  {

    @Override
    public JComponent createSideBar(JTextComponent target) {
        try {
            return new FeatureFoldingSidebar(target);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return new JPanel();
    }
}
