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
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import dk.sdu.mmmi.srcUtils.sdm.model.Util;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ao
 */
public class VirtualScattering extends AbstractMetric{


    public VirtualScattering() {
        super("Virtual scattering", AbstractMetric.Scope.FEATURE);
    }
    
    @Override
    public void calculateAll(Set<TraceModel> ftms, StaticDependencyModel sdm) {
        Double res = 0d;

        List<JPackage> pkgs = ScaTangUtil.getNonInsulatedPackages(ftms, sdm);

        if(ftms.size()==0 || pkgs.size()==0){
            return;
        }

        for(TraceModel ftm : ftms){
            res = sca(ftm, sdm);
            setResultForSubject(res.floatValue()/((float)ftms.size()*pkgs.size()), ftm.getName());
        }
    }

    private Double sca(TraceModel ftm, StaticDependencyModel dm) {
        Set<JPackage> a = new HashSet<JPackage>();
        for (ClassModel t : ftm.getClassSet()) {
            JPackage p = Util.getTypesPackage(t.getName(), dm);
            if (p != null) {
                a.add(p);
            }
        }

        if(a.size()<1){
//            throw new RuntimeException("Bug calculating a");
        }

        return (double)a.size();
    }

    @Override
    public float getResult() {
        return getMeanVal();
    }

}
