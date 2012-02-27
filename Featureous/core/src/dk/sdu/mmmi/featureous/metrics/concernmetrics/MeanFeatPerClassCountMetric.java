/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.metrics.concernmetrics;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author aolszak
 */
public class MeanFeatPerClassCountMetric extends AbstractMetric{

    public MeanFeatPerClassCountMetric() {
        super("Features/class", AbstractMetric.Scope.CLASS);
    }

    @Override
    public float getResult() {
        return getMeanVal();
    }

    @Override
    public void calculateAll(Set<TraceModel> tms, StaticDependencyModel sdm) {
        for(JPackage pkg : sdm.getPackages()){
            for(JType t : pkg.getTopLevelTypes()){
                Set<String> feats = new HashSet<String>();
                for(TraceModel tm : tms){
                    if(tm.getClass(t.getQualName())!=null){
                        feats.add(tm.getName());
                    }
                }

                if(feats.size()>0){
                    //Insulated filtered out
                    setResultForSubject((float)feats.size(), t.getQualName());
                }
            }
        }
    }
}
