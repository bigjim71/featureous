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
public class PCoupImportNormFactorAllTypes implements Measure{

    public Double calculate(StaticDependencyModel dm, JPackage p) {
        double m=0;
        int currPkgTypeCount = p.getAllTypes().size();
        int allTypesCount = dm.getAllTypesCount();
        for (JType t1 : p.getAllTypes()) {
            m += (allTypesCount - currPkgTypeCount) * (t1.getFieldCount() +
                (t1.getMethodCount() - t1.getEstAccessorCount()) +
                t1.getConstructorCount() + 1);
        }
        if(m==0){
            return null;
        }
        return m;
    }

}
