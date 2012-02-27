/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.logic.objectives;

import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.featureous.remodularization.metrics.FTMClassCount;
import dk.sdu.mmmi.featureous.remodularization.spi.RemodularizationObjectiveProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ao
 */
@ServiceProvider(service=RemodularizationObjectiveProvider.class)
public class FTMClassCountObjective implements RemodularizationObjectiveProvider{

    @Override
    public String getObjectiveName() {
        return "[Other] FTM class count";
    }

    @Override
    public AbstractMetric createObjective() {
        return new FTMClassCount();
    }

    @Override
    public boolean isMinimization() {
        return false;
    }
    
}
