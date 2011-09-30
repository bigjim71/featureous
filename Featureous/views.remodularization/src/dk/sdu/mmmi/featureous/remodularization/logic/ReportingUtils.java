/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.logic;

import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.ui.OutputUtil;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.ScaTangUtil;
import dk.sdu.mmmi.featureous.remodularization.metrics.VirtualScattering;
import dk.sdu.mmmi.featureous.remodularization.metrics.VirtualTangling;
import dk.sdu.mmmi.srcUtils.sdm.metrics.PCoupImport;
import dk.sdu.mmmi.srcUtils.sdm.metrics.SCoh;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ao
 */
public class ReportingUtils {

    public static void showComparison(StaticDependencyModel dm,
            StaticDependencyModel newDm, Set<TraceModel> ftms) {

        OutputUtil.log("FSca: " + new VirtualScattering().calculateAndReturnRes(ftms, dm) 
                + " -> " + new VirtualScattering().calculateAndReturnRes(ftms, newDm));
        OutputUtil.log("FTang: " + new VirtualTangling().calculateAndReturnRes(ftms, dm) 
                + " -> " + new VirtualTangling().calculateAndReturnRes(ftms, newDm));
        OutputUtil.log("SCohesion: " + new SCoh().calculate(dm, null) + " -> " + new SCoh().calculate(newDm, null));
        Double sCoup1 = 0d;
        for (JPackage p : dm.getPackages()) {
            Double res = new PCoupImport().calculate(dm, p);
            if (res != null) {
                sCoup1 += res;
            }
        }
        Double sCoup2 = 0d;
        for (JPackage p : newDm.getPackages()) {
            Double res = new PCoupImport().calculate(newDm, p);
            if (res != null) {
                sCoup2 += res;
            }
        }
        OutputUtil.log("SCoupling: " + sCoup1 + " -> " + sCoup2);
        OutputUtil.log("Packages: " + dm.getPackages().size() + " -> " + newDm.getPackages().size());
    }

    public static void showTypeCoverage(Set<TraceModel> ftms, StaticDependencyModel dm) {
        Set<ClassModel> ftTypes = new HashSet<ClassModel>();
        for (TraceModel ftm : ftms) {
            for(ClassModel t : ftm.getClassSet()){
                JType tt = dm.getTypeByNameOrNull(t.getName());
                if(tt!=null && tt.isTopLevel()){
                    ftTypes.add(t);
                }
            }
        }

        Set<JType> allTopTypes = new HashSet<JType>();
        for(JPackage p : dm.getPackages()){
            allTopTypes.addAll(p.getTopLevelTypes());
        }

        OutputUtil.log("\nFeature traces cover " + ftTypes.size() + " out of " + allTopTypes.size() + " top-level program types.");
    }

    public static void reportPackageDistribution(StaticDependencyModel dm, Set<TraceModel> ftms, String prefix) throws IOException{
        List<String> ids = new ArrayList<String>();
        List<Float> resTang = new ArrayList<Float>();
        List<Float> resSca = new ArrayList<Float>();
        List<JPackage> notInsPkgs = new ArrayList<JPackage>(dm.getPackages());
        notInsPkgs.removeAll(ScaTangUtil.getInsulatedPackages(ftms, dm));
        List<TraceModel> notInsFtms = new ArrayList<TraceModel>(ftms);
        notInsFtms.removeAll(ScaTangUtil.getInsulatedFeatures(ftms));
        for(JPackage p : notInsPkgs){
            Float res = new VirtualTangling().calculateAndReturnFor(ftms, dm, p.getQualName());
            if(res!=null){
                res = res/((float)ftms.size()*notInsPkgs.size());
            }
            ids.add(p.getQualName());
            resTang.add(res);
            resSca.add(null);
        }
        for(TraceModel ftm : notInsFtms){
            Float res = new VirtualScattering().calculateAndReturnFor(new HashSet<TraceModel>(notInsFtms), dm, ftm.getName());
            ids.add(ftm.getName());
            resTang.add(null);
            resSca.add(res);
        }

        Calendar c = Calendar.getInstance();
        String dateStamp = c.get(Calendar.YEAR) + "." + c.get(Calendar.MONTH) + "."
                + c.get(Calendar.DAY_OF_MONTH) + ". " +
                + c.get(Calendar.HOUR_OF_DAY) + "." + c.get(Calendar.MINUTE);

//        CustomXlsModifier xMod = new CustomXlsModifier(ids, resTang, resSca, resSca);
//        xMod.doAlterSpreadSheet("..\\GAClusterer\\_template.xls", "s1", "PackageDistribution " + prefix + " " + dateStamp + ".xls");
    }
}
