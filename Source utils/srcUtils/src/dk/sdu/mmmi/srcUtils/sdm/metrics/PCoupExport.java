/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.sdm.metrics;

import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;

/**
 *
 * @author ao
 */

public class PCoupExport implements Measure{

    public Double calculate(StaticDependencyModel dm, JPackage p) {
        double res = 0;
        for(JPackage pp : dm.getPackages()){
            if(pp.getQualName().equals(p.getQualName())){
                continue;
            }
            for(JType t : pp.getAllTypes()){
                for(JType tp : p.getAllTypes()){
                    res += t.getDepsTowardsCount(tp);
                }
            }
        }
        return res;
    }

}
