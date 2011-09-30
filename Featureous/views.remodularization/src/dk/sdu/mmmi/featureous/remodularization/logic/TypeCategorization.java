/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.logic;

import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.srcUtils.sdm.model.JDependency;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ao
 */
public class TypeCategorization {

    static Map<JType, String> getNeighborhoodTypesForTraces(StaticDependencyModel dm, List<TraceModel> ftms) {
        Set<String> tracesTypesIDs = new HashSet<String>();
        for(TraceModel ftm : ftms){
            for(ClassModel t : ftm.getClassSet()){
                tracesTypesIDs.add(t.getName());
            }
        }
        Map<JType, String> neighbors = new HashMap<JType, String>();
        Set<JType> neighborsMulti = new HashSet<JType>();
        for(TraceModel ftm : ftms){
            for(ClassModel t : ftm.getClassSet()){
                JType dt = dm.getTypeByNameOrNull(t.getName());
                if(dt!=null){
                    for(JDependency dep : dt.getDependencies()){
                        JType refed = dep.getReferencedType();
                        if(!tracesTypesIDs.contains(refed.getQualName()) && refed.isTopLevel()){
                            if(neighborsMulti.contains(refed) || neighbors.containsKey(refed)){
                                neighbors.remove(refed);
                                neighborsMulti.add(refed);
                            }else{
                                neighbors.put(refed, ftm.getName());
                            }
                        }
                    }
                }
            }
        }

        for(JType nt : neighborsMulti){
            Map<String, Integer> featToRefs= new HashMap<String, Integer>();
            for(TraceModel ftm : ftms){
                featToRefs.put(ftm.getName(), 0);

                for(ClassModel t : ftm.getClassSet()){
                    JType dt = dm.getTypeByNameOrNull(t.getName());
                    if(dt!=null){
                        int deps = dt.getDependenciesTowards(nt, false).size();
                        featToRefs.put(ftm.getName(), featToRefs.get(ftm.getName()) + deps);
                    }
                }
            }

            String fMax = null;
            int maxCount = 0;
            for(Map.Entry<String, Integer> me : featToRefs.entrySet()){
                if(me.getValue()>maxCount){
                    maxCount = me.getValue();
                    fMax = me.getKey();
                }
            }

            if(fMax!=null){
                neighbors.put(nt, fMax);
            }
        }

        return neighbors;
    }

    public static List<JType> getNotCoveredTypes(StaticDependencyModel dm, Set<TraceModel> ftms){
        List<JType> notCoveredTypes = getTopLevelTypesByFeatureParticipationRation(dm, ftms,
                new ParticipationCondition() {
            public boolean accept(int featuresParticipation, int featuresCount) {
                return featuresParticipation == 0;
            }
        });
        return notCoveredTypes;
    }
    
    private static interface ParticipationCondition{
        boolean accept(int featuresParticipation, int featuresCount);
    }

    private static List<JType> getTopLevelTypesByFeatureParticipationRation(StaticDependencyModel dm,
            Set<TraceModel> ftms, ParticipationCondition pc){
        List<JType> infTypes = new ArrayList<JType>();
        int featCount = ftms.size();
        for(JPackage p : dm.getPackages()){
            for(JType t : p.getTopLevelTypes()){
                int typeParticipation = 0;
                for(TraceModel ftm : ftms){
                    for(ClassModel ft : ftm.getClassSet()){
                        if(ft.getName().equals(t.getQualName())){
                            typeParticipation++;
                        }
                    }

                }
                if(pc.accept(typeParticipation, featCount)){
                    infTypes.add(t);
                }
            }
        }

        return infTypes;
    }
}
