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
 * @author ao
 */
public class PCoh implements Measure{

    public Double calculate(StaticDependencyModel dm, JPackage p) {
        return PCohp(p);
    }

    public Double PCohp(JPackage p) {
        double l=0, m=0;
        for (JType t1 : p.getAllTypes()) {
            for (JType t2 : p.getAllTypes()) {
                if(t1.equals(t2)){
                    continue;
                }
                l += t1.getDependenciesTowards(t2, false).size();
                m += t1.getFieldCount() + 
                        (t1.getMethodCount() - t1.getEstAccessorCount()) + 
                        t1.getConstructorCount() + 1;
            }
        }
//        System.out.println(l+"/"+m);

        if(m==0){
            return null;
        }
        return l/m;
    }

}
