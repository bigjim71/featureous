/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.metrics.concernmetrics;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.featureous.metrics.Result;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author aolszak
 */
public class FeatCohesionMetric extends FeatCouplingMetric {

    public FeatCohesionMetric() {
        super("Feature cohesion", AbstractMetric.Scope.FEATURE);
    }

    @Override
    public void calculateAll(Set<TraceModel> tms, StaticDependencyModel sdm) {
        super.calculateAll(tms, sdm);
        Set<Result> normRess = new HashSet<Result>();
        for(Result r :getResults()){
            TraceModel tmm = null;
            for(TraceModel tm : tms){
                if(tm.getName().equals(r.name)){
                    tmm = tm;
                    break;
                }
            }
            Result newRes = new Result(r.value/((float)tmm.getClassSet().size()*(tmm.getClassSet().size()-1)/2f), r.name);
            normRess.add(newRes);
        }
        
        for(Result norm : normRess){
            setResultForSubject(norm.value, norm.name);
        }
    }

    @Override
    public float getResult() {
        return getMeanVal();
    }
    

    @Override
    protected boolean isInBounds() {
        return true;
    }
}
