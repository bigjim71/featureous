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

        List<JPackage> packs = new ArrayList<JPackage>(sdm.getPackages());
        packs.removeAll(ScaTangUtil.getInsulatedPackages(ftms, sdm));

        if(packs.size()==0 || ftms.size()==0){
            return;
        }

        for(JPackage pack : packs){
            res += tang(ftms, pack, sdm);
            setResultForSubject(res.floatValue()/((float)ftms.size()*packs.size()), pack.getQualName());
        }
    }

    private Double tang(Set<TraceModel> ftms, JPackage pack, StaticDependencyModel dm) {
        List<TraceModel> f = new ArrayList<TraceModel>();
        for(TraceModel ftm : ftms){
            for (ClassModel t : ftm.getClassSet()) {
                JPackage p = Util.getTypesPackage(t.getName(), dm);
                if (p != null && p.getQualName().equals(pack.getQualName())
                        && !f.contains(ftm)) {
                    f.add(ftm);
                }
            }
        }

        if(f.size() > ftms.size()){
            throw new RuntimeException("Bug calculating f");
        }

        if(f.size()==0){
            return 0d;
        }
        return f.size() - 1d;
    }

    @Override
    public float getResult() {
        return getMeanVal();
    }

}
