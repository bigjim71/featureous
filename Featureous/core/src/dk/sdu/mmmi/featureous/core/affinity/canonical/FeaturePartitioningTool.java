/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.affinity.canonical;

import dk.sdu.mmmi.featureous.core.model.OrderedBinaryRelation;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author ao
 */
public class FeaturePartitioningTool {
    private HashMap<OrderedBinaryRelation<String, Double>, Double> similarityMatrix;

    public Map<TraceModel, Set<TraceModel>> partitionFeatures(
            Set<TraceModel> canonicalFeats, Set<TraceModel> allFeats,
            Set<OrderedBinaryRelation<String, Double>> simMatrix){

        this.similarityMatrix = new HashMap<OrderedBinaryRelation<String, Double>, Double>();
        for(OrderedBinaryRelation<String, Double> r : simMatrix){
            this.similarityMatrix.put(r, r.getVal());
        }

        Map<TraceModel, Set<TraceModel>> grouping = new HashMap<TraceModel, Set<TraceModel>>();
        for(TraceModel canFtm : canonicalFeats){
            grouping.put(canFtm, new HashSet<TraceModel>());
            grouping.get(canFtm).add(canFtm);
        }

        for(TraceModel allFtm : allFeats){
            if(canonicalFeats.contains(allFtm)){
                continue;
            }
            double maxRel = 0d;
            TraceModel maxCanFtm = null;
            for(TraceModel canFtm : canonicalFeats){
                double rel = getRelation(allFtm, canFtm);
                rel += getRelation(canFtm, allFtm);
                if(rel>maxRel){
                    maxRel = rel;
                    maxCanFtm = canFtm;
                }
            }
            if(maxCanFtm==null){
                maxCanFtm = allFtm;
                grouping.put(maxCanFtm, new HashSet<TraceModel>());
            }
            grouping.get(maxCanFtm).add(allFtm);
        }

        return grouping;
    }

    private Double getRelation(TraceModel m1, TraceModel m2){
        Double res = 0d;
        res = similarityMatrix.get(new OrderedBinaryRelation<String, Double>(m1.getName(), m2.getName()));
        return res;
    }
}
