/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.logic;

import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import dk.sdu.mmmi.srcUtils.sdm.model.Util;
import java.util.List;

/**
 * @author ao
 */
@Deprecated
public class TypeAssignement {

    public static String getPkgStr(String pkg, String programName){
        return programName.toLowerCase().replace(" ", "_") + "." + pkg.toLowerCase().replace(" ", "_");
    }

//    public static StaticDependencyModel creatNewPackageStructure(Set<TraceModel> ftms, String programName) {
//        StaticDependencyModel newDm = new StaticDependencyModel();
//        for(TraceModel ftm : ftms){
//            newDm.getOrAddPackageByName(getPkgStr(ftm.getName(), programName));
//        }
//        return newDm;
//    }

//    public static void addCharacteristicTypes(StaticDependencyModel newDm,
//            Set<TraceModel> ftms,
//            Map<JType, String> characteristicTypes, String programName){
//        for(Map.Entry<JType, String> e : characteristicTypes.entrySet()){
//            if(!e.getKey().isTopLevel()){
//                throw new RuntimeException("Attempted to add inner type");
//            }
//            String orgPkg = "";
//            for(TraceModel ftm : ftms){
//                ClassModel t = ftm.getClass(e.getKey().getQualName());
//                if(t!=null){
//                    orgPkg = t.getPackageName();
//                    break;
//                }
//            }
//            Util.deepInsertType(newDm.getOrAddPackageByName(getPkgStr(e.getValue(), programName) + "." + orgPkg), e.getKey());
//        }
//    }

//    @Deprecated
//    public static void clusterGroupTypesByDynamicDeps(StaticDependencyModel newDm,
//            Set<TraceModel> ftms, List<JType> groupTypes, String progName) throws Exception{
//        if(groupTypes.size()>0){
//            List<JType> preserved = new ArrayList<JType>();
//            for(JPackage p : newDm.getPackages()){
//                for(JType t : p.getTopLevelTypes()){
//                    preserved.add(t);
//                }
//            }
//
//            Random r = new Random(new Date().getTime());
//            for(JType gt : groupTypes){
//                JPackage insertPackage = newDm.getPackages().get(r.nextInt(newDm.getPackages().size()));
//                Util.deepInsertType(insertPackage, gt);
//            }
//
//            // backup original static deps
//            Map<String, List<JDependency>> orgStaticDeps = new HashMap<String, List<JDependency>>();
//
//            for(JPackage p : newDm.getPackages()){
//                for(JType t : p.getAllTypes()){
//                    orgStaticDeps.put(t.getQualName(), new ArrayList<JDependency>(t.getDependencies()));
//                    // Clear all static deps
//                    t.getDependencies().clear();
//                }
//            }
//            // change static deps to dynamic deps
////            for(TraceModel ftm : ftms){
////                for(OrderedBinaryRelation<String, Integer> inv : ftm.getInvocations()){
////                    // Dynamic deps are weighted by invocation count
////                    JType caller = null;
////                    if(inv.getFirst()!=null){
////                        caller = newDm.getTypeByNameOrNull(inv.getFirst().getContainingType());
////                    }
////                    JType called = newDm.getTypeByNameOrNull(inv.getSecond().getContainingType());
////                    if(caller != null && called!=null){
////                        caller.getDependencies().add(new JDependency(called));
////                    }
////                }
////            }
//
//            RemodOF of = createOF(newDm, ftms, preserved);
//            GA ga = doCluster(newDm, of, 300, 200);
//            newDm = of.createNewModelFromChromosome(newDm,
//                    ga.getCurrentlyBestChromosomeEntry().getFirst());
//            saveEvolutionStats(ga.getEvolutionStats(), progName + "- Group clustering stats");
//
//            // restore static deps in the model
//            for(Map.Entry<String, List<JDependency>> e : orgStaticDeps.entrySet()){
//                JType t = newDm.getTypeByNameOrNull(e.getKey());
//                t.getDependencies().clear();
//                t.getDependencies().addAll(e.getValue());
//            }
//        }
//    }

    public static void straightDeepInsertTypes(StaticDependencyModel newDm,
            StaticDependencyModel srcDm, List<JType> types) throws Exception{
        
        for(JType t : types){
            JPackage srcPkg = null;
            for(JPackage p : srcDm.getPackages()){
                if(p.getTopLevelTypes().contains(t)){
                    srcPkg = p;
                    break;
                }
            }
            Util.deepInsertType(newDm.getOrAddPackageByName(srcPkg.getQualName()), t);
        }
    }
    
//    public static void clusterNotCoveredTypesByStaticDeps(StaticDependencyModel newDm,
//            Set<TraceModel> ftms, List<JType> notCoveredTypes, String progName) throws Exception{
//        if(notCoveredTypes.size()>0){
//            List<JType> preserved = new ArrayList<JType>();
//            for(JPackage p : newDm.getPackages()){
//                for(JType t : p.getTopLevelTypes()){
//                    preserved.add(t);
//                }
//            }
//
//            Random r2 = new Random(new Date().getTime());
//            for(JType gt : notCoveredTypes){
//                JPackage insertPackage = newDm.getPackages().get(r2.nextInt(newDm.getPackages().size()));
//                Util.deepInsertType(insertPackage, gt);
//            }
//
//            RemodOF of = createOF(newDm, ftms, preserved);
//            GA ga = doCluster(newDm, of, 300, 200);
//            newDm = of.createNewModelFromChromosome(newDm,
//                    ga.getCurrentlyBestChromosomeEntry().getFirst());
//            saveEvolutionStats(ga.getEvolutionStats(), progName + "- Not covered clustering stats");
//        }
//    }

//    public static void reclusterDm(StaticDependencyModel dm,
//            Set<TraceModel> ftms, String progName) throws Exception{
//        List<JType> preserved = new ArrayList<JType>();
//
//        RemodOF of = createOF(dm, ftms, preserved);
//        GA ga = doCluster(dm, of, 300, 100);
//        dm = of.createNewModelFromChromosome(dm,
//                ga.getCurrentlyBestChromosomeEntry().getFirst());
//        saveEvolutionStats(ga.getEvolutionStats(), progName + "- reclustering stats");
//    }

//    public static void addInfrastructuralTypes(StaticDependencyModel newDm,
//            List<JType> infTypes, String programName){
//        String infPkg = getPkgStr("infrastructure", programName);
//        newDm.getOrAddPackageByName(infPkg);
//        for(JType it : infTypes){
//            Util.deepInsertType(newDm.getOrAddPackageByName(infPkg), it);
//        }
//    }

//    public static void saveEvolutionStats(List<List<Double>> stats, String namePrefix) throws Exception{
//        Calendar c = Calendar.getInstance();
//        String dateStamp = c.get(Calendar.YEAR) + "." + c.get(Calendar.MONTH) + "."
//                + c.get(Calendar.DAY_OF_MONTH) + ". " +
//                + c.get(Calendar.HOUR_OF_DAY) + "." + c.get(Calendar.MINUTE);
//
////        RemodOF.CustomXslModifier xMod = new RemodOF.CustomXslModifier(stats);
////        xMod.doAlterSpreadSheet("..\\GAClusterer\\_template.xls", "s1", namePrefix + " " + dateStamp + ".xls");
//    }

//    public static GA doCluster(StaticDependencyModel dm, RemodOF of, int populationSize, int eras) throws IOException, ClassNotFoundException {
//        DecValChromosome prototype = new DecValChromosome(of.getParticipatingTypes().size(), 0, dm.getPackages().size() - 1);
//        OutputUtil.log("Starting GA...");
//        GA ga = new GA(of, populationSize, 0.01, prototype);
//        // Insert some decent candidates to start with
//        DecValChromosome currStr = of.createChromosome(dm);
//        ga.insertClonesIntoPopulation(new Pair<DecValChromosome, Double>(currStr, null), populationSize/3);
//        OutputUtil.log("GA started...");
//        ga.evolve(eras, null);
//        dm = of.createNewModelFromChromosome(dm, ga.getCurrentlyBestChromosomeEntry().getFirst());
//        OutputUtil.log("\n\n*** Fitness of the best chromosome: " + ga.getCurrentlyBestChromosomeEntry().getSecond());
////        OutputUtil.log(dm);
//        return ga;
//    }

//    public static RemodOF createOF(StaticDependencyModel dm, Set<TraceModel> ftms, List<JType> preserved) throws FileNotFoundException, IOException, ClassNotFoundException, ClassNotFoundException{
//        return new FScaAndFTangOF(dm, ftms, preserved);
//    }
}
