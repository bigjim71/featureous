/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.logic.objectives;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.VirtualScattering;
import dk.sdu.mmmi.featureous.remodularization.spi.RemodularizationObjectiveProvider;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ao
 */
@ServiceProvider(service=RemodularizationObjectiveProvider.class)
public class ImpactScatteringObjective implements RemodularizationObjectiveProvider{

    public static float orgVal;

    protected static AbstractMetric instantiateMetric(){
        return new VirtualScattering();
    }
    
    public static final void init(Set<TraceModel> tms, StaticDependencyModel sdm) {
        AbstractMetric m = instantiateMetric();
        m.calculateAll(tms, sdm);
        orgVal = m.getResult();
    }
    
    @Override
    public String getObjectiveName() {
        return "[Impact] Impact on scattering of features (Min)";
    }

    @Override
    public final AbstractMetric createObjective() {
        final AbstractMetric m = instantiateMetric();
        return new AbstractMetric(m.getName(), m.getScope()) {

            private final AbstractMetric metric = m;
            
            @Override
            public void calculateAll(Set<TraceModel> tms, StaticDependencyModel sdm) {
                metric.calculateAll(tms, sdm);
            }

            @Override
            public float getResult() {
                return Math.abs(metric.getResult() - orgVal);
            }
        };
    }

    @Override
    public final boolean isMinimization() {
        return true;
    }
}
