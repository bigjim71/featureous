/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.logic.objectives;

import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.featureous.remodularization.metrics.VirtualTangling;
import dk.sdu.mmmi.featureous.remodularization.spi.RemodularizationObjectiveProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ao
 */
@ServiceProvider(service=RemodularizationObjectiveProvider.class)
public class TanglingObjective implements RemodularizationObjectiveProvider{

    @Override
    public String getObjectiveName() {
        return "Tangling of features in packages";
    }

    @Override
    public AbstractMetric createObjective() {
        return new VirtualTangling();
    }

    @Override
    public boolean isMinimization() {
        return true;
    }
    
}
