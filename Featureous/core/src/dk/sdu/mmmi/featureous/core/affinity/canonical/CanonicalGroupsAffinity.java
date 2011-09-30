/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.affinity.canonical;

import dk.sdu.mmmi.featureous.core.affinity.Affinity;
import dk.sdu.mmmi.featureous.core.affinity.AffinityProvider;
import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.OrderedBinaryRelation;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.model.TraceSet;
import dk.sdu.mmmi.featureous.core.similarity.JaccardClassSimilarity;
import dk.sdu.mmmi.featureous.core.similarity.SimilarityRelation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author ao
 */
public class CanonicalGroupsAffinity implements AffinityProvider {

    private final HashMap<String, Affinity> classes = new HashMap<String, Affinity>();
    private final HashMap<String, Affinity> pkgs = new HashMap<String, Affinity>();
    private final HashMap<String, Affinity> methods = new HashMap<String, Affinity>();
    private final HashMap<String, Affinity> fields = new HashMap<String, Affinity>();
    private TraceSet dirtySet = null;

    public void traceListChanged(TraceSet tl) {
        dirtySet = tl;
    }

    private void update(TraceSet tl) {
        if (tl == null) {
            return;
        }
        classes.clear();
        pkgs.clear();
        methods.clear();
        fields.clear();

        Map<TraceModel, Set<TraceModel>> canonicalGrouping = partitionWithCFS(tl);

        Set<String> allClasses = new HashSet<String>();
        Set<String> allPkgs = new HashSet<String>();
        Set<String> allMethods = new HashSet<String>();
        Set<String> allFields = new HashSet<String>();

        for (TraceModel tm : tl.getFirstLevelTraces()) {
            for (ClassModel cm : tm.getClassSet()) {
                allClasses.add(cm.getName());
                allPkgs.add(cm.getPackageName());
                for (String ms : cm.getAllMethods()) {
                    allMethods.add(ms);
                }
                for(String fa : cm.getAllFields()){
                    allFields.add(fa);
                }
            }
        }

        for (String cs : allClasses) {
            int fUsingCount = getClassUsageCount(cs, tl.getFirstLevelTraces());

            int fCanUsingCount = getClassUsageCount(cs, canonicalGrouping.keySet());

            int gUsingCount = 0;
            boolean groupCore = false;
            for (Set<TraceModel> gfs : canonicalGrouping.values()) {
                int gUsage = getClassUsageCount(cs, gfs);
                if(gUsage == gfs.size()){
                    groupCore = true;
                }
                if(gUsage>0){
                    gUsingCount++;
                }
            }

            // save affinity
            Affinity caf = null;
            if (fUsingCount == 1) {
                caf = Affinity.SINGLE_FEATURE;
            } else if (gUsingCount == canonicalGrouping.keySet().size()) {
                caf = Affinity.CORE;
            }else if(gUsingCount==1){
                caf = Affinity.SINGLE_GROUP;
            } else if (gUsingCount>1 && gUsingCount<canonicalGrouping.keySet().size()) {
                caf = Affinity.INTER_GROUP;
            }else{
                throw new RuntimeException("Bug in affinity calculation for " + cs);
            }

            classes.put(cs, caf);
        }

        // PKGs
        for (String p : allPkgs) {

            int fUsingCount = getPkgUsageCount(p, tl.getFirstLevelTraces());

            int fCanUsingCount = getPkgUsageCount(p, canonicalGrouping.keySet());

            int gUsingCount = 0;
            boolean groupCore = false;
            for (Set<TraceModel> gfs : canonicalGrouping.values()) {
                int gUsage = getPkgUsageCount(p, gfs);
                if(gUsage == gfs.size()){
                    groupCore = true;
                }
                if(gUsage>0){
                    gUsingCount++;
                }
            }

            // save affinity
            Affinity caf = null;
            if (fUsingCount == 1) {
                caf = Affinity.SINGLE_FEATURE;
            } else if (fCanUsingCount == canonicalGrouping.keySet().size()) {
                caf = Affinity.CORE;
            } else if (groupCore) {
                caf = Affinity.GROUP_CORE;
            } else if (gUsingCount>1) {
                caf = Affinity.INTER_GROUP;
            }else if(gUsingCount==1){
                caf = Affinity.SINGLE_GROUP;
            }else{
                throw new RuntimeException("Bug in affinity calculation for " + p);
            }

            pkgs.put(p, caf);
        }

        // Methods
        for (String m : allMethods) {
            int fUsingCount = getMethodUsageCount(m, tl.getFirstLevelTraces());

            int fCanUsingCount = getMethodUsageCount(m, canonicalGrouping.keySet());

            int gUsingCount = 0;
            boolean groupCore = false;
            for (Set<TraceModel> gfs : canonicalGrouping.values()) {
                int gUsage = getMethodUsageCount(m, gfs);
                if(gUsage == gfs.size()){
                    groupCore = true;
                }
                if(gUsage>0){
                    gUsingCount++;
                }
            }

            // save affinity
            Affinity caf = null;
            if (fUsingCount == 1) {
                caf = Affinity.SINGLE_FEATURE;
            } else if (fCanUsingCount == canonicalGrouping.keySet().size()) {
                caf = Affinity.CORE;
            } else if (groupCore) {
                caf = Affinity.GROUP_CORE;
            } else if (gUsingCount>1) {
                caf = Affinity.INTER_GROUP;
            }else if(gUsingCount==1){
                caf = Affinity.SINGLE_GROUP;
            }else{
                throw new RuntimeException("Bug in affinity calculation for " + m);
            }

            methods.put(m, caf);
        }

        // Fields
        for (String f : allFields) {
            int fUsingCount = getFieldUsageCount(f, tl.getFirstLevelTraces());

            int fCanUsingCount = getFieldUsageCount(f, canonicalGrouping.keySet());

            int gUsingCount = 0;
            boolean groupCore = false;
            for (Set<TraceModel> gfs : canonicalGrouping.values()) {
                int gUsage = getFieldUsageCount(f, gfs);
                if(gUsage == gfs.size()){
                    groupCore = true;
                }
                if(gUsage>0){
                    gUsingCount++;
                }
            }

            // save affinity
            Affinity caf = null;
            if (fUsingCount == 1) {
                caf = Affinity.SINGLE_FEATURE;
            } else if (fCanUsingCount == canonicalGrouping.keySet().size()) {
                caf = Affinity.CORE;
            } else if (groupCore) {
                caf = Affinity.GROUP_CORE;
            } else if (gUsingCount>1) {
                caf = Affinity.INTER_GROUP;
            }else if(gUsingCount==1){
                caf = Affinity.SINGLE_GROUP;
            }else{
                throw new RuntimeException("Bug in affinity calculation for " + f);
            }

            fields.put(f, caf);
        }

        dirtySet = null;
    }

    private int getClassUsageCount(String cm, Set<TraceModel> tms) {
        int usageCount = 0;
        for (TraceModel tm : tms) {
            if (tm.getClass(cm) != null) {
                usageCount++;
            }
        }
        return usageCount;
    }

    private int getPkgUsageCount(String pkg, Set<TraceModel> tms) {
        int usageCount = 0;
        for (TraceModel tm : tms) {
            for (ClassModel cm : tm.getClassSet()) {
                if(cm.getPackageName().equals(pkg)){
                    usageCount++;
                    break;
                }
            }
        }
        return usageCount;
    }

    private int getMethodUsageCount(String m, Set<TraceModel> tms) {
        int usageCount = 0;
        for (TraceModel tm : tms) {
            for (ClassModel cm : tm.getClassSet()) {
                if(cm.getAllMethods().contains(m)){
                    usageCount++;
                    break;
                }
            }
        }
        return usageCount;
    }
    
    private int getFieldUsageCount(String f, Set<TraceModel> tms) {
        int usageCount = 0;
        for (TraceModel tm : tms) {
            for (ClassModel cm : tm.getClassSet()) {
                if(cm.getAllFields().contains(f)){
                    usageCount++;
                    break;
                }
            }
        }
        return usageCount;
    }

    private Map<TraceModel, Set<TraceModel>> partitionWithCFS(TraceSet tl) {
//        SimilarityRelation sr = new JaccardCallsSimilarity();
        SimilarityRelation sr = new JaccardClassSimilarity();

        SimilarityMeasurementTool simTool = new SimilarityMeasurementTool(sr);

        Set<TraceModel> ftms = new HashSet<TraceModel>(tl.getFirstLevelTraces());

        Set<OrderedBinaryRelation<String, Double>> simMatrix = simTool.calculateSimilarityMatrix(ftms);

        CanonicalFeatureSetTool cfsTool = new CanonicalFeatureSetTool();
        Set<TraceModel> cfs = cfsTool.calculateCFS(ftms, simMatrix);

        FeaturePartitioningTool partitioningTool = new FeaturePartitioningTool();
        Map<TraceModel, Set<TraceModel>> partitioned = partitioningTool.partitionFeatures(cfs, ftms, simMatrix);

        return partitioned;
    }

    @Override
    public Affinity getClassAffinity(String className) {
        update(dirtySet);
        return classes.get(className);
    }

    @Override
    public Affinity getPkgAffinity(String pkgName) {
        update(dirtySet);
        return pkgs.get(pkgName);
    }

    @Override
    public Affinity getMethodAffinity(String methodName) {
        update(dirtySet);
        return methods.get(methodName);
    }

    @Override
    public Affinity getFieldAffinity(String fieldName) {
        update(dirtySet);
        return fields.get(fieldName);
    }
}
