package dk.sdu.mmmi.featureous.remodularization;

import dk.sdu.mmmi.featureous.core.affinity.Affinity;
import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.ui.OutputUtil;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ao
 */
public class CleanSplitSimulation {
    public static void cleanSplitCurrentTraces(){
        int cleanSplitCounter = 0;
        Controller c = Controller.getInstance();
        Set<TraceModel> tms = c.getTraceSet().getFirstLevelTraces();
        for(TraceModel tm : tms){
            Set<ClassModel> toRemove = new HashSet<ClassModel>();
            for(ClassModel cm : tm.getClassSet()){
                if(isUsedByOtherFeats(cm, tms)){
                    Set<String> mToRemove = new HashSet<String>();
                    for(String mm : cm.getAllMethods()){
                        if(c.getAffinity().getMethodAffinity(mm).equals(Affinity.SINGLE_FEATURE)){
                            mToRemove.add(mm);
                        }
                    }
                    for(String m : mToRemove){
                        cm.removeMethod(m);
                    }
                }
                if(cm.getAllMethods().isEmpty()){
                    toRemove.add(cm);
                }
            }
            for(ClassModel cmm : toRemove){
                tm.removeClass(cmm.getName());
                cleanSplitCounter++;
            }
        }
        OutputUtil.log("Clean splitted features from classes: " + cleanSplitCounter);
        c.getAffinity().traceListChanged(c.getTraceSet());
    }
    
    private static boolean isUsedByOtherFeats(ClassModel cm, Set<TraceModel> tms){
        Set<String> featsUsing = new HashSet<String>();
        for(TraceModel tm : tms){
            if(tm.getClass(cm.getName())!=null){
                featsUsing.add(tm.getName());
            }
        }
        
        return featsUsing.size()>1;
    }
}
