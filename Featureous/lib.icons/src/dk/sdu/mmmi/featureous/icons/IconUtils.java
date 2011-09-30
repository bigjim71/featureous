/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.icons;

import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import org.openide.util.ImageUtilities;

/**
 *
 * @author ao
 */
public class IconUtils {
    public static Icon loadIcon(String name) {
        return ImageUtilities.loadImageIcon(name, false);
    }

    public static Icon loadOverlayedIcon(String background, String overlay) {
        Image bkg = ImageUtilities.loadImage(background, false);
        Image icon = new BufferedImage(bkg.getWidth(null), bkg.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Image overl = ImageUtilities.loadImage(overlay, false);

        icon.getGraphics().drawImage(bkg, 0, 0, null);
        icon.getGraphics().drawImage(overl, icon.getWidth(null)-overl.getWidth(null),
                icon.getHeight(null) - overl.getHeight(null), null);

        return ImageUtilities.image2Icon(icon);
    }
}
