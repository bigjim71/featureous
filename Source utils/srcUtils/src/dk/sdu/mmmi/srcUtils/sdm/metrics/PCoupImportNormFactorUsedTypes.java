/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.sdm.metrics;

import dk.sdu.mmmi.srcUtils.sdm.model.JDependency;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ao
 */

@Deprecated
public class PCoupImportNormFactorUsedTypes implements Measure{

    public Double calculate(StaticDependencyModel dm, JPackage p) {
        double m=0;
        Set<JType> outsideUsedTypes = new HashSet<JType>();
        for(JType t : p.getAllTypes()){
            for(JDependency d : t.getDependencies()){
                if(!p.getAllTypes().contains(d.getReferencedType())){
                    outsideUsedTypes.add(t);
                }
            }
        }
        for (JType t1 : p.getAllTypes()) {
            m += outsideUsedTypes.size() * (t1.getFieldCount() +
                (t1.getMethodCount() - t1.getEstAccessorCount()) +
                t1.getConstructorCount() + 1);
        }
        if(m==0){
            return null;
        }
        return m;
    }

}
