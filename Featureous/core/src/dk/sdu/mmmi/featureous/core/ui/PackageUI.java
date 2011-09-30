/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.ui;

/**
 *
 * @author ao
 */
public class PackageUI implements UIResource{
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getIconPath() {
        return "dk/sdu/mmmi/featureous/icons/nb/package.png";
    }

    public String getIconShadowlessPath() {
        return "dk/sdu/mmmi/featureous/icons/nb/package.png";
    }

}
