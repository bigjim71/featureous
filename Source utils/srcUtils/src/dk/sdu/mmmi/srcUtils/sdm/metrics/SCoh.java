/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.sdm.metrics;

import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;


/**
 * Mean PCoh
 * @author ao
 */
public class SCoh implements Measure{

    public Double calculate(StaticDependencyModel dm, JPackage pack) {
        double res = 0;
        double div = 0;
        for(JPackage p : dm.getPackages()){
            Double parRes = new PCoh().calculate(dm, p);
            if(parRes!=null){
                res += parRes;
                div++;
            }
        }
        return res/div;
    }

}
