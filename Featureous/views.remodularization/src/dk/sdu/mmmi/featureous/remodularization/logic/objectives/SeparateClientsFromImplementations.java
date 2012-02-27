/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.logic.objectives;

import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.featureous.remodularization.metrics.SeparationOfClientsFromImplementations;
import dk.sdu.mmmi.featureous.remodularization.spi.RemodularizationObjectiveProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ao
 */
@ServiceProvider(service=RemodularizationObjectiveProvider.class)
public class SeparateClientsFromImplementations implements RemodularizationObjectiveProvider{

    @Override
    public String getObjectiveName() {
        return "[Principle] Separation of clients from implementations (Min)";
    }

    @Override
    public AbstractMetric createObjective() {
        return new SeparationOfClientsFromImplementations();
    }

    @Override
    public boolean isMinimization() {
        return true;
    }
    
}
