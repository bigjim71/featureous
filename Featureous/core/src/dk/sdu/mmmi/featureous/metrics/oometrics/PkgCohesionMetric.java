/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.metrics.oometrics;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.srcUtils.sdm.metrics.PCoh;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.Set;

/**
 *
 * @author aolszak
 */
public class PkgCohesionMetric extends AbstractMetric {

    public PkgCohesionMetric() {
        super("Pkg cohesion", AbstractMetric.Scope.PACKAGE);
    }

    @Override
    public float getResult() {
        return getMeanVal();
    }

    @Override
    public void calculateAll(Set<TraceModel> tms, StaticDependencyModel sdm) {
        for (JPackage pkg : sdm.getPackages()) {
            PCoh metric = new PCoh();
            Double res = metric.calculate(sdm, pkg);
            if (res != null) {
                setResultForSubject(res.floatValue(), pkg.getQualName());
            }
        }
    }
}
