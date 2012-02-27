/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.logic.objectives;

import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.featureous.remodularization.metrics.CommonReusePrinciple;
import dk.sdu.mmmi.featureous.remodularization.spi.RemodularizationObjectiveProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ao
 */
@ServiceProvider(service=RemodularizationObjectiveProvider.class)
public class CommonReusePrincipleObjective implements RemodularizationObjectiveProvider{

    @Override
    public String getObjectiveName() {
        return "[Principle] Classes reused together packaged together (Max)";
    }

    @Override
    public AbstractMetric createObjective() {
        return new CommonReusePrinciple();
    }

    @Override
    public boolean isMinimization() {
        return false;
    }
    
}
