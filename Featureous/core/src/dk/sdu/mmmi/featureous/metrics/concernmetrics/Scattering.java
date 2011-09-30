/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.metrics.concernmetrics;

import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.featureous.metrics.Result;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author ao
 */
public class Scattering extends AbstractMetric{


    private final boolean pkgScope;
    
    public Scattering(boolean pkgScope) {
        super("Scattering over" + ((pkgScope)?"pkgs":"class"), AbstractMetric.Scope.FEATURE);
        this.pkgScope = pkgScope;
    }

    @Override
    public float getResult() {
        return getMeanVal();
    }

    @Override
    public void calculateAll(Set<TraceModel> tms, StaticDependencyModel sdm) {
        for(Result res : calculate(new ArrayList<TraceModel>
            (tms), pkgScope)){
            setResultForSubject(res.value.floatValue(), res.name);
        }
    }
    private static List<Result> calculate(List<TraceModel> traces, boolean pkgScope){
        Collections.sort(traces);
        Set<String> allElements = (pkgScope)?ScaTangUtil.getNonInsulatedPackages(new HashSet<TraceModel>(traces))
                :ScaTangUtil.getNonInsulatedClasses(new HashSet<TraceModel>(traces));
        
        if(traces.size() == 0 || allElements.size() == 0){
            return new ArrayList<Result>();
        }
        List<Result> ress = new ArrayList<Result>();
        for (TraceModel traceModel : traces) {
            double res = sca(traceModel, pkgScope) / ((double)traces.size()*allElements.size());
            ress.add(new Result((float)res, traceModel.getName()));
        }

        return ress;
    }
    
    private static double sca(TraceModel traceModel, boolean pkgScope){
        Set<String> usedElements = new HashSet<String>();
        for(ClassModel cm : traceModel.getClassSet()){
            if(pkgScope){
                if(cm.getPackageName() != null){
                    usedElements.add(cm.getPackageName());
                }
            }else{
                if(cm.getName() != null){
                    usedElements.add(cm.getName());
                }
            }
        }
        return usedElements.size();
    }
}
