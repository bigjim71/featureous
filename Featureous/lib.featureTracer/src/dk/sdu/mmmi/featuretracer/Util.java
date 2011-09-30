/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featuretracer;

import java.io.InputStream;

/**
 *
 * @author ao
 */
public class Util {

    public static InputStream getFTStream(){
        InputStream sourceFileStreamFt = Util.class.getResourceAsStream("/dk/sdu/mmmi/featuretracer/ft.jar");
        if(sourceFileStreamFt==null){
            throw new RuntimeException("Null stream!");
        }
        return sourceFileStreamFt;
    }
}
