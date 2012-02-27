/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.metrics;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.Set;

/**
 * @author ao
 */
public class OriginalPackageCount extends AbstractMetric{
    private final int orgPkgCount;

    public OriginalPackageCount(int orgPkgCount) {
        super("Original package count difference", AbstractMetric.Scope.SYSTEM);
        this.orgPkgCount = orgPkgCount;
    }
    
    @Override
    public void calculateAll(Set<TraceModel> ftms, StaticDependencyModel sdm) {
        int diff = orgPkgCount - sdm.getPackages().size();
        setResultForSubject(Math.abs(diff), "system");
    }

    @Override
    public float getResult() {
        return getSumVal();
    }
}
