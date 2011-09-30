/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.util;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class FileWrite {

    public static String writeTextToFile(String path, String content) {
        try {
            FileWriter fileWriter = new FileWriter(path);
            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write(content);
            out.close();
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
