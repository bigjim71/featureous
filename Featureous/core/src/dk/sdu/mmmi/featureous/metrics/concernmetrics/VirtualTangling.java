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
import java.util.List;
import java.util.Set;

/**
 * @author ao
 */
public class VirtualTangling extends AbstractMetric {

    private final boolean normalize;
    private final boolean scaleByClassCount;

    public VirtualTangling() {
        this(true, false);
    }

    public VirtualTangling(boolean normalize, boolean scaleByClassCount) {
        super("Virtual tangling", AbstractMetric.Scope.PACKAGE);
        this.normalize = normalize;
        this.scaleByClassCount = scaleByClassCount;
    }

    @Override
    public void calculateAll(Set<TraceModel> ftms, StaticDependencyModel sdm) {
        Double res = 0d;

        List<JPackage> pkgs = ScaTangUtil.getNonInsulatedPackages(ftms, sdm);

        if (pkgs.size() == 0 || ftms.size() == 0) {
            return;
        }

        for (JPackage pack : pkgs) {
            res = tang(ftms, pack, sdm);
            if (normalize) {
                setResultForSubject(res.floatValue() / ((float) ftms.size() * pkgs.size()), pack.getQualName());
            } else {
                float rr = res.floatValue() + 1;
                if(scaleByClassCount){
                    rr = rr * pack.getTopLevelTypes().size();
                }
                setResultForSubject(rr, pack.getQualName());
            }
        }
    }

    private Double tang(Set<TraceModel> ftms, JPackage pack, StaticDependencyModel dm) {
        Set<String> f = new HashSet<String>();
        for (JType t : pack.getTopLevelTypes()) {
            for (TraceModel ftm : ftms) {
                if (ftm.getClass(t.getQualName()) != null) {
                    f.add(ftm.getName());
                }
            }
        }

        if (f.size() > ftms.size()/*|| f.size()<1*/) {
            throw new RuntimeException("Bug calculating f");
        }

        return Math.max(0, (double) f.size() - 1);
    }

    @Override
    public float getResult() {
        if(normalize){
            return getSumVal();
        }else{
            return getMeanVal();
        }
    }
}
