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
public class RelativeSizeOfPkgInterface extends AbstractMetric {

    // This measures instability of a package
    public RelativeSizeOfPkgInterface() {
        super("Relative size of package's interface", AbstractMetric.Scope.PACKAGE);
    }

    @Override
    public void calculateAll(Set<TraceModel> ftms, StaticDependencyModel sdm) {

        // instability = Ce/(Ca+Ce)
        for (JPackage pkg : sdm.getPackages()) {
            Set<JType> interfTypes = new HashSet<JType>();
            for(JType t : pkg.getTopLevelTypes()){
                for(JType td : t.getIncomingDeps()){
                    JPackage pkg2 = dk.sdu.mmmi.srcUtils.sdm.model.Util.getTypesPackage(td, sdm);
                    if(pkg2!=null && !pkg2.getQualName().equals(pkg.getQualName())){
                        interfTypes.add(t);
                        break;
                    }
                }
            }

            if(pkg.getTopLevelTypes().size() > 0){
                setResultForSubject(interfTypes.size()/((float)pkg.getTopLevelTypes().size()), pkg.getQualName());
            }
        }
    }

    @Override
    public float getResult() {
        return getMeanVal();
    }
}
