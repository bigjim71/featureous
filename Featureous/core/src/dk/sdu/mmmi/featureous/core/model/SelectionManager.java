/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.model;

import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;

/**
 *
 * @author ao
 */
public class SelectionManager {

    private Set<SelectionChangeListener> selectionListeners = new HashSet<SelectionChangeListener>();
    private Set<String> selectedFeats = new HashSet<String>();
    private Set<String> selectedPkgs = new HashSet<String>();
    private Set<String> selectedClasses = new HashSet<String>();
    private Set<String> selectedExecs = new HashSet<String>();

    public void addSelectionListener(SelectionChangeListener l) {
        selectionListeners.add(l);
    }

    public void removeSelectionListener(SelectionChangeListener l) {
        selectionListeners.remove(l);
    }

    public void notifyFeatureSelectionChanged() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Set<SelectionChangeListener> ls = new HashSet<SelectionChangeListener>();
                ls.addAll(selectionListeners);

                for (SelectionChangeListener l : ls) {
                    l.featureSelectionChanged(SelectionManager.this);
                }
            }
        });
    }

    public void notifyUnitSelectionChanged() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Set<SelectionChangeListener> ls = new HashSet<SelectionChangeListener>();
                ls.addAll(selectionListeners);
                for (SelectionChangeListener l : ls) {
                    l.compUnitSelectionChanged(SelectionManager.this);
                }
            }
        });
    }

    public void clearAllSelections(boolean notifyListeners) {
        selectedFeats.clear();
        selectedPkgs.clear();
        selectedClasses.clear();
        selectedExecs.clear();
        if (notifyListeners) {
            notifyFeatureSelectionChanged();
            notifyUnitSelectionChanged();
        }
    }

    public void addFeatureSelection(Set<String> fs) {
        selectedFeats.addAll(fs);
        notifyFeatureSelectionChanged();
    }

    public void addPkgSelection(Set<String> pkgs) {
        selectedPkgs.addAll(pkgs);
        notifyUnitSelectionChanged();
    }

    public void addClassSelection(Set<String> classes) {
        selectedClasses.addAll(classes);
        notifyUnitSelectionChanged();
    }

    public void addExecSelection(Set<String> execs) {
        selectedExecs.addAll(execs);
        notifyUnitSelectionChanged();
    }

    public Set<String> getSelectedClasses() {
        return selectedClasses;
    }

    public Set<String> getSelectedExecs() {
        return selectedExecs;
    }

    public Set<String> getSelectedFeats() {
        return selectedFeats;
    }

    public Set<String> getSelectedPkgs() {
        return selectedPkgs;
    }
}
