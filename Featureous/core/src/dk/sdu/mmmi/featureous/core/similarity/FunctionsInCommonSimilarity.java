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
public class FunctionsInCommonSimilarity implements SimilarityRelation{

    public Double getSimilarity(TraceModel f1, TraceModel f2) {
        Set<String> ef1 = new HashSet<String>();
        for(ClassModel t : f1.getClassSet()){
            ef1.addAll(t.getAllMethods());
        }

        Double res = 0d;
        for(ClassModel t : f2.getClassSet()){
            for(String e : t.getAllMethods()){
                if(ef1.contains(e)){
                    res++;
                }
            }
        }
        return res;
    }

    @Override
    public String toString() {
        return "Functions in common";
    }
}
