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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ao
 */
public class VirtualTangling extends AbstractMetric{


    public VirtualTangling() {
        super("Virtual tangling", AbstractMetric.Scope.PACKAGE);
    }

    @Override
    public void calculateAll(Set<TraceModel> ftms, StaticDependencyModel sdm) {
        Double res = 0d;

        List<JPackage> pkgs = ScaTangUtil.getNonInsulatedPackages(ftms, sdm);

        if(pkgs.size()==0 || ftms.size()==0){
            return;
        }

        for(JPackage pack : pkgs){
            res = tang(ftms, pack, sdm);
            setResultForSubject(res.floatValue()/((float)ftms.size()*pkgs.size()), pack.getQualName());
        }
    }

    private Double tang(Set<TraceModel> ftms, JPackage pack, StaticDependencyModel dm) {
        Set<TraceModel> f = new HashSet<TraceModel>();
        for(TraceModel ftm : ftms){
            for (ClassModel t : ftm.getClassSet()) {
                JPackage p = Util.getTypesPackage(t.getName(), dm);
                if (p != null && p.getQualName().equals(pack.getQualName())) {
                    f.add(ftm);
                }
            }
        }

        if(f.size() > ftms.size() || f.size()==0){
            throw new RuntimeException("Bug calculating f");
        }

        return (double) f.size();
    }

    @Override
    public float getResult() {
        return getMeanVal();
    }

}
