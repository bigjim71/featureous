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
public class JaccardFunctionsSimilarity implements SimilarityRelation{

    public Double getSimilarity(TraceModel f1, TraceModel f2) {
        Set<String> ef1 = new HashSet<String>();
        Set<String> union = new HashSet<String>();
        for(ClassModel t : f1.getClassSet()){
            union.addAll(t.getAllMethods());
            ef1.addAll(t.getAllMethods());
        }

        Double intersection = 0d;
        for(ClassModel t : f2.getClassSet()){
            for(String e : t.getAllMethods()){
                union.add(e);
                if(ef1.contains(e)){
                    intersection++;
                }
            }
        }

        if(union.size() == 0){
            return 0d;
        }
        
        return intersection/union.size();
    }

    @Override
    public String toString() {
        return "Jaccard functions";
    }
}
