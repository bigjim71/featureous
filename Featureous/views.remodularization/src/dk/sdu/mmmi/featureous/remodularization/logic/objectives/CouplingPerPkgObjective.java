/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.logic.objectives;

import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.featureous.metrics.oometrics.PkgCouplingMetric;
import dk.sdu.mmmi.featureous.remodularization.spi.RemodularizationObjectiveProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ao
 */
@ServiceProvider(service=RemodularizationObjectiveProvider.class)
public class CouplingPerPkgObjective implements RemodularizationObjectiveProvider{

    @Override
    public String getObjectiveName() {
        return "[Metric] Mean inter-package coupling (Min)";
    }

    @Override
    public AbstractMetric createObjective() {
        return new PkgCouplingMetric(){

            @Override
            public float getResult() {
                return this.getMeanVal();
            }
        };
    }

    @Override
    public boolean isMinimization() {
        return true;
    }
    
}
