/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.utils.mogga;

import dk.sdu.mmmi.utils.ga.DecValChromosome;
import dk.sdu.mmmi.utils.ga.multiobj.KeyedTuple;
import dk.sdu.mmmi.utils.ga.multiobj.ParetoGA;
import dk.sdu.mmmi.utils.ga.multiobj.ParetoObjectiveFunction;
import dk.sdu.mmmi.utils.ga.multiobj.Randomizer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Representation of chromosome:
 * the same as usaually, BUT group numbers in one chromosome DO NOT correspond 
 * to group numbers in the other!
 * 
 * 
 * @author ao
 */
public class MOGGA extends ParetoGA{

    public MOGGA(ParetoObjectiveFunction f, int populationCount, double mutationProb, DecValChromosome prototype) {
        super(f, populationCount, mutationProb, prototype);
    }

    @Override
    public void breedCrossOver(KeyedTuple<DecValChromosome, Double>[] reproductors) {
        Set<DecValChromosome> toAdd = new HashSet<DecValChromosome>();
        for (int i = 0; i + 1 < reproductors.length; i++) {
            KeyedTuple<DecValChromosome, Double> mother = reproductors[i];
            KeyedTuple<DecValChromosome, Double> father = reproductors[i + 1];

            DecValChromosome childM = childFirstToSecond(mother.getKey(), father.getKey());
            DecValChromosome childF = childFirstToSecond(father.getKey(), mother.getKey());

            toAdd.add(childM);
            toAdd.add(childF);
        }

        for (DecValChromosome chr : toAdd) {
            population[Randomizer.nextInt(population.length)] = new KeyedTuple<DecValChromosome, Double>(chr, null);
        }
    }
    
    private DecValChromosome childFirstToSecond(DecValChromosome first, DecValChromosome second){
        DecValChromosome child = second.getClone();
        int groupToTransfer = first.getGeneVal(Randomizer.nextInt(first.getLength()));
        
        Set<Integer> classesToTrn = new HashSet<Integer>();
        for(int i=0;i<first.getLength();i++){
            if(first.getGeneVal(i) == groupToTransfer){
                classesToTrn.add(i);
            }
        }
        
        Set<Integer> cGroups = new HashSet<Integer>();
        for(int i=0;i<child.getLength();i++){
            cGroups.add(child.getGeneVal(i));
        }
        
        int newGID = getFirstEmptyGroupID(cGroups);
        
        for(int i=0;i<child.getLength();i++){
            if(classesToTrn.contains(i)){
                child.setGeneVal(i, newGID);
            }
        }
        return child;
    }

    @Override
    public void mutate(double probability) {
        // find module for an orphan, split biggest module, join two smallest modules
        //Group->numclasses count
        for (KeyedTuple<DecValChromosome, Double> chr : population) {
            if(Randomizer.nextFloat()<probability){
                
                Map<Integer, Integer> groupToSize = getGroupSizes(chr.getKey());
                int choice = Randomizer.nextInt(3);
                switch(choice){
                    case 0 : 
                        adoptOrphan(groupToSize, chr.getKey());
                        break;
                    case 1 :
                        joinTwoSmallest(groupToSize, chr.getKey());
                        break;
                    case 2 :
                        splitBiggest(groupToSize, chr.getKey());
                        break;
                }
            }
        }
    }

    private Map<Integer, Integer> getGroupSizes(DecValChromosome chr) {
        Map<Integer, Integer> groups = new HashMap<Integer, Integer>();
        for(int i=0;i<chr.getLength();i++){
            Integer g = chr.getGeneVal(i);
            Integer count = groups.get(g);
            if(count == null){
                count = 0;
            }
            groups.put(g, count+1);
        }
        return groups;
    }
    
    private boolean adoptOrphan(Map<Integer, Integer> groupToSize, DecValChromosome chr){
        if(!groupToSize.containsValue(1)){
            return false;
        }
        
        int orphan = -1;
        for(Map.Entry<Integer, Integer> e : groupToSize.entrySet()){
            if(e.getValue()==1){
                orphan = e.getKey();
                break;
            }
        }
        if(orphan!=-1){
            for(int i = 0; i<chr.getLength();i++){
                if(chr.getGeneVal(i)==orphan){
                    int newGroup = chr.getGeneVal(Randomizer.nextInt(chr.getLength()));
                    chr.setGeneVal(i, newGroup);
                    return true;
                }
            }
            return false;
        }else{
            return false;
        }
    }

    private void joinTwoSmallest(Map<Integer, Integer> groupToSize, DecValChromosome key) {
        List<Integer> l = new LinkedList<Integer>(groupToSize.values());
        if(l.size()<2){
            return;
        }
        Collections.sort(l);
        int smallestG1 = -1;
        int smallestG2 = -1;
        for(Map.Entry<Integer, Integer> e : groupToSize.entrySet()){
            if(e.getValue()==l.get(0)){
                smallestG1 = e.getKey();
                break;
            }
        }
        
        for(Map.Entry<Integer, Integer> e : groupToSize.entrySet()){
            if(e.getValue()==l.get(1) && e.getKey()!=smallestG1){
                smallestG2 = e.getKey();
                break;
            }
        }
        
        for(int i=0;i<key.getLength();i++){
            if(key.getGeneVal(i)==smallestG2){
                key.setGeneVal(i, smallestG1);
            }
        }
    }

    private void splitBiggest(Map<Integer, Integer> groupToSize, DecValChromosome key) {
        List<Integer> l = new LinkedList<Integer>(groupToSize.values());
        Collections.sort(l);
        int biggestGVal = l.get(l.size()-1);
        int biggestG = -1;
        for(Map.Entry<Integer, Integer> e : groupToSize.entrySet()){
            if(biggestGVal==e.getValue()){
                biggestG = e.getKey();
                break;
            }
        }
        int newID = getFirstEmptyGroupID(groupToSize.keySet());
        
        for(int i = 0; i<key.getLength(); i++){
            if(key.getGeneVal(i) == biggestG){
                if(Randomizer.nextFloat()<0.5){
                    key.setGeneVal(i, newID);
                }
            }
        }
    }
    
    private int getFirstEmptyGroupID(Set<Integer> groups){
        for(int i = 0; i<5000;i++){
            if(!groups.contains(i)){
                return i;
            }
        }
        throw new RuntimeException("Error in generating group ID: " + groups);
    }
}
