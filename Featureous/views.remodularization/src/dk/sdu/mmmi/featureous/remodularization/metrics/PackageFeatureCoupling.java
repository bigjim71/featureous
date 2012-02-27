/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.metrics;

import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.ScaTangUtil;
import dk.sdu.mmmi.srcUtils.sdm.model.JDependency;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import dk.sdu.mmmi.srcUtils.sdm.model.Util;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ao
 */
public class PackageFeatureCoupling extends AbstractMetric {

    public PackageFeatureCoupling() {
        super("Package feature import coupling", AbstractMetric.Scope.FEATURE);
    }

    @Override
    public void calculateAll(Set<TraceModel> ftms, StaticDependencyModel sdm) {

        if (ftms.size() == 0) {
            return;
        }

        for (JPackage pkg : sdm.getPackages()) {
            double pkgRes = 0d;
            int featCount = 0;
            for (TraceModel tm : ftms) {
                Set<JPackage> dependee = new HashSet<JPackage>();
                double featRes = 0d;
                int classCount = 0;
                for (JType type : pkg.getAllTypes()) {
                    ClassModel cm = tm.getClass(type.getQualName());
                    if(cm!=null){
                        classCount++;
                        for(JDependency jdep : type.getDependencies()){
                            JType depType = jdep.getReferencedType();
                            //if dependee is from the same package but does not belong to the same feature...
                            if(Util.getTypesPackage(depType.getQualName(), sdm).getQualName().equals(pkg.getQualName())
                                && tm.getClass(depType.getQualName())==null){
                                featRes++;
                            }
                        }
                    }
                }
                if(classCount>0){
                    featCount++;
                }
                pkgRes+=featRes;
            }
            
            setResultForSubject((float)pkgRes, pkg.getQualName());
        }
    }

    @Override
    public float getResult() {
        return getMeanVal();
    }
}
