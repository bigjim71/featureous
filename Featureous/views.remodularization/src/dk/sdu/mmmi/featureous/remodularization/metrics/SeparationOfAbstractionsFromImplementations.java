/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.metrics;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.srcUtils.sdm.model.JDependency;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ao
 */
public class SeparationOfAbstractionsFromImplementations extends AbstractMetric {

    public SeparationOfAbstractionsFromImplementations() {
        super("Separation of abstractions from their implementations", AbstractMetric.Scope.PACKAGE);
    }

    @Override
    public void calculateAll(Set<TraceModel> ftms, StaticDependencyModel sdm) {

        for (JPackage pkg : sdm.getPackages()) {
            if (pkg.getTopLevelTypes().size() == 0) {
                continue;
            }

            Set<JType> abstrs = new HashSet<JType>();

            for (JType t : pkg.getTopLevelTypes()) {
                if (t.isAbstractType() || t.isInterfaceType()) {
                    abstrs.add(t);
                }
            }

            float pkgSum = 0f;

            for (JType t : abstrs) {
                int sum = 0;
                int num = 0;
                Set<JType> subs = new HashSet<JType>();
                for (JType ref : t.getIncomingDeps()) {
                    for (JDependency refd : ref.getSuperDependencies()) {
                        if (refd.getReferencedType().getQualName().equals(t.getQualName())) {
                            subs.add(ref);
                        }
                    }
                }

                if (subs.size() > 1) {
                    for (JType tt : subs) {
                        JPackage pkg2 = dk.sdu.mmmi.srcUtils.sdm.model.Util.getTypesPackage(tt, sdm);
                        if (pkg2 != null) {
                            if (!pkg2.getQualName().equals(pkg.getQualName())) {
                                sum ++; 
                            }
                            num++;
                        }
                    }
                    pkgSum += sum/(float)num;
                }else{
                    pkgSum+=1;
                }
            }

            if (pkg.getTopLevelTypes().size() > 0) {
                setResultForSubject(pkgSum / pkg.getTopLevelTypes().size(), pkg.getQualName());
            }
        }
    }

    @Override
    public float getResult() {
        return getMeanVal();
    }
}
