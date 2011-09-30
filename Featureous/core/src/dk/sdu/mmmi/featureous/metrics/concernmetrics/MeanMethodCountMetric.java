/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.metrics.concernmetrics;

import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.Set;

/**
 *
 * @author aolszak
 */
public class MeanMethodCountMetric extends AbstractMetric {

    public MeanMethodCountMetric() {
        super("Avg methods/feature", AbstractMetric.Scope.FEATURE);
    }

    @Override
    public float getResult() {
        return getMeanVal();
    }

    @Override
    public void calculateAll(Set<TraceModel> tms, StaticDependencyModel sdm) {
        for (TraceModel tm : tms) {
            int count = 0;
            for (ClassModel cm : tm.getClassSet()) {
                count += cm.getAllMethods().size();
            }
            setResultForSubject((float) count, tm.getName());
        }
    }
}
