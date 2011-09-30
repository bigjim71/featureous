/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package org.mediavirus.parvis.api;


import java.awt.BorderLayout;
import org.mediavirus.parvis.gui.ParallelDisplay;

/**
 *
 * @author  flo
 */
public class ParallelPanel extends javax.swing.JPanel {

    public ParallelPanel() {
        initComponents();
        defaultSetup();
    }

    private void defaultSetup(){
        parallelDisplay.setFloatPreference("brushRadius", 0.2f);

        parallelDisplay.setBoolPreference("hoverText", true);
        parallelDisplay.setBoolPreference("hoverLine", true);
    }

    public ParallelDisplay getParallelDisplay() {
        return parallelDisplay;
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        
        parallelDisplay = new org.mediavirus.parvis.gui.ParallelDisplay();
        this.add(parallelDisplay, java.awt.BorderLayout.CENTER);
        this.revalidate();
        parallelDisplay.invalidate();
        parallelDisplay.updateUI();
    }

    private org.mediavirus.parvis.gui.ParallelDisplay parallelDisplay;
}
