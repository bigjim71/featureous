/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.logic.objectives;

import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.VirtualScattering;
import dk.sdu.mmmi.featureous.remodularization.spi.RemodularizationObjectiveProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ao
 */
@ServiceProvider(service=RemodularizationObjectiveProvider.class)
public class ScatteringObjective implements RemodularizationObjectiveProvider{

    @Override
    public String getObjectiveName() {
        return "[Metric] Scattering of features over packages (Min)";
    }

    @Override
    public AbstractMetric createObjective() {
        return new VirtualScattering();
    }

    @Override
    public boolean isMinimization() {
        return true;
    }
    
}
