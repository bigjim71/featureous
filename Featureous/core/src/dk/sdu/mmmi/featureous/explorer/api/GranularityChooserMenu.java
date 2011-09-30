/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.explorer.api;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JSlider;

/**
 *
 * @author ao
 */
public class GranularityChooserMenu extends JPopupMenu {

    public static final int METHOD_GRANULARITY = 2;
    public static final int CLASS_GRANULARITY = 1;
    public static final int PACKAGE_GRANULARITY = 0;
    private final GranularityChangeListener listener;
    private int currGranularity;

    public GranularityChooserMenu(int[] supportedGranularities, int initGran, GranularityChangeListener listener) {
        super();
        this.listener = listener;
        currGranularity = initGran;
        JMenuItem gran = new JMenuItem("Granularity:");
        gran.setEnabled(false);
        ButtonGroup group = new ButtonGroup();
        JMenuItem pkgMenuItem = new JRadioButtonMenuItem("Packages");
        pkgMenuItem.setEnabled(false);
        pkgMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                notifyNewGranularity(PACKAGE_GRANULARITY);
            }
        });
        JMenuItem classMenuItem = new JRadioButtonMenuItem("Classes");
        classMenuItem.setEnabled(false);
        classMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                notifyNewGranularity(CLASS_GRANULARITY);
            }
        });
        JMenuItem methodMenuItem = new JRadioButtonMenuItem("Methods");
        methodMenuItem.setEnabled(false);
        methodMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                notifyNewGranularity(METHOD_GRANULARITY);
            }
        });
        group.add(pkgMenuItem);
        group.add(classMenuItem);
        group.add(methodMenuItem);
        add(gran);
        add(pkgMenuItem);
        add(classMenuItem);
        add(methodMenuItem);
        add(new JSeparator(JSeparator.HORIZONTAL));

        Dictionary<Integer, JLabel> d = new Hashtable<Integer, JLabel>();
        for (int i : supportedGranularities) {
            if (i == 0) {
                pkgMenuItem.setEnabled(true);
            }
            if (i == 1) {
                classMenuItem.setEnabled(true);
            }
            if (i == 2) {
                methodMenuItem.setEnabled(true);
            }
        }

        if (initGran == 0) {
            pkgMenuItem.setSelected(true);
        }
        if (initGran == 1) {
            classMenuItem.setSelected(true);
        }
        if (initGran == 2) {
            methodMenuItem.setSelected(true);
        }
    }

    private void notifyNewGranularity(int newGran) {
        if (newGran != currGranularity) {
            listener.granularityChanged(newGran);
            currGranularity = newGran;
        }
    }

    public int getValue() {
        return currGranularity;
    }
    
    public PopupListener getPopupListener(){
        return new PopupListener();
    }

    class PopupListener extends MouseAdapter {

        boolean pt = false;
        
        public void mousePressed(MouseEvent e) {
            if(!e.isControlDown()){
                pt = e.isPopupTrigger();
            }
        }

        public void mouseReleased(MouseEvent e) {
            if(!e.isControlDown()){
                maybeShowPopup(e);
            }
        }

        private void maybeShowPopup(MouseEvent e) {
//            if (e.isPopupTrigger()) {
            if(pt || e.isPopupTrigger()){
                show(e.getComponent(),
                        e.getX(), e.getY());
            }
            pt = false;
//            }
        }
    }
}
