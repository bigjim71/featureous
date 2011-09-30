/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.metrics.concernmetrics;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.Set;

/**
 *
 * @author aolszak
 */
public class MeanPkgCountMetric extends AbstractMetric{

    public MeanPkgCountMetric() {
        super("Pkgs/feature", AbstractMetric.Scope.FEATURE);
    }

    @Override
    public float getResult() {
        return getMeanVal();
    }

    @Override
    public void calculateAll(Set<TraceModel> tms, StaticDependencyModel sdm) {
        for(TraceModel tm : tms){
            setResultForSubject((float)tm.getPackageNames().size(), tm.getName());
        }
    }
}
