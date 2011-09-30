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
public class Tangling extends AbstractMetric {

    private final boolean pkgScope;

    public Tangling(boolean pkgScope) {
        super("Tangling in " + ((pkgScope) ? "pkgs" : "class"),
                AbstractMetric.Scope.getPkgOrClass(pkgScope));
        this.pkgScope = pkgScope;
    }

    @Override
    public float getResult() {
        return getMeanVal();
    }

    @Override
    public void calculateAll(Set<TraceModel> tms, StaticDependencyModel sdm) {
        for (Result res : calculate(new ArrayList<TraceModel>(tms), pkgScope)) {
            setResultForSubject(res.value.floatValue(), res.name);
        }
    }

    private static List<Result> calculate(List<TraceModel> traces, boolean pkgScope) {
        Collections.sort(traces);
        Set<String> allElementsSet = (pkgScope) ? ScaTangUtil.getNonInsulatedPackages(new HashSet<TraceModel>(traces))
                : ScaTangUtil.getNonInsulatedClasses(new HashSet<TraceModel>(traces));

        if (traces.size() == 0 || allElementsSet.size() == 0) {
            return new ArrayList<Result>();
        }
        List<String> allElements = new ArrayList<String>(allElementsSet);
        Collections.sort(allElements);
        List<Result> ress = new ArrayList<Result>();
        for (String elem : allElements) {
            double res = tang(traces, elem, pkgScope) / ((double) traces.size() * allElements.size());
            ress.add(new Result((float) res, elem));
        }

        return ress;
    }

    private static double tang(List<TraceModel> traces, String elem, boolean pkgScope) {
        Set<String> featsUsingElem = new HashSet<String>();
        for (TraceModel traceModel : traces) {
            for (ClassModel cm : traceModel.getClassSet()) {
                if (featsUsingElem.contains(traceModel.getName())) {
                    break;
                }
                if (pkgScope) {
                    if (cm.getPackageName() != null && cm.getPackageName().equals(elem)) {
                        featsUsingElem.add(traceModel.getName());
                    }
                } else {
                    if (cm.getName() != null && cm.getName().equals(elem)) {
                        featsUsingElem.add(traceModel.getName());
                    }
                }
            }
        }
        return featsUsingElem.size();
    }
}
