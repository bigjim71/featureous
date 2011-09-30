/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.explorer.spi;

/**
 *
 * @author andrzejolszak
 */
public interface LicenseProvider {

    public void downloadLicense(String confirmString);

    public void refreshLicense();

    public boolean isValid();

    public String getMachineIdString();
    
}
