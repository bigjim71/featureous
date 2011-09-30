/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.licensing;

import java.io.InputStream;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import sun.security.rsa.RSAPublicKeyImpl;

/**
 *
 * @author jemr
 */
final class LicenseFile {

    static final PublicKey publicKey;

    static {
        PublicKey key = null;
        try {
            key = new RSAPublicKeyImpl(SignatureVerifyer.decodeByteArray(
                    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkh54SAZm/1bmsKa2GnlF"
                    + "NpcSNzZgP0hMWK1/wRSy7cYn5E0FogClwDOcf29oE6Ghp5iQMcq1MlVt8+qRwiUz"
                    + "TPFkKX2y/E251b6B3geMq7DihmVSXkuzCbCS+GeofWx6oF/KdCghZthzrrtBJczG"
                    + "W+oOzoRt6PrtOIkPf7Pu2mEjAeIlwK1uGMpVVk1Ul06VnlIlMPYSWjL7r/Zp5kf4"
                    + "dYsmqOuJpf58EIfp/L8Ih9XJk3H7rtSiQt9Obx38+16Q4ICdbVKVuB/C3FHA1DtK"
                    + "nAQjZAIzrmZj9zNppfFtsFLQT3sfuo1LXRNpZ6QDRRcPEHrIJsRcPECof5drszKE"
                    + "+wIDAQAB"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        publicKey = key;
    }

    static LicenseFile createInstance(InputStream licenseFile, Set<String> machineIds) {
        LicenseFile result = null;
        try {
            result = new LicenseFile(licenseFile, machineIds);
        } catch (Throwable e) {
        }
        return result;
    }
    private final boolean isValid;
    private final long expirationDate;
    private final Properties properties;
    private final Set<String> ids;

    private LicenseFile(Properties properties, Set<String> machineIds) {
        this.properties = properties;
        boolean valid = false;
        this.ids = new HashSet<String>();
        int total = Integer.parseInt(properties.getProperty("totalMachineIds"));
        for (int i = 0; i < total; i++) {
            String id = properties.getProperty("machineId." + i);
            this.ids.add(id);
            if (machineIds.contains(id)) {
                valid = true;
            }
        }
        expirationDate = Long.parseLong(properties.getProperty("expirationDate"));
        if (!valid) {
            throw new RuntimeException("Incorrect machine ID for License File");
        }
        isValid = valid;
    }

    private LicenseFile(InputStream licenseFile, Set<String> machineIds) {
        this(LicenseFile.validate(licenseFile, machineIds), machineIds);
    }

    private static Properties validate(InputStream file, Set<String> machineIds) {
        if (file == null || machineIds == null || machineIds.isEmpty()) {
            throw new RuntimeException();
        }

        try {
            Properties p = new Properties();
            String old = System.getProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
            p.loadFromXML(file);
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory", old);


            if (!SignatureVerifyer.verifySignature(p, publicKey)) {
                throw new RuntimeException("Verification failed");
            }

            return p;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    boolean isValid() {
        return isValid;
    }

    long getExpirationDate() {
        return expirationDate;
    }
}
