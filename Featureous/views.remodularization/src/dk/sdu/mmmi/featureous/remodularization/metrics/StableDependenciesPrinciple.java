/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.metrics;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.srcUtils.sdm.metrics.PCoupExport;
import dk.sdu.mmmi.srcUtils.sdm.metrics.PCoupImport;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.Set;

/**
 * @author ao
 */
public class StableDependenciesPrinciple extends AbstractMetric {

    // This measures instability of a package
    public StableDependenciesPrinciple() {
        super("Stable dependencies principle", AbstractMetric.Scope.PACKAGE);
    }

    @Override
    public void calculateAll(Set<TraceModel> ftms, StaticDependencyModel sdm) {

        // instability = Ce/(Ca+Ce)
        for (JPackage pkg : sdm.getPackages()) {
            PCoupImport importCoup = new PCoupImport();
            Double importRes = importCoup.calculate(sdm, pkg);
            
            PCoupExport exportCoup = new PCoupExport();
            Double exportRes = exportCoup.calculate(sdm, pkg);
                
            if(importRes+exportRes != 0){
                setResultForSubject((float)(exportRes/(importRes+exportRes)), pkg.getQualName());
            }
        }
    }

    @Override
    public float getResult() {
        return getMeanVal();
    }
}
