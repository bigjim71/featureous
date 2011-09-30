/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package org.mediavirus.parvis.gui;

/**
 *
 * @author  flo
 * @version 1.0
 */
public interface ProgressListener extends java.util.EventListener {

    public void processProgressEvent(ProgressEvent e);
    
}

