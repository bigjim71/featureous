/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.metrics;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.featureous.metrics.Result;
import dk.sdu.mmmi.srcUtils.sdm.metrics.PCoupExport;
import dk.sdu.mmmi.srcUtils.sdm.metrics.PCoupImport;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.Set;

/**
 * @author ao
 */
public class StableAbstractionsPrinciple extends AbstractMetric {

    // This measures instability of a package
    public StableAbstractionsPrinciple() {
        super("Stable dependencies principle", AbstractMetric.Scope.PACKAGE);
    }

    @Override
    public void calculateAll(Set<TraceModel> ftms, StaticDependencyModel sdm) {

        StableDependenciesPrinciple sdp = new StableDependenciesPrinciple();
        sdp.calculateAll(ftms, sdm);
        
        for (JPackage pkg : sdm.getPackages()) {
            Result i = sdp.getResultFor(pkg.getQualName());
            float acs = 0f;
            for(JType t : pkg.getTopLevelTypes()){
                if(t.isAbstractType() || t.isInterfaceType()){
                    acs++;
                }
            }
            
            if(i!=null && i.value!=null && pkg.getTopLevelTypes().size()>0){
                //Distance from main sequence
                setResultForSubject(Math.abs( i.value + (acs/((float)pkg.getTopLevelTypes().size())) - 1), pkg.getQualName());
            }
        }
    }

    @Override
    public float getResult() {
        return getMeanVal();
    }
}
