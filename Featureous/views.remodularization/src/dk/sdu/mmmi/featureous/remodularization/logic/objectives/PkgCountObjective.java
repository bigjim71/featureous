/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.logic.objectives;

import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.PkgCountMetric;
import dk.sdu.mmmi.featureous.remodularization.spi.RemodularizationObjectiveProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ao
 */
@ServiceProvider(service=RemodularizationObjectiveProvider.class)
public class PkgCountObjective implements RemodularizationObjectiveProvider{

    @Override
    public String getObjectiveName() {
        return "[Other] Pkg count";
    }

    @Override
    public AbstractMetric createObjective() {
        return new PkgCountMetric();
    }

    @Override
    public boolean isMinimization() {
        return false;
    }
    
}
