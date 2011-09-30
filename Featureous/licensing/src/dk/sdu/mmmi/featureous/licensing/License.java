/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.licensing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author jemr
 */
public final class License {

    public static License getInstance() {
        return new License();
    }
    private LicenseFile licenseFile;

    private License() {
        try {
            licenseFile = LicenseFile.createInstance(new FileInputStream(new File(FileUtil.toFile(FileUtil.getConfigRoot()).getPath()+"/featureous.lic")), MachineID.getMachineIds());
        } catch (FileNotFoundException ex) {
        }
    }

    public boolean isValid() {
        return licenseFile != null ? licenseFile.isValid() : false;
    }

    public long getExpirationDate() {
        return licenseFile != null ? licenseFile.getExpirationDate() : 0;
    }

    public static String getMachineIdString() {
        return MachineID.getMachineIdString();
    }

    public boolean isExpired() {
        return licenseFile != null && licenseFile.getExpirationDate() < System.currentTimeMillis();
    }
}
