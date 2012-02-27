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

    public static InputStream getFTJarStream(){
        InputStream sourceFileStreamFt = Util.class.getResourceAsStream("/dk/sdu/mmmi/featuretracer/FeatureTracer.jar");
        if(sourceFileStreamFt==null){
            throw new RuntimeException("Null stream!");
        }
        return sourceFileStreamFt;
    }
    public static InputStream getFTClassStream(){
        InputStream sourceFileStreamFt = Util.class.getResourceAsStream("/dk/sdu/mmmi/featuretracer/FeatureTracer.class");
        if(sourceFileStreamFt==null){
            throw new RuntimeException("Null stream!");
        }
        return sourceFileStreamFt;
    }
    public static InputStream getBtraceAgentStream(){
        InputStream sourceFileStreamFt = Util.class.getResourceAsStream("/dk/sdu/mmmi/featuretracer/btrace-agent.jar");
        if(sourceFileStreamFt==null){
            throw new RuntimeException("Null stream!");
        }
        return sourceFileStreamFt;
    }
    
    public static InputStream getBtraceAgentBootStream(){
        InputStream sourceFileStreamFt = Util.class.getResourceAsStream("/dk/sdu/mmmi/featuretracer/btrace-boot.jar");
        if(sourceFileStreamFt==null){
            throw new RuntimeException("Null stream!");
        }
        return sourceFileStreamFt;
    }

    public static InputStream getXMLDescriptor(){
        InputStream sourceFileStreamFt = Util.class.getResourceAsStream("/dk/sdu/mmmi/featuretracer/dk.sdu.mmmi.featuretracer.xml");
        if(sourceFileStreamFt==null){
            throw new RuntimeException("Null stream!");
        }
        return sourceFileStreamFt;
    }
}
