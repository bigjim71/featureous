/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.metrics.oometrics;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.srcUtils.sdm.metrics.PCoupImport;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.Set;

/**
 *
 * @author aolszak
 */
public class PkgCouplingMetric extends AbstractMetric{

    public PkgCouplingMetric() {
        super("Sum pkg coupling", AbstractMetric.Scope.PACKAGE);
    }

    @Override
    public float getResult() {
        return getSumVal();
    }

    @Override
    public void calculateAll(Set<TraceModel> tms, StaticDependencyModel sdm) {
        for(JPackage pkg : sdm.getPackages()){
            PCoupImport metric = new PCoupImport();
            Double res = metric.calculate(sdm, pkg);
            if(res != null){
                setResultForSubject(res.floatValue(), pkg.getQualName());
            }
        }
    }
}
