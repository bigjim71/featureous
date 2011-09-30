/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package org.mediavirus.parvis.gui;

import org.mediavirus.parvis.model.*;

/**
 *
 * @author  flo
 */
public interface BrushListener {
    
    /**
     * Called when the brush is replaced by a new one.
     */
    void brushChanged(Brush b);
    
    /**
     * Called when the brush is modified.
     */
    void brushModified(Brush b);
    
}
