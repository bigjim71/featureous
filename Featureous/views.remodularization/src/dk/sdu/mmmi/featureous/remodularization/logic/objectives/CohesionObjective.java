/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.logic.objectives;

import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.featureous.metrics.oometrics.PkgCohesionMetric;
import dk.sdu.mmmi.featureous.remodularization.spi.RemodularizationObjectiveProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ao
 */
@ServiceProvider(service=RemodularizationObjectiveProvider.class)
public class CohesionObjective implements RemodularizationObjectiveProvider{

    @Override
    public String getObjectiveName() {
        return "[Metric] Package cohesion (Max)";
    }

    @Override
    public AbstractMetric createObjective() {
        return new PkgCohesionMetric();
    }

    @Override
    public boolean isMinimization() {
        return false;
    }
    
}
