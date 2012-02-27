/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.metrics;

import dk.sdu.mmmi.featureous.core.model.OrderedBinaryRelation;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.srcUtils.sdm.model.JDependency;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.Set;

/**
 * @author ao
 */
public class SeparationOfClientsFromImplementations extends AbstractMetric {

    public SeparationOfClientsFromImplementations() {
        super("Separation of clients from service implementations", AbstractMetric.Scope.PACKAGE);
    }

    @Override
    public void calculateAll(Set<TraceModel> ftms, StaticDependencyModel sdm) {

        for (JPackage pkg : sdm.getPackages()) {
            if (pkg.getAllTypes().size() == 0) {
                continue;
            }
            int pkgPenalty = 0;
            for (JType t : pkg.getAllTypes()) {
                for (JType t2 : pkg.getAllTypes()) {
                    if (t != t2) {
                        boolean call = false;
                        for (TraceModel tm : ftms) {
                            for (OrderedBinaryRelation<String, Integer> rr : tm.getInterTypeInvocations()) {
                                if (rr.getFirst().equals(t.getQualName())
                                        && rr.getSecond().equals(t2.getQualName())) {
                                    call = true;
                                    break;
                                }
                            }
                        }
                        if (call) {
                            boolean dp = false;
                            for (JDependency dep : t.getDependencies()) {
                                if (!dep.getKind().equals(JDependency.Kind.TO_SUPER)
                                        && dep.getReferencedType() == t2) {
                                    dp = true;
                                    break;
                                }
                            }
                            if(!dp){
                                pkgPenalty++;
                            }
                        }
                    }
                }
            }

            setResultForSubject(pkgPenalty, pkg.getQualName());
        }
    }

    @Override
    public float getResult() {
        return getSumVal();
    }
}
