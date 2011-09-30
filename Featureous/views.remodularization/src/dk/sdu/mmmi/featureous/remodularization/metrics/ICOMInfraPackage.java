/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.metrics;

import dk.sdu.mmmi.featureous.core.model.OrderedBinaryRelation;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ao
 */

public class ICOMInfraPackage extends AbstractMetric {

    public ICOMInfraPackage() {
        super("ICOMInfraPackage coupling", AbstractMetric.Scope.PACKAGE);
    }
    
    @Override
    public void calculateAll(Set<TraceModel> tms, StaticDependencyModel sdm) {
        Set<OrderedBinaryRelation<String, Integer>> invs = new HashSet<OrderedBinaryRelation<String, Integer>>();
        for(TraceModel tm : tms){
            invs.addAll(tm.getInterTypeInvocations());
        }
        for(JPackage p : sdm.getPackages()){
            setResultForSubject(calculate(invs, p), p.getQualName());
        }
    }
    
    private float calculate(Set<OrderedBinaryRelation<String, Integer>> invs, JPackage p){
        float res = 0f;
        for(OrderedBinaryRelation<String, Integer> i : invs){
            String caller = i.getFirst();
            String called = i.getSecond();
            if(caller != null){
                JType tCaller = p.getTypeByQualNameOrNull(caller);
                JType tCalled = p.getTypeByQualNameOrNull(called);
                if(tCaller != null && tCalled != null && tCaller != tCalled){
                    res +=i.getVal();
                }
            }
        }
        return res;
    }

    @Override
    public float getResult() {
        return getMeanVal();
    }
}
