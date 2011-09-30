/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.ui;

import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author ao
 */
public class OutputUtil {
    private static InputOutput io = IOProvider.getDefault().getIO ("Featureous output", true);
    
    public static void log(String message){
        if(io.isClosed()){
            io = IOProvider.getDefault().getIO("Featureous output", true);
        }
        
        String cName = Thread.currentThread().getStackTrace()[2].getClassName();
        io.getOut().println("[" + cName + ":]\n" + message);
    }
}
