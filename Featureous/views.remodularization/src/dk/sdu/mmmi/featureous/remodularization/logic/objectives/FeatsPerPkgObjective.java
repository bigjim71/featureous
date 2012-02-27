/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.logic.objectives;

import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.VirtualTangling;
import dk.sdu.mmmi.featureous.remodularization.spi.RemodularizationObjectiveProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ao
 */
@ServiceProvider(service=RemodularizationObjectiveProvider.class)
public class FeatsPerPkgObjective implements RemodularizationObjectiveProvider{

    @Override
    public String getObjectiveName() {
        return "[Metric] Mean features/package (Min)";
    }

    @Override
    public AbstractMetric createObjective() {
        return new VirtualTangling(false, false);
    }

    @Override
    public boolean isMinimization() {
        return true;
    }
    
}
