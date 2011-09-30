/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.metrics;

import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.featureous.remodularization.spi.RemodularizationObjectiveProvider;

/**
 *
 * @author ao
 */
public class VirtualScatteringObjective implements RemodularizationObjectiveProvider{

    @Override
    public String getObjectiveName() {
        return "Scattering of features over packages";
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
