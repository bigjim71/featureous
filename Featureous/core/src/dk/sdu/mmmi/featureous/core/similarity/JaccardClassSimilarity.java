/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.similarity;

import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author ao
 */

@ServiceProvider(service=SimilarityRelation.class)
public class JaccardClassSimilarity implements SimilarityRelation{

    public Double getSimilarity(TraceModel f1, TraceModel f2) {
        Set<String> ef1 = new HashSet<String>();
        Set<String> union = new HashSet<String>();
        for(ClassModel t : f1.getClassSet()){
            union.add(t.getName());
            ef1.add(t.getName());
        }

        Double intersection = 0d;
        for(ClassModel t : f2.getClassSet()){
            union.add(t.getName());
            if(ef1.contains(t.getName())){
                intersection++;
            }
        }

        if(union.size() == 0){
            return 0d;
        }
        
        return intersection/union.size();
    }

    @Override
    public String toString() {
        return "Jaccard classes";
    }
}
