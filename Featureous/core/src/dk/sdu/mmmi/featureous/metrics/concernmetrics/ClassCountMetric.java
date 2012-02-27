/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.metrics.concernmetrics;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.Set;

/**
 *
 * @author aolszak
 */
public class ClassCountMetric extends AbstractMetric{

    public ClassCountMetric() {
        super("Class count", AbstractMetric.Scope.SYSTEM);
    }

    @Override
    public void calculateAll(Set<TraceModel> tms, StaticDependencyModel sdm) {
        int c = 0;
        for(JPackage p : sdm.getPackages()){
            c += p.getTopLevelTypes().size();
        }
        setResultForSubject((float)c, "res");
    }
    
    @Override
    public float getResult() {
        return getMeanVal();
    }

}
