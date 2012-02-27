/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.metrics;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.Set;

/**
 * @author ao
 */
public class PkgSizeDistribution extends AbstractMetric{


    public PkgSizeDistribution() {
        super("StdDev of package sizes", AbstractMetric.Scope.PACKAGE);
    }
    
    @Override
    public void calculateAll(Set<TraceModel> ftms, StaticDependencyModel sdm) {
        for(JPackage pkg : sdm.getPackages()){
            setResultForSubject(pkg.getTopLevelTypes().size(), pkg.getQualName());
        }
    }

    @Override
    public float getResult() {
        return getStdDev();
    }

}
