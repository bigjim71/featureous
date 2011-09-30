/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.licensing;

import dk.sdu.mmmi.featureous.explorer.spi.LicenseProvider;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author jemr
 */
@ServiceProvider(service = LicenseProvider.class)
public class FeatureousLicense implements LicenseProvider {

    private static String machineIdStr = "";
    private boolean valid = false;
    private long expire = 0;
    private FileObject extRoot;

    void saveLicense(InputStream is) {
        try {
            FileObject outFile = extRoot.getFileObject("featureous.lic");
            if (outFile == null || outFile.isVirtual()) {
                outFile = extRoot.createData("featureous.lic");
            }

            OutputStream os = outFile.getOutputStream();
            FileUtil.copy(is, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    void downloadTrialLicense() {
        try {
            URL url = new URL("http://featureous.org/reg/trial.php?mid=" + getMachineIdString());
            InputStream is = url.openStream();
            saveLicense(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public void downloadLicense(String confirmString) {
        try {
            URL url = new URL("http://featureous.org/reg/license.php?code=" + confirmString);
            InputStream is = url.openStream();
            saveLicense(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public FeatureousLicense() {
        refreshLicense();
    }

    public void refreshLicense() {
            extRoot = FileUtil.getConfigRoot();
        try {
            License lic = License.getInstance();
            machineIdStr = License.getMachineIdString();
            valid = lic.isValid();
            expire = lic.getExpirationDate();
        } catch (Exception e) {
            valid = true;
            expire = Long.MAX_VALUE;
        }
    }

    public long getExpire() {
        return expire;
    }

    public String getMachineIdString() {
        return machineIdStr;
    }

    public boolean isValid() {
        return valid;
    }
}
