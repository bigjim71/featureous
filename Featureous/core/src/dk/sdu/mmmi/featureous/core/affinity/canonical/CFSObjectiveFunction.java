/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.affinity.canonical;

import dk.sdu.mmmi.featureous.core.model.OrderedBinaryRelation;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.utils.ga.DecValChromosome;
import dk.sdu.mmmi.utils.ga.ObjectiveFunction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ao
 */
public class CFSObjectiveFunction implements ObjectiveFunction{

    private Double innerEdgesRes = 0d;
    private Double cutEdgesRes = 0d;
    private final Map<OrderedBinaryRelation<String, Double>, Double> similarityMatrix;
    private final List<String> features;
    private final Set<TraceModel> ftms;

    public CFSObjectiveFunction(Set<TraceModel> ftms, Set<OrderedBinaryRelation<String, Double>> similarityMatrix){
        this.features = new ArrayList<String>();
        for(TraceModel ftm : ftms){
            features.add(ftm.getName());
        }
        this.similarityMatrix = new HashMap<OrderedBinaryRelation<String, Double>, Double>();
        for(OrderedBinaryRelation<String, Double> r : similarityMatrix){
            this.similarityMatrix.put(r, r.getVal());
        }
        this.ftms = ftms;
    }

    public double evaluateChromosomeFitness(DecValChromosome arg0) {
        // inner
        Set<String> inners = new HashSet<String>();
        Set<String> outers = new HashSet<String>();
        for(int i = 0; i<arg0.getLength();i++){
            if(arg0.getGeneVal(i)==1){
                inners.add(features.get(i));
            }else if(arg0.getGeneVal(i)==0){
                outers.add(features.get(i));
            }else{
                throw new RuntimeException("Bad value range(not 0-1): " + arg0.getGeneVal(i));
            }
        }

        innerEdgesRes = 0d;
        for(String i1 : inners){
            for(String i2 : inners){
                innerEdgesRes += similarityMatrix.get(new OrderedBinaryRelation(i1, i2));
            }
        }

        cutEdgesRes = 0d;
        for(String o : outers){
            for(String i : inners){
                cutEdgesRes += similarityMatrix.get(new OrderedBinaryRelation(o, i));
                // TODO: confirm this: (dependency diretion stuff)
                cutEdgesRes += similarityMatrix.get(new OrderedBinaryRelation(i, o));
            }
        }

        return cutEdgesRes - innerEdgesRes + ((float)inners.size())/(inners.size()+outers.size());
    }

    public List<Double> getStatsForChromosome(DecValChromosome arg0) {
        List<Double> d = new ArrayList<Double>();
        d.add(innerEdgesRes);
        d.add(cutEdgesRes);
        return d;
    }

    public String[] getStatsLabels() {
        return new String[]{
            "Inner edges",
            "Cut-edges"
        };
    }

    public Set<TraceModel> getCFS(DecValChromosome chr){
        // TODO: copy-paste reuse...
        Set<String> inners = new HashSet<String>();
        for(int i = 0; i<chr.getLength();i++){
            if(chr.getGeneVal(i)==1){
                inners.add(features.get(i));
            }
        }
        
        Set<TraceModel> cfs = new HashSet<TraceModel>();
        for(TraceModel ftm : ftms){
            if(inners.contains(ftm.getName())){
                cfs.add(ftm);
            }
        }

        return cfs;
    }
}
