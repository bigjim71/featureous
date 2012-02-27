/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.metrics.concernmetrics;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.srcUtils.sdm.model.JDependency;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.Set;

/**
 *
 * @author aolszak
 */
public class FeatureLOCMetric extends AbstractMetric {

    public FeatureLOCMetric(String name, Scope scope) {
        super(name, scope);
    }
    
    public FeatureLOCMetric() {
        super("Mean feature LOC", AbstractMetric.Scope.FEATURE);
    }

    @Override
    public float getResult() {
        return getMeanVal();
    }

    @Override
    public void calculateAll(Set<TraceModel> tms, StaticDependencyModel sdm) {
        for (TraceModel tm : tms) {
            float res = 0f;
            for (JPackage pkg : sdm.getPackages()) {
                for(JType t : pkg.getAllTypes()){
                    if(tm.getClass(t.getQualName())!=null){
                        res+=t.getLoc();
                    }
                }
            }
            setResultForSubject(res, tm.getName());
        }
    }
    
    protected boolean isInBounds(){
        return false;
    }
}
