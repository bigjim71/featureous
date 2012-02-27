package dk.sdu.mmmi.featureous.remodularization;

import dk.sdu.mmmi.featureous.core.affinity.Affinity;
import dk.sdu.mmmi.featureous.core.affinity.AffinityProvider;
import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ao
 */
public class PkgRenamer {

    public static final float JACCARD_SIM_TOLERANCE = 0.66f;

    public static void renamePackages(StaticDependencyModel sdm, StaticDependencyModel orgSdm) {
        Map<String, Integer> createdPkgs = new HashMap<String, Integer>();

        for (JPackage pkg : sdm.getPackages()) {
            addPackage(pkg.getQualName(), createdPkgs);
        }
        
        nameSimilar(sdm, orgSdm, createdPkgs);
        
        sdm.syncPkgNames();
        
        nameFeatureSpecific(sdm, createdPkgs);
        
        sdm.syncPkgNames();
    }

    private static void nameSimilar(StaticDependencyModel sdm, StaticDependencyModel orgSdm, Map<String, Integer> createdPkgs) {
        // Find similar original packages
        for (JPackage pkg : sdm.getPackages()) {
            List<JType> types = new ArrayList<JType>(pkg.getTopLevelTypes());
            float bestJaccard = 0f;
            String bestJaccardName = null;
            for (JPackage orgPkg : orgSdm.getPackages()) {
                List<JType> orgTypes = new ArrayList<JType>(orgPkg.getTopLevelTypes());

                List<JType> typesMinusOrg = new ArrayList<JType>(types);
                typesMinusOrg.removeAll(orgTypes);

                List<JType> orgMinusType = new ArrayList<JType>(orgTypes);
                orgMinusType.removeAll(types);

                float jaccardSim = types.size() - typesMinusOrg.size();
                jaccardSim = jaccardSim / (types.size() + orgMinusType.size());

                if (jaccardSim > bestJaccard) {
                    bestJaccard = jaccardSim;
                    bestJaccardName = orgPkg.getQualName();
                }
            }

            if (bestJaccard > JACCARD_SIM_TOLERANCE) {
                String newName = getName(bestJaccardName, createdPkgs);
                removePackage(pkg.getQualName(), createdPkgs);
                addPackage(bestJaccardName, createdPkgs);
                pkg.setQualName(newName);
            }
        }
    }

    private static void nameFeatureSpecific(StaticDependencyModel sdm, Map<String, Integer> createdPkgs) {
        // Find feature-specific packages
        AffinityProvider ap = Controller.getInstance().getAffinity();
        for (JPackage pkg : sdm.getPackages()) {
            boolean fs = true;
            String fName = null;
            for (JType t : pkg.getAllTypes()) {
                if (ap.getClassAffinity(t.getQualName()) != null
                        && !ap.getClassAffinity(t.getQualName()).equals(Affinity.SINGLE_FEATURE)) {
                    fs = false;
                    break;
                }
                if (ap.getClassAffinity(t.getQualName()) != null
                        && ap.getClassAffinity(t.getQualName()).equals(Affinity.SINGLE_FEATURE)) {
                    for (TraceModel tm : Controller.getInstance().getTraceSet().getFirstLevelTraces()) {
                        if(tm.getClass(t.getQualName())!=null){
                            fName = tm.getName();
                            break;
                        }
                    }
                }
            }

            if (fs && fName!=null) {
                String newName = getName(fName, createdPkgs);
                removePackage(pkg.getQualName(), createdPkgs);
                addPackage(fName, createdPkgs);
                pkg.setQualName(newName);
            }
        }
    }

    private static String getName(String protoName, Map<String, Integer> map) {
        Integer c = map.get(protoName);
        if (c == null || c == 0) {
            return protoName;
        } else {
            c++;
            return protoName + c;
        }
    }

    private static void addPackage(String pkg, Map<String, Integer> map) {
        Integer count = map.get(pkg);
        if (count == null) {
            count = 0;
        }
        count++;
        map.put(pkg, count);
    }

    private static void removePackage(String pkg, Map<String, Integer> map) {
        Integer count = map.get(pkg);
        if (count != null && count > 0) {
            count--;
            map.put(pkg, count);
        }
    }
}
