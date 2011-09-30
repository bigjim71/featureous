/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.affinity.canonical;

import dk.sdu.mmmi.featureous.core.model.OrderedBinaryRelation;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.utils.ga.DecValChromosome;
import dk.sdu.mmmi.utils.ga.GA;
import java.util.Set;

/**
 * @author ao
 */
public class CanonicalFeatureSetTool {

    public Set<TraceModel> calculateCFS(Set<TraceModel> ftms,
            Set<OrderedBinaryRelation<String, Double>> similarityMatrix){

        CFSObjectiveFunction gf = new CFSObjectiveFunction(ftms, similarityMatrix);

        DecValChromosome prototype = new DecValChromosome(ftms.size(), 0, 1);
        GA ga = new GA(gf, 500, 0.05, prototype);

        ga.evolve(100, null);
        
        Set<TraceModel> cfs = gf.getCFS(ga.getCurrentlyBestChromosomeEntry().getFirst());

        return cfs;
    }
}
