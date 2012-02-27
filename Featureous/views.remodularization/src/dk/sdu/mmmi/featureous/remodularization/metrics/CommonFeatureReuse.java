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
public class CommonFeatureReuse extends AbstractMetric{

    public CommonFeatureReuse() {
        super("Feature-oriented common reuse principle", AbstractMetric.Scope.PACKAGE);
    }

    @Override
    public void calculateAll(Set<TraceModel> ftms, StaticDependencyModel sdm) {

        if (ftms.size() == 0) {
            return;
        }

        for(JPackage pkg : sdm.getPackages()){
            Set<String> featsUsing = new HashSet<String>();
            int productCount = 0;
            for(TraceModel tm : ftms){
                for(JType t : pkg.getTopLevelTypes()){
                    if(tm.getClass(t.getQualName())!=null){
                        featsUsing.add(tm.getName());
                        productCount++;
                    }
                }
            }
            if(productCount>0){
                setResultForSubject((float)productCount/((float)(featsUsing.size()*pkg.getTopLevelTypes().size())), pkg.getQualName());
            }
        }
    }

    @Override
    public float getResult() {
        return getMeanVal();
    }
}
