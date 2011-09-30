/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.similarity;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.model.OrderedBinaryRelation;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author ao
 */

@ServiceProvider(service=SimilarityRelation.class)
public class JaccardCallsSimilarity implements SimilarityRelation{

    public Double getSimilarity(TraceModel f1, TraceModel f2) {
        Set<OrderedBinaryRelation> if1 = new HashSet<OrderedBinaryRelation>();
        if1.addAll(f1.getInterTypeInvocations());
        int s1 = if1.size();
        if1.removeAll(f2.getInterTypeInvocations());
        double intersection = s1 - if1.size();

        if1.addAll(f2.getInterTypeInvocations());
        double union = if1.size();
        if(union == 0){
            return 0d;
        }
        
        return intersection/union;
    }

    @Override
    public String toString() {
        return "Jaccard invocations";
    }

}
