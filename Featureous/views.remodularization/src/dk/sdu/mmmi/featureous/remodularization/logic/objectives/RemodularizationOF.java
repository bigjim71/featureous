/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.logic.objectives;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.remodularization.spi.RemodularizationObjectiveProvider;
import dk.sdu.mmmi.srcUtils.sdm.model.HashList;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import dk.sdu.mmmi.srcUtils.sdm.model.Util;
import dk.sdu.mmmi.utils.ga.DecValChromosome;
import dk.sdu.mmmi.utils.ga.multiobj.ParetoObjectiveFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author ao
 */
public class RemodularizationOF implements ParetoObjectiveFunction{

    private final StaticDependencyModel sdm;
    private final Set<TraceModel> traces;
    private final List<String> subjectTypes;
    private final List<RemodularizationObjectiveProvider> objectiveProviders;

    public RemodularizationOF(List<String> subjectTypes, StaticDependencyModel orgModel, Set<TraceModel> traces, Set<RemodularizationObjectiveProvider> objectiveProviders) {
        this.sdm = orgModel;
        this.traces = traces;
        this.subjectTypes = subjectTypes;
        this.objectiveProviders = new ArrayList<RemodularizationObjectiveProvider>(objectiveProviders);
    }

    public Double[] evaluateChromosomeFitness(DecValChromosome h) {
        //TODO: make it use the providers
        StaticDependencyModel dm = createNewModelFromChromosome(h);
        Double[] res = new Double[objectiveProviders.size()];
        for(int i = 0; i<objectiveProviders.size(); i++){
            res[i] = new Double(objectiveProviders.get(i).createObjective().calculateAndReturnRes(traces, dm));
            if(objectiveProviders.get(i).isMinimization()){
                res[i] = -res[i];
            }
        }
        
        return res;
        
//        Double sCoh = new SCoh().calculate(dm, null);
//        Double sCoupAbs = 0d;
//        for(JPackage p : dm.getPackages()){
//            Double pCoupAbs = new PCoupImport().calculate(dm, p);
//            if(pCoupAbs!=null){
//                sCoupAbs += pCoupAbs;
//            }
//        }
//            // Normalize coupling ??
//    //        sCoupAbs = sCoupNormL/sCoupNormM;
//        Double fsca = 0d;
//        for(TraceModel ftm : traces){
//            Set<JPackage> a = new HashSet<JPackage>();
//            for (ClassModel t : ftm.getClassSet()) {
//                JPackage p = Util.getTypesPackage(t.getName(), dm);
//                if (p != null) {
//                    a.add(p);
//                }
//            }
//            fsca += a.size();
//        }
//        fsca = fsca/traces.size();
//        Double ftang = 0d;
//        for(JPackage pp : dm.getPackages()){
//            List<TraceModel> f = new ArrayList<TraceModel>();
//            for(TraceModel ftm : traces){
//                for (ClassModel t : ftm.getClassSet()) {
//                    JPackage p = Util.getTypesPackage(t.getName(), dm);
//                    if (p != null && p.getQualName().equals(pp.getQualName())
//                            && !f.contains(ftm)) {
//                        f.add(ftm);
//                    }
//                }
//            }
//            ftang += f.size();
//        }
//        ftang = ftang / dm.getPackages().size();
//        Double[] res = new Double[4];
//        res[0] = sCoh;
//        res[1] = -sCoupAbs;
//        res[2] = -fsca;
//        res[3] = -ftang;
//        
//        return res;
    }

    public DecValChromosome createChromosome(StaticDependencyModel dm) {
        DecValChromosome chr = new DecValChromosome(subjectTypes.size(), 0, dm.getPackages().size()-1);
        for(int i = 0; i<subjectTypes.size();i++){
            for(JPackage p : dm.getPackages()){
                if(p.getTypeByQualNameOrNull(subjectTypes.get(i))!=null){
                    chr.setGeneVal(i, dm.getPackages().indexOf(p));
                }
            }
        }

        return chr;
    }

    public StaticDependencyModel createNewModelFromChromosome(DecValChromosome chr) {
        StaticDependencyModel newDm = new StaticDependencyModel();
//        for(JPackage p : sdm.getPackages()){
//            newDm.getOrAddPackageByName(p.getQualName());
//        }

        HashList<JType> added = new HashList<JType>();
        for(int i = 0; i<chr.getLength(); i++){
            int p = chr.getGeneVal(i);
            JType type = sdm.getTypeByNameOrNull(subjectTypes.get(i));
            if(type==null){
                throw new RuntimeException("Null");
            }
            
            Util.deepInsertType(newDm.getOrAddPackageByName("pack"+p), type, added);
            
//            Util.deepInsertType(newDm.getOrAddPackageByName(sdm.getPackages().get(p).getQualName()), type, added);
        }

//        for(JPackage p : sdm.getPackages()){
//            for(JType t : p.getTopLevelTypes()){
//                if(!added.contains(t)){
//                    Util.deepInsertType(newDm.getOrAddPackageByName(p.getQualName()), t);
////                    newDm.getOrAddPackageByName(p.getQualName()).getOrAddType(t);
//                }
//            }
//        }

        return newDm;
    }

}
