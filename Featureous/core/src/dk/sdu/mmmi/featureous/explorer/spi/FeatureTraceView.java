/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.explorer.spi;

import java.awt.Image;
import org.openide.windows.TopComponent;

/**
 * This needs to be used as service in order for a view to be displayed
 * as an icon in the main toolbar of Featureous.
 * @author ao
 */
public interface FeatureTraceView {
    TopComponent getInstance();
    TopComponent createInstance();
    Image getBigIcon();
}
