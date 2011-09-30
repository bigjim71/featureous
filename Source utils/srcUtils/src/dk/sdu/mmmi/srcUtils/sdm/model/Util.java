/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.sdm.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ao
 */
 public class Util {

    public static JPackage getTypesPackage(JType t, StaticDependencyModel dm){
        return getTypesPackage(t.getQualName(), dm);
    }

    public static JPackage getTypesPackage(String typeQualName, StaticDependencyModel dm){
        for(JPackage p : dm.getPackages()){
            for(JType tt : p.getAllTypes()){
                if(tt.getQualName().equals(typeQualName)){
                    return p;
                }
            }
        }

        return null;
    }

    public static List<JType> deepInsertType(JPackage pack, JType type) {
        return deepInsertType(pack, type, new HashList<JType>());
    }

    public static List<JType> deepInsertType( JPackage pack, JType type, HashList<JType> added) {
        pack.getOrAddType(type);
        added.add(type);

        // handled inner types
        for(JType t : type.getEnclosedTypes()){
            deepInsertType(pack, t, added);
        }
        return added;
    }

    public static int findDiffCount(StaticDependencyModel m1, StaticDependencyModel m2){
        int diffs = 0;
        for(JPackage p1 : m1.getPackages()){
            for(JType t1 : p1.getAllTypes()){
                boolean found = false;
                for(JPackage p2 : m2.getPackages()){
                    if(p1.getQualName().equals(p2.getQualName())){
                        for(JType t2 : p2.getAllTypes()){
                            if(t1.getQualName().equals(t2.getQualName())){
                                found = true;
                            }
                        }
                    }
                }
                if(!found){
                    diffs++;
                }
            }
        }
        return diffs;
    }

    public static Double meanDiffCount(List<StaticDependencyModel> models){
        if(models.size()<2){
            return null;
        }
        double meanDiffSum = 0d;
        for(StaticDependencyModel m1 : models){
            double partialMean = 0d;
            for(StaticDependencyModel m2 : models){
                if(m1!=m2){
                    partialMean += findDiffCount(m1, m2);
                }
            }
            partialMean = partialMean/(models.size()-1);
            meanDiffSum += partialMean;
        }
        return meanDiffSum/models.size();
    }
    
    public static int getDepCountFromPackageToType(JPackage p, JType t) {
        int res = 0;
        for (JType tt : p.getAllTypes()) {
            if (!tt.getQualName().equals(t.getQualName())) {
                res += tt.getDependenciesTowards(t, false).size();
            }
        }
        return res;
    }
    
    public static Set<JType> getReferencedTopLevelTypes(JType m, HashSet<JType> collected) {
        if (collected.contains(m)) {
            return collected;
        }

        if (m.isTopLevel()) {
            collected.add(m);
        }

        Set<JType> refed = new HashSet<JType>();
        for (JDependency dep : m.getDependencies()) {
            refed.add(dep.getReferencedType());
        }
        for (JType tt : m.getEnclosedTypes()) {
            for (JDependency dep : tt.getDependencies()) {
                refed.add(dep.getReferencedType());
            }
        }
        refed.removeAll(collected);

        for (JType tt : refed) {
            getReferencedTopLevelTypes(tt, collected);
        }

        return collected;
    }
    
    public static StaticDependencyModel createRefactoredModel(Map<String, String> typeToPkg, StaticDependencyModel dm) throws RuntimeException {
        StaticDependencyModel finalDm = new StaticDependencyModel();
        for (Map.Entry<String, String> me : typeToPkg.entrySet()) {
            JPackage pkg = finalDm.getOrAddPackageByName(getPkgStr(me.getValue()));
            JType t = dm.getTypeByNameOrNull(me.getKey());
            if (t == null) {
                throw new RuntimeException("Inconsistency when adding a type to new dependency model.");
            }
            Util.deepInsertType(pkg, t);
        }
        return finalDm;
    }
    
    public static String getPkgStr(String pkg){
        return pkg.toLowerCase().replace(" ", "_");
    }
}
