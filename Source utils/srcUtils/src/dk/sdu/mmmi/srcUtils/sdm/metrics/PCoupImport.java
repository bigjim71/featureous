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

/**
 *
 * @author ao
 */
public class PCoupImport implements Measure{

    public Double calculate(StaticDependencyModel dm, JPackage p) {
        double res = 0;
//        for(spoonTestBench.simpleDependencyModel.model.JPackage pp : dm.getPackages()){
//            if(pp.equals(p)){
//                continue;
//            }
//            for(JType t : pp.getTypes()){
//                for(JType tp : p.getTypes()){
//                    res += t.getDependenciesTowards(tp, false).size();
//                }
//            }
//        }
        
        for(JType t : p.getAllTypes()){
            for(JDependency d : t.getDependencies()){
                if(!p.getAllTypes().contains(d.getReferencedType())){
                    res++;
                }
            }
        }
        return res;
    }

}
