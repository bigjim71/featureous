/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.metrics;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ao
 */
public class SDMClassCount extends AbstractMetric{

    public SDMClassCount() {
        super("SDM class count", AbstractMetric.Scope.SYSTEM);
    }

    @Override
    public void calculateAll(Set<TraceModel> ftms, StaticDependencyModel sdm) {
        Set<String> classes = new HashSet<String>();
        for(JPackage pkg : sdm.getPackages()){
            for(JType t : pkg.getTopLevelTypes()){
                classes.add(t.getQualName());
            }
        }
        setResultForSubject(classes.size(), "res");
    }

    @Override
    public float getResult() {
        return getSumVal();
    }
}
