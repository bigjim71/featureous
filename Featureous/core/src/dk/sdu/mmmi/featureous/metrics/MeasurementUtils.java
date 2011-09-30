/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.metrics;

import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author ao
 */
public class MeasurementUtils {
    private static List<JType> getTypesInFeature(String feature, Map<JType, String> mapping){
        List<JType> res = new ArrayList<JType>();
        for(Map.Entry<JType, String> e : mapping.entrySet()){
            if(e.getValue().equals(feature)){
                res.add(e.getKey());
            }
        }
        return res;
    }
}
