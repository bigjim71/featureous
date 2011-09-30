/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.OrderedBinaryRelation;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.model.TraceSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openide.windows.IOProvider;
import org.openide.windows.OutputWriter;

/**
 *
 * @author aolszak
 */
public class StaticDynamicComparison {

    public static void printFCA() {
        OutputWriter ow = IOProvider.getDefault().getStdOut();
        TraceSet tc = Controller.getInstance().getTraceSet();
        List<TraceModel> tms = new ArrayList<TraceModel>();
        tms.addAll(tc.getFirstLevelTraces());

        //For classes
        ow.println("[Class-feature]:");
        ow.print(";");
        for (TraceModel tm : tms) {
            ow.print(tm.getName() + ";");
        }
        ow.println();
        for (ClassModel cid : tc.getAllClassIDs()) {
            ow.print(cid.getName() + ";");
            for (TraceModel tm : tms) {
                ow.print((tm.hasClass(cid.getName())) ? "1;" : "0;");
            }
            ow.println();
        }

        //For packages
        ow.println("[Package-feature]:");
        ow.print(";");
        for (TraceModel tm : tms) {
            ow.print(tm.getName() + ";");
        }
        ow.println();
        List<String> pkgs = new ArrayList<String>();
        for (ClassModel cid : tc.getAllClassIDs()) {
            if (!pkgs.contains(cid.getPackageName())) {
                pkgs.add(cid.getPackageName());
            }
        }
        for (String pid : pkgs) {
            ow.print(pid + ";");
            for (TraceModel tm : tms) {
                boolean corr = false;
                for (ClassModel cm : tm.getClassSet()) {
                    if (pid.equals(cm.getPackageName())) {
                        corr = true;
                        break;
                    }
                }
                ow.print((corr) ? "1;" : "0;");
            }
            ow.println();
        }
    }

    public static void printFCAInstances() {
        OutputWriter ow = IOProvider.getDefault().getStdOut();
        TraceSet tc = Controller.getInstance().getTraceSet();
        List<TraceModel> tms = new ArrayList<TraceModel>();
        tms.addAll(tc.getFirstLevelTraces());

        //For classes
        ow.println("[Instance-feature]:");
        ow.print(";");
        for (TraceModel tm : tms) {
            ow.print(tm.getName() + ";");
        }
        ow.println();
        Set<String> instances = new HashSet<String>();
        for (ClassModel cid : tc.getAllClassIDs()) {
            instances.addAll(cid.getInstancesUsed());
        }

        for (String ins : instances) {
            ow.print(ins+";");
            for(TraceModel tm : tms){
                boolean hasIns = false;
                for(ClassModel cm : tm.getClassSet()){
                    if(cm.getInstancesUsed().contains(ins)){
                        hasIns = true;
                        break;
                    }
                }
                
                ow.print(hasIns?"1;":"0;");
            }
            ow.println();
        }
    }

    public static void exportVeggieClassCalls() {
        OutputWriter ow = IOProvider.getDefault().getStdOut();
        TraceSet tc = Controller.getInstance().getTraceSet();
        
        ow.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!-- SGG Graph Data --><graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">"
                + "<graph edgedefault=\"undirected\" xmlns=\"http://viscomp.utdallas.edu/VEGGIE\">");
        
        for(TraceModel tm : tc.getFirstLevelTraces()){
            //Nodes
            for(ClassModel cm : tm.getClassSet()){
                int id = getMethodID(tm.getName(), cm.getName());
                ow.println("<node id=\"" + id +
                        "\" type=\""+cm.getName()+"\" pos=\"239 117\"> <port id=\"{"+cm.getName()+"}\"/>"
                        + "<data key=\"attrib\"><attrib id=\"Terminal\" type=\"2\" bool=\"true\"/>"
                        + "</data> </node>");
            }
                
            //Calls
            for(OrderedBinaryRelation<String,Integer> rel : tm.getInterTypeInvocations()){
                ow.println("<edge type=\"call\" directed=\"true\" source=\""
                        +getMethodID(tm.getName(), rel.getFirst())+"\" "
                        + "target=\""
                        +getMethodID(tm.getName(), rel.getSecond())+"\" sourceport=\"{"
                        +rel.getFirst()+"}\" targetport=\"{"+rel.getSecond()+"}\"/>");
            }
        }
        ow.println("  </graph> </graphml>");
    }
    
    private static int getMethodID(String feature, String method){
        return (feature + method).hashCode();
    }
    
    public static void callComp() {
        int missedDyn = 0, statForks = 0;
        OutputWriter ow = IOProvider.getDefault().getStdOut();
        TraceSet tc = Controller.getInstance().getTraceSet();

        Map<TraceModel, TraceModel> dynToSta = getDynToStaticMap(tc);

        for (Map.Entry<TraceModel, TraceModel> e : dynToSta.entrySet()) {
            TraceModel dyn = e.getKey();
            TraceModel sta = e.getValue();
            if (sta == null) {
                continue;
            }
            Map<String, Set<String>> dynCalls = getCallMap(dyn);
            Map<String, Set<String>> staCalls = getCallMap(sta);

            ow.println("\n*** Feature: " + dyn.getName());
            for (String dynM : dynCalls.keySet()) {
                Set<String> dynC = dynCalls.get(dynM);
                Set<String> staC = staCalls.get(dynM);
                if (dynC == null || staC == null) {
                    continue;
                }

                Set<String> tmp = new HashSet<String>(dynC);
                tmp.removeAll(staC);
                if (!tmp.isEmpty()) {
                    ow.println("        " + tmp.size() + "[" + dynM + "]: dynamic calls not found by static analysis: " + tmp);
                    missedDyn += tmp.size();
                }

                Set<String> tmp2 = new HashSet<String>(staC);
                tmp2.removeAll(dynC);
                if (!tmp2.isEmpty()) {
                    ow.println(tmp2.size() + "-->[" + dynM + "]: additional static forking calls: " + tmp2);
                    statForks += tmp2.size();
                }
            }
        }
        ow.println("DONE, missedDyn=" + missedDyn + ", staticForks=" + statForks);
    }

    private static Map<TraceModel, TraceModel> getDynToStaticMap(TraceSet tc) {
        Map<TraceModel, TraceModel> dynToSta = new HashMap<TraceModel, TraceModel>();
        for (TraceModel tm : tc.getAllTraces()) {
            if (!tm.getName().endsWith("[s]")) {
                dynToSta.put(tm, tc.getFirstLevelTraceByName(tm.getName() + "[s]"));
            }
        }
        return dynToSta;
    }

    private static Map<String, Set<String>> getCallMap(TraceModel dyn) {
        Map<String, Set<String>> dynCalls = new HashMap<String, Set<String>>();
        for (OrderedBinaryRelation<String, Integer> i : dyn.getMethodInvocations()) {

            Set<String> calls = dynCalls.get(i.getFirst());
            if (calls == null) {
                calls = new HashSet<String>();
            }
            calls.add(i.getSecond());
            dynCalls.put(i.getFirst(), calls);
        }
        return dynCalls;
    }

    static void printUnitCorrelation() {
        Set<String> dynP = new HashSet<String>();
        Set<String> staP = new HashSet<String>();
        Set<String> dynC = new HashSet<String>();
        Set<String> staC = new HashSet<String>();
        Set<String> dynM = new HashSet<String>();
        Set<String> staM = new HashSet<String>();
        OutputWriter ow = IOProvider.getDefault().getStdOut();
        Map<TraceModel, TraceModel> dynToSta = getDynToStaticMap(Controller.getInstance().getTraceSet());
        ow.println("feature, commonP, commonC, commonM, onlyStaticP, onlyStaticC, onlyStaticM, onlyDynP, onlyDynC, onlyDynM,");
        int totalOnlyDynM = 0, totalOnyStatM = 0, totalCommonM = 0;
        for (Map.Entry<TraceModel, TraceModel> e : dynToSta.entrySet()) {
            TraceModel dyn = e.getKey();
            TraceModel sta = e.getValue();
            if (dyn == null || sta == null) {
                continue;
            }

            for (ClassModel dynCm : dyn.getClassSet()) {
                dynP.add(dynCm.getPackageName());
                dynC.add(dynCm.getName());
                dynM.addAll(dynCm.getAllMethods());
            }

            for (ClassModel staCm : sta.getClassSet()) {
                staP.add(staCm.getPackageName());
                staC.add(staCm.getName());
                staM.addAll(staCm.getAllMethods());
            }

            Set<String> commonP = new HashSet<String>(dynP);
            Set<String> commonC = new HashSet<String>(dynC);
            Set<String> commonM = new HashSet<String>(dynM);
            for (String s : dynP) {
                if (!staP.contains(s)) {
                    commonP.remove(s);
                }
            }
            for (String s : dynC) {
                if (!staC.contains(s)) {
                    commonC.remove(s);
                }
            }
            for (String s : dynM) {
                if (!staM.contains(s)) {
                    commonM.remove(s);
                }
            }

            totalOnyStatM += staM.size() - commonM.size();
            totalOnlyDynM += dynM.size() - commonM.size();
            totalCommonM += commonM.size();

            ow.println(dyn.getName() + ", " + commonP.size() + ", " + commonC.size() + ", " + commonM.size()
                    + ", " + (staP.size() - commonP.size()) + ", " + (staC.size() - commonC.size()) + ", " + (staM.size() - commonM.size())
                    + ", " + (dynP.size() - commonP.size()) + ", " + (dynC.size() - commonC.size()) + ", " + (dynM.size() - commonM.size()) + ",");
        }
        ow.println("\n[totalOnlyStaM, totalOnlyDynM, totalCommonM]: " + totalOnyStatM + ", " + totalOnlyDynM + ", " + totalCommonM);
    }
}
