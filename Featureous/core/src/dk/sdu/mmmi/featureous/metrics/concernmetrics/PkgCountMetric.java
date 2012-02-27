/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.metrics.concernmetrics;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.Set;

/**
 *
 * @author aolszak
 */
public class PkgCountMetric extends AbstractMetric{

    public PkgCountMetric() {
        super("Pkg count", AbstractMetric.Scope.SYSTEM);
    }

    @Override
    public void calculateAll(Set<TraceModel> tms, StaticDependencyModel sdm) {
        setResultForSubject((float)sdm.getPackages().size(), "res");
    }
    
    @Override
    public float getResult() {
        return getMeanVal();
    }

}
