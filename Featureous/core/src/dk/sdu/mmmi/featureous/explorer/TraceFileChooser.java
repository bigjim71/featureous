/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.explorer;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("serial")
public class TraceFileChooser extends JFileChooser {

    public TraceFileChooser() {
        try {
            File defaultDirectory = new File(new File("./traces").getCanonicalPath());
            setCurrentDirectory(defaultDirectory);
        } catch (IOException e) {
            //do nothing
        }
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Trace files", "ser", "xml", "ftf");
        setFileFilter(filter);
        setMultiSelectionEnabled(true);
        setDragEnabled(true);
        setApproveButtonText("Add traces");
        setApproveButtonToolTipText("Add selected traces to the list");
    }
}
