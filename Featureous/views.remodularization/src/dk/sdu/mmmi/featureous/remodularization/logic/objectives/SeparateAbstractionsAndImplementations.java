/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.logic.objectives;

import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.featureous.remodularization.metrics.SeparationOfAbstractionsFromImplementations;
import dk.sdu.mmmi.featureous.remodularization.spi.RemodularizationObjectiveProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ao
 */
@ServiceProvider(service=RemodularizationObjectiveProvider.class)
public class SeparateAbstractionsAndImplementations implements RemodularizationObjectiveProvider{

    @Override
    public String getObjectiveName() {
        return "[Principle] Separation of abstractions and implementations (Max)";
    }

    @Override
    public AbstractMetric createObjective() {
        return new SeparationOfAbstractionsFromImplementations();
    }

    @Override
    public boolean isMinimization() {
        return false;
    }
    
}
