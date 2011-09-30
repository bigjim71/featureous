/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.affinity.canonical;

import dk.sdu.mmmi.featureous.core.model.OrderedBinaryRelation;
import dk.sdu.mmmi.featureous.core.similarity.SimilarityRelation;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ao
 */
public class SimilarityMeasurementTool {
    private final SimilarityRelation simRel;

    public SimilarityMeasurementTool(SimilarityRelation r){
        this.simRel = r;
    }


    /**
     * TODO: open question - should the infrastructural classes be taken into account here?
     * They will affect clustering process, but maybe normalization of class relations can help?
     * @param ftms
     * @return
     */
    public Set<OrderedBinaryRelation<String, Double>> calculateSimilarityMatrix(Set<TraceModel> ftms){
        Set<OrderedBinaryRelation<String, Double>> sm =
                new HashSet<OrderedBinaryRelation<String, Double>>();

        for(TraceModel f1 : ftms){
            for(TraceModel f2 : ftms){
            // Assumption: we calculate complete matrix
//                if(f1!=f2){
                    sm.add(new OrderedBinaryRelation<String, Double>(
                            f1.getName(), f2.getName(), simRel.getSimilarity(f1, f2)));
//                }
            }
        }

        return sm;
    }
}
