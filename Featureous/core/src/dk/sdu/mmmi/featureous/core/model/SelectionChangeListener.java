/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.model;

/**
 *
 * @author ao
 */
public interface SelectionChangeListener {
    void featureSelectionChanged(SelectionManager tl);
    void compUnitSelectionChanged(SelectionManager tl);
}
