/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.logic;

import dk.sdu.mmmi.featureous.remodularization.logic.objectives.RemodularizationOF;
import dk.sdu.mmmi.featureous.core.affinity.Affinity;
import dk.sdu.mmmi.featureous.core.affinity.AffinityProvider;
import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.model.TraceSet;
import dk.sdu.mmmi.featureous.core.ui.OutputUtil;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.ScaTangUtil;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.VirtualScattering;
import dk.sdu.mmmi.featureous.metrics.concernmetrics.VirtualTangling;
import dk.sdu.mmmi.featureous.remodularization.spi.RemodularizationObjectiveProvider;
import dk.sdu.mmmi.srcUtils.sdm.metrics.PCoupImport;
import dk.sdu.mmmi.srcUtils.sdm.metrics.SCoh;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import dk.sdu.mmmi.srcUtils.sdm.model.Util;
import dk.sdu.mmmi.utils.ga.DecValChromosome;
import dk.sdu.mmmi.utils.ga.multiobj.KeyedTuple;
import dk.sdu.mmmi.utils.mogga.MOGGA;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.visual.layout.DevolveWidgetLayout;

/**
 *
 * @author ao
 */
public class MainRemodularization {
    private static Set<KeyedTuple<DecValChromosome, Double>> prevParetoFront = new HashSet<KeyedTuple<DecValChromosome, Double>>();

    public static void resetPrevParetoFront() {
        MainRemodularization.prevParetoFront = new HashSet<KeyedTuple<DecValChromosome, Double>>();
    }

    private static Map<String, String> calcTypeToPkg(Map<String, String> singlTypeToFeature, StaticDependencyModel newDm) {
        Map<String, String> typeToPkg = new HashMap<String, String>();
        for (Entry<String, String> e : singlTypeToFeature.entrySet()) {
            typeToPkg.put(e.getKey(), getPkgStr(e.getValue()));
        }
        for (JPackage p : newDm.getPackages()) {
            for (JType t : p.getTopLevelTypes()) {
                if(!typeToPkg.containsKey(t.getQualName())){
                    typeToPkg.put(t.getQualName(), getPkgStr(p.getQualName()));
                }
            }
        }
        return typeToPkg;
    }

    private static StaticDependencyModel createModel(Map<String, String> typeToPkg, StaticDependencyModel dm) throws RuntimeException {
        StaticDependencyModel finalDm = new StaticDependencyModel();
        for (Map.Entry<String, String> me : typeToPkg.entrySet()) {
            JPackage pkg = finalDm.getOrAddPackageByName(getPkgStr(me.getValue()));
            JType t = dm.getTypeByNameOrNull(me.getKey());
            if (t == null) {
                throw new RuntimeException("Inconsistency when adding a type to new dependency model.");
            }
            Util.deepInsertType(pkg, t);
        }
        return finalDm;
    }

    public static StaticDependencyModel findNewModularization(StaticDependencyModel originalSdm, 
            ProgressHandle progress, Set<RemodularizationObjectiveProvider> providers, 
            boolean factorSingle, AtomicInteger iterations, int population, float mutation) 
            throws IOException, Exception, ClassNotFoundException, RuntimeException {

        Controller c = Controller.getInstance();

        Set<TraceModel> ftms = c.getTraceSet().getFirstLevelTraces();
        checkModelsConsistency(ftms, originalSdm);
        ReportingUtils.showTypeCoverage(ftms, originalSdm);

        progress.start(iterations.get()+2);

        List<String> singleFeature = new ArrayList<String>();
        List<String> multiFeature = new ArrayList<String>();
        for (ClassModel cm : c.getTraceSet().getAllClassIDs()) {
            if (c.getAffinity().getClassAffinity(cm.getName()).equals(Affinity.SINGLE_FEATURE)
                    && originalSdm.getTypeByNameOrNull(cm.getName()).isTopLevel()) {
                singleFeature.add(cm.getName());
            } else if (originalSdm.getTypeByNameOrNull(cm.getName()).isTopLevel()) {
                multiFeature.add(cm.getName());
            }
        }

        OutputUtil.log("Single-feature class count: " + singleFeature.size());
        OutputUtil.log("Multi-feature class count: " + multiFeature.size());

        List<JType> nonCoveredTypes = new ArrayList<JType>();
        for (String t : originalSdm.getTopLevelTypes()) {
            if (!singleFeature.contains(t) && !multiFeature.contains(t)) {
                nonCoveredTypes.add(originalSdm.getTypeByNameOrNull(t));
            }
        }

        OutputUtil.log("Non-covered types: " + nonCoveredTypes.size());

        Map<String, String> singleFeatTypeToFeature = new HashMap<String, String>();
        for (String t : singleFeature) {
            for (TraceModel tm : c.getTraceSet().getFirstLevelTraces()) {
                if (tm.getClasses().containsKey(t)) {
                    singleFeatTypeToFeature.put(t, tm.getName());
                    break;
                }
            }
        }

        OutputUtil.log("Starting GA...");
        List<String> toCluster = new ArrayList<String>();
        toCluster.addAll(multiFeature);
        for (JType t : nonCoveredTypes) {
            toCluster.add(t.getQualName());
        }

        StaticDependencyModel newSdm = null;
        
        if(!factorSingle){
            toCluster.addAll(singleFeatTypeToFeature.keySet());
            singleFeatTypeToFeature.clear();
        }

        if (providers != null && !providers.isEmpty()) {
            RemodularizationOF of = new RemodularizationOF(toCluster, originalSdm, ftms, providers, singleFeatTypeToFeature.keySet());
            DecValChromosome currStr = of.createChromosome(originalSdm);

            MOGGA ga = new MOGGA(of, population, mutation, currStr);
            // Insert some decent candidates to start with
            KeyedTuple<DecValChromosome, Double>[] cl = new KeyedTuple[1];
            cl[0] = new KeyedTuple<DecValChromosome, Double>(currStr, null);
            ga.insertClonesIntoPopulation(cl, 1 + population/50);
            int parCl = 1 + population/50;
            ArrayList<DecValChromosome> pars = new ArrayList<DecValChromosome>();
            for(KeyedTuple<DecValChromosome, Double> kt : prevParetoFront){
                pars.add(kt.getKey());
            }
            for(int i=0;i<parCl;i++){
                Random r = new Random();
                if(pars.size()>0){
                    DecValChromosome chrcl = pars.get(r.nextInt(pars.size()));
                    ga.insertClonesIntoPopulation(
                            new KeyedTuple[]{new KeyedTuple<DecValChromosome, Double>(chrcl, null)}
                            , 1);
                }
            }
            OutputUtil.log("MOGGA started...");

            prevParetoFront = ga.evolve(iterations, progress);
            
//        printParetoFrontStats(paretoFront, of, originalSdm, ftms);
            newSdm = chooseBestChromosome(prevParetoFront, of, of.createChromosome(originalSdm));
        } else {
            newSdm = new StaticDependencyModel();
            
            for (String cc : toCluster) {
                JPackage pkg = Util.getTypesPackage(cc, originalSdm);
                newSdm.getOrAddPackageByName(pkg.getQualName());
                pkg = newSdm.getOrAddPackageByName(pkg.getQualName());
                JType t = originalSdm.getTypeByNameOrNull(cc);

                Util.deepInsertType(pkg, t);
            }
        }

        // Create newDm
        Map<String, String> typeToPkg = calcTypeToPkg(singleFeatTypeToFeature, newSdm);
//        Map<String, String> typeToPkg = calcTypeToPkg(new HashMap<String, String>(), newSdm);
        StaticDependencyModel finalDm = createModel(typeToPkg, originalSdm);

//        TypeAssignement.straightDeepInsertTypes(finalDm, dm, notCoveredTypes);

        if (!checkConsistency(originalSdm, finalDm)) {
            OutputUtil.log("ERROR: New static model inconsistent with the old one!");
            throw new RuntimeException("Consistency exception.");
        }
        ReportingUtils.showComparison(originalSdm, finalDm, ftms);
//        printStatsPrePost(originalSdm, ftms, finalDm);

        progress.finish();
        return finalDm;
    }

    private static void printStatsPrePost(StaticDependencyModel preDm, Set<TraceModel> ftmsPre, StaticDependencyModel postDm) throws IOException {
        OutputUtil.log("-----Feature stattering [pre, post]:");

        List<JPackage> packs = new ArrayList<JPackage>(preDm.getPackages());
        packs.removeAll(ScaTangUtil.getInsulatedPackages(ftmsPre, preDm));
        int insPkgDm = packs.size();
        packs = new ArrayList<JPackage>(postDm.getPackages());
        packs.removeAll(ScaTangUtil.getInsulatedPackages(ftmsPre, postDm));
        int insPkgFinalDm = packs.size();

        for (TraceModel tm : ftmsPre) {
            float pre = new VirtualScattering().calculateAndReturnFor(ftmsPre, preDm, tm.getName());
            float post = new VirtualScattering().calculateAndReturnFor(ftmsPre, postDm, tm.getName());
            OutputUtil.log("" + tm.getName() + ", " + pre + ", " + post + ", ");
        }
        OutputUtil.log("-----Package tangling pre:");
        for (JPackage p : preDm.getPackages()) {
            float pre = new VirtualTangling().calculateAndReturnFor(ftmsPre, preDm, p.getQualName());
            OutputUtil.log("" + p.getQualName() + ", " + pre + ", ");
        }

        OutputUtil.log("------Package tangling post:");
        for (JPackage p : postDm.getPackages()) {
            float post = new VirtualScattering().calculateAndReturnFor(ftmsPre, postDm, p.getQualName());
            OutputUtil.log("" + p.getQualName() + ", " + post + ", ");
        }
        OutputUtil.log(".");
        ReportingUtils.reportPackageDistribution(preDm, ftmsPre, "pre");
        OutputUtil.log(".");
        ReportingUtils.reportPackageDistribution(postDm, ftmsPre, "post");
        OutputUtil.log(".");
    }

    private static StaticDependencyModel chooseBestChromosome(Set<KeyedTuple<DecValChromosome, Double>> paretoFront, RemodularizationOF of, DecValChromosome org) {
        // Sort along each dimension separately, rank chromosomes
        // Choose the chromosome with lowest total rank
        Double[] cutoffPoints = of.evaluateChromosomeFitness(org);
        Set<KeyedTuple<DecValChromosome, Double>> toRemove = new HashSet<KeyedTuple<DecValChromosome, Double>>();
        for(KeyedTuple<DecValChromosome, Double> p : paretoFront){
            for(int i=0;i<cutoffPoints.length;i++){
                if(p.getValues()[i]<cutoffPoints[i]){
                    toRemove.add(p);
                    break;
                }
            }
        }
        
        paretoFront.removeAll(toRemove);
        
        int objectiveCount = paretoFront.iterator().next().getValues().length;
        Map<Integer, Map<DecValChromosome, Double>> ranks = new HashMap<Integer, Map<DecValChromosome, Double>>();
        for (int i = 0; i < objectiveCount; i++) {
            Map<DecValChromosome, Double> ranking = sortAlongNthObjective(paretoFront, i);
            ranks.put(i, ranking);
        }

        final Map<DecValChromosome, Double> combinedRank = new HashMap<DecValChromosome, Double>();

        for (KeyedTuple<DecValChromosome, Double> r : paretoFront) {
            double comb = 0;
            for (int i = 0; i < objectiveCount; i++) {
                Double rank = ranks.get(i).get(r.getKey());
                comb += rank*rank;
            }
            combinedRank.put(r.getKey(), comb);
        }

        List<DecValChromosome> ress = new ArrayList<DecValChromosome>();
        ress.addAll(combinedRank.keySet());

        Collections.sort(ress, new Comparator<DecValChromosome>() {

            @Override
            public int compare(DecValChromosome o1, DecValChromosome o2) {
                return combinedRank.get(o1).compareTo(combinedRank.get(o2));
            }
        });

        //Need to take the minimal combined rank
        StaticDependencyModel newDm = of.createNewModelFromChromosome(ress.get(0));
        
        return newDm;
    }

    private static Map<DecValChromosome, Double> sortAlongNthObjective(Set<KeyedTuple<DecValChromosome, Double>> paretoFront, int objectiveNr) {
        final Map<DecValChromosome, Double> results = new HashMap<DecValChromosome, Double>();
        for (KeyedTuple<DecValChromosome, Double> r : paretoFront) {
            results.put(r.getKey(), r.getValues()[objectiveNr]);
        }
        List<DecValChromosome> res = new ArrayList<DecValChromosome>();
        res.addAll(results.keySet());

        Collections.sort(res, new Comparator<DecValChromosome>() {

            @Override
            public int compare(DecValChromosome o1, DecValChromosome o2) {
                return results.get(o2).compareTo(results.get(o1));
            }
        });
        
        Map<DecValChromosome, Double> ress = new HashMap<DecValChromosome, Double>();
        
        int place = 0;
        double prevVal = -123.123;
        
        for(DecValChromosome chr : res){
            double r = results.get(chr);
            if(prevVal!=r){
                place++;
                prevVal = r;
            }
            ress.put(chr, (double)place);
        }
        
//        double max = results.get(res.get(0));
//        double min = results.get(res.get(res.size()-1));
//        
//        if(max<min){
//            throw new RuntimeException("Bug in min max");
//            }
//            
//        for(DecValChromosome chr : results.keySet()){
//            double norm = max-min;
//            if(norm==0){
//                norm =1;
//        }
//            ress.put(chr, 1-(Math.abs(results.get(chr)-min)/norm));
//        }
        
        // Best with lowest results
        return ress;
    }

    private static void printParetoFrontStats(Set<KeyedTuple<DecValChromosome, Double>> paretoFront, RemodularizationOF of, StaticDependencyModel dm, Set<TraceModel> ftms) throws RuntimeException {
        OutputUtil.log("------------Pareto front's params[Coh, Coup, Sca, Tang, pkgC]:-----------");
        for (KeyedTuple<DecValChromosome, Double> pc : paretoFront) {
//            StaticDependencyModel pDm = of.createNewModelFromChromosome(pc.getKey());
            Map<String, String> typeToPkg = null; //pDm
            StaticDependencyModel newDm = Util.createRefactoredModel(typeToPkg, dm);
            VirtualScattering vs = new VirtualScattering();
            vs.calculateAll(ftms, newDm);
            Float sca = vs.getResult();
            float tang = new VirtualTangling().calculateAndReturnRes(ftms, newDm);
            Double coh = new SCoh().calculate(newDm, null);
            Double coup = 0d;
            for (JPackage p : newDm.getPackages()) {
                Double res = new PCoupImport().calculate(newDm, p);
                if (res != null) {
                    coup += res;
                }
            }
            int pkgC = newDm.getPackages().size();
            OutputUtil.log("" + coh + ", " + coup + ", " + sca + ", " + tang + ", " + pkgC + ", ");
        }
    }

    private static Map<String, String> getTypeToFeatureMap(Set<String> types, Set<TraceModel> topTraces) {
        Map<String, String> typeToFeature = new HashMap<String, String>();
        for (String t : types) {
            for (TraceModel tm : topTraces) {
                if (tm.getClasses().containsKey(t)) {
                    typeToFeature.put(t, tm.getName());
                    break;
                }
            }
        }
        return typeToFeature;
    }

    private static List<String> getSingleFeatureClasses(StaticDependencyModel dm, TraceSet traceSet, AffinityProvider afp) {
        List<String> singleFeature = new ArrayList<String>();
        for (ClassModel cm : traceSet.getAllClassIDs()) {
            if (afp.getClassAffinity(cm.getName()).equals(Affinity.SINGLE_FEATURE)
                    && dm.getTypeByNameOrNull(cm.getName()).isTopLevel()) {
                singleFeature.add(cm.getName());
            }
        }
        return singleFeature;
    }

    private static Set<JType> getReferencedTopTypes(Set<TraceModel> ftms, StaticDependencyModel dm) {
        Set<JType> referencedTopTypes = new HashSet<JType>();
        for (TraceModel ftm : ftms) {
            for (ClassModel tt : ftm.getClassSet()) {
                JType m = dm.getTypeByNameOrNull(tt.getName());
                if (m != null && !referencedTopTypes.contains(m)) {
                    referencedTopTypes.addAll(Util.getReferencedTopLevelTypes(m, new HashSet<JType>()));
                }
            }
        }
        return referencedTopTypes;
    }

    private static void checkModelsConsistency(Set<TraceModel> ftms, StaticDependencyModel dm) throws RuntimeException {

        for (TraceModel ftm : ftms) {
            for (ClassModel t : ftm.getClassSet()) {
                if (dm.getTypeByNameOrNull(t.getName()) == null) {
                    String msg = "Inconsistency between feature and static models for: "
                            + t.getName() + ftm.getName();
                    OutputUtil.log(msg);
                    throw new RuntimeException(msg);
                }
            }
        }
    }

    public static boolean checkConsistency(StaticDependencyModel dm1, StaticDependencyModel dm2) {

        if (dm1.getTopLevelTypesCount() != dm2.getTopLevelTypesCount()) {
            throw new RuntimeException("Bad topLevel type count after remodularization[dm, newDm]: "
                    + dm1.getTopLevelTypesCount() + ", " + dm2.getTopLevelTypesCount());
        }
        if (dm1.getAllTypesCount() != dm2.getAllTypesCount()) {
            throw new RuntimeException("Bad all type count after remodularization[dm, newDm]: "
                    + dm1.getAllTypesCount() + ", " + dm2.getAllTypesCount());
        }

        if (dm1.getAllTypesCount() != dm2.getAllTypesCount()) {
            return false;
        }

        for (JPackage p : dm1.getPackages()) {
            for (JType t : p.getAllTypes()) {
                JType t2 = dm2.getTypeByNameOrNull(t.getQualName());
                if (t2 == null) {
                    return false;
                }
                if (t.getMethodCount() != t2.getMethodCount()) {
                    return false;
                }
            }
        }

        return true;
    }

    private static String getPkgStr(String string) {
        return string.replaceAll(" ", "_");
    }
}
