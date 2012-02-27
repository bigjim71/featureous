/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.metrics;

import dk.sdu.mmmi.featureous.core.affinity.Affinity;
import dk.sdu.mmmi.featureous.core.affinity.AffinityProvider;
import dk.sdu.mmmi.featureous.core.controller.Controller;
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
public class LocalizedFeatureSpecifics extends AbstractMetric {

    public LocalizedFeatureSpecifics() {
        super("Localization of feature-specific classes", AbstractMetric.Scope.FEATURE);
    }

    @Override
    public void calculateAll(Set<TraceModel> ftms, StaticDependencyModel sdm) {

        if (ftms.size() == 0) {
            return;
        }
        AffinityProvider ap = Controller.getInstance().getAffinity();

        for (TraceModel tm : ftms) {
            Set<String> pkgsOfSpecs = new HashSet<String>();
            for (JPackage pkg : sdm.getPackages()) {
                for (JType t : pkg.getTopLevelTypes()) {
                    if (tm.getClass(t.getQualName()) != null 
                            && Affinity.SINGLE_FEATURE.equals(ap.getClassAffinity(t.getQualName()))) {
                        pkgsOfSpecs.add(pkg.getQualName());
                    }
                }
            }

            setResultForSubject(1f / pkgsOfSpecs.size(), tm.getName());
        }
    }

    @Override
    public float getResult() {
        return getMeanVal();
    }
}
