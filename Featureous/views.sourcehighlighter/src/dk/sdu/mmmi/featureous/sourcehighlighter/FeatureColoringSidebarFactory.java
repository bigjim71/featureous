/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.sourcehighlighter;

import java.io.IOException;
import java.util.WeakHashMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.editor.SideBarFactory;

import javax.swing.text.JTextComponent;
import org.openide.util.Exceptions;

public class FeatureColoringSidebarFactory implements SideBarFactory  {

    private static final WeakHashMap<JTextComponent, FeatureColoringSidebar> mapping =
            new WeakHashMap<JTextComponent, FeatureColoringSidebar>();

    public static WeakHashMap<JTextComponent, FeatureColoringSidebar> getMapping() {
        return mapping;
    }

    @Override
    public JComponent createSideBar(JTextComponent target) {
        try {
            FeatureColoringSidebar sb = new FeatureColoringSidebar(target);
            mapping.put(target, sb);
            return sb;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return new JPanel();
    }
}
