/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.metrics.concernmetrics;

import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import dk.sdu.mmmi.srcUtils.sdm.model.Util;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author ao
 */
public class ScaTangUtil {

    public static List<JPackage> getInsulatedPackages(Set<TraceModel> ftms, StaticDependencyModel dm){
        Set<JPackage> usedPacks = new HashSet<JPackage>(getNonInsulatedPackages(ftms, dm));

        List<JPackage> res = new ArrayList<JPackage>();
        res.addAll(dm.getPackages());
        res.removeAll(usedPacks);
        return res;
    }

    public static List<JPackage> getNonInsulatedPackages(Set<TraceModel> ftms, StaticDependencyModel dm){
        Set<JPackage> usedPacks = new HashSet<JPackage>();
        for(TraceModel ftm : ftms){
            for(ClassModel t : ftm.getClassSet()){
                JPackage p = Util.getTypesPackage(t.getName(), dm);
                if(p!=null){
                    usedPacks.add(p);
                }
            }
        }

        return new ArrayList<JPackage>(usedPacks);
    }

    public static Set<String> getNonInsulatedPackages(Set<TraceModel> ftms){
        Set<String> usedPacks = new HashSet<String>();
        for(TraceModel ftm : ftms){
            for(ClassModel t : ftm.getClassSet()){
                usedPacks.add(t.getPackageName());
            }
        }
        return usedPacks;
    }
    
    public static Set<String> getNonInsulatedClasses(Set<TraceModel> ftms){
        Set<String> usedTypes = new HashSet<String>();
        for(TraceModel ftm : ftms){
            for(ClassModel t : ftm.getClassSet()){
                usedTypes.add(t.getName());
            }
        }

        return usedTypes;
    }

    public static List<JType> getNonInsulatedTypes(Set<TraceModel> ftms, StaticDependencyModel dm){
        Set<JType> usedTypes = new HashSet<JType>();
        for(TraceModel ftm : ftms){
            for(ClassModel t : ftm.getClassSet()){
                JType tt = dm.getTypeByNameOrNull(t.getName());
                if(tt!=null){
                    usedTypes.add(tt);
                }
            }
        }

        return new ArrayList<JType>(usedTypes);
    }

    public static List<TraceModel> getInsulatedFeatures(Set<TraceModel> ftms){
        List<TraceModel> res = new ArrayList<TraceModel>();
        for(TraceModel ftm : ftms){
            if(ftm.getClassSet().size()==0){
                res.add(ftm);
            }
        }
        return res;
    }
}
