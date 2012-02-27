/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.logic.objectives;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.featureous.remodularization.metrics.OriginalPackageCount;
import dk.sdu.mmmi.featureous.remodularization.spi.RemodularizationObjectiveProvider;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ao
 */
@ServiceProvider(service=RemodularizationObjectiveProvider.class)
public class ImpactPackageCountObjective implements RemodularizationObjectiveProvider{

    public static int orgPkgCount;

    public static void init(Set<TraceModel> tms, StaticDependencyModel sdm) {
        ImpactPackageCountObjective.orgPkgCount = sdm.getPackages().size();
    }
    
    @Override
    public String getObjectiveName() {
        return "[Impact] Impact on package count (Min)";
    }

    @Override
    public AbstractMetric createObjective() {
        return new OriginalPackageCount(orgPkgCount);
    }

    @Override
    public boolean isMinimization() {
        return true;
    }
    
}
