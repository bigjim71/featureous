/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.logic.objectives;

import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.featureous.remodularization.metrics.StableAbstractionsPrinciple;
import dk.sdu.mmmi.featureous.remodularization.spi.RemodularizationObjectiveProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ao
 */
@ServiceProvider(service=RemodularizationObjectiveProvider.class)
public class StableAbstractionsPrincipleObjective implements RemodularizationObjectiveProvider{

    @Override
    public String getObjectiveName() {
        return "[Principle] Distance from abstract/instable sequence (Min)";
    }

    @Override
    public AbstractMetric createObjective() {
        return new StableAbstractionsPrinciple();
    }

    @Override
    public boolean isMinimization() {
        return true;
    }
    
}
