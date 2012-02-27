/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.utils.ga.multiobj;

import dk.sdu.mmmi.utils.ga.DecValChromosome;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.api.progress.ProgressHandle;

public class ParetoGA {

    protected KeyedTuple<DecValChromosome, Double>[] population =
            new KeyedTuple[0];
    protected int era = 0;
    final double mutationPercent;
    protected Set<KeyedTuple<DecValChromosome, Double>> paretoFront =
            new HashSet<KeyedTuple<DecValChromosome, Double>>();
    protected ParetoObjectiveFunction of;
    private KeyedTuple<DecValChromosome, Double> proto;

    public ParetoGA(ParetoObjectiveFunction f, int populationCount, double mutationProb, DecValChromosome prototype) {
        this.of = f;
        proto = new KeyedTuple<DecValChromosome, Double>(prototype, of.evaluateChromosomeFitness(prototype));
        this.mutationPercent = mutationProb;
        createNewPopulation(populationCount, prototype);
    }

    public List<Double> getMeanFitness(KeyedTuple<DecValChromosome, Double>[] subj) {
        Double[] sum = new Double[0];
        for (KeyedTuple<DecValChromosome, Double> e : subj) {
            if (sum.length == 0) {
                sum = Arrays.copyOf(e.getValues(), e.getValues().length);
            } else {
                for (int i = 0; i < e.getValues().length; i++) {
                    sum[i] = sum[i] + e.getValues()[i];
                }
            }
        }

        for (int i = 0; i < sum.length; i++) {
            sum[i] = sum[i] / (double) subj.length;
        }

        return Arrays.asList(sum);
    }

    public List<Double> getMaxFitness(Collection<KeyedTuple<DecValChromosome, Double>> subj) {
        Double[] sum = new Double[0];
        for (KeyedTuple<DecValChromosome, Double> e : subj) {
            if (sum.length == 0) {
                sum = Arrays.copyOf(e.getValues(), e.getValues().length);
            } else {
                for (int i = 0; i < e.getValues().length; i++) {
                    if (sum[i] < e.getValues()[i]) {
                        sum[i] = e.getValues()[i];
                    }
                }
            }
        }

        return Arrays.asList(sum);
    }

    private void updateParetoFront(
            Map<KeyedTuple<DecValChromosome, Double>, Integer> dominance) {

        Set<KeyedTuple<DecValChromosome, Double>> toAdd =
                new HashSet<KeyedTuple<DecValChromosome, Double>>();
        for (Entry<KeyedTuple<DecValChromosome, Double>, Integer> e : dominance.entrySet()) {
            if (e.getValue() == 0) {
                toAdd.add(e.getKey());
            }
        }
        Map<KeyedTuple<DecValChromosome, Double>, Integer> newDom = 
                new HashMap<KeyedTuple<DecValChromosome, Double>, Integer>();
        
        for(KeyedTuple<DecValChromosome, Double> p : population){
            newDom.put(p, dominance.get(p));
        }
        
        dominance.clear();
        dominance.putAll(newDom);
        
        paretoFront.clear();
        
        paretoFront.addAll(toAdd);

    }

    public final Set<KeyedTuple<DecValChromosome, Double>> evolve(AtomicInteger stopOnEra, ProgressHandle progress) {

        if (era > 0) {
            throw new RuntimeException("Evolution already ran for this population.");
        }

        for (int i = 0; i < stopOnEra.get(); i++) {
            era = i;
            insertClonesIntoPopulation(paretoFront.toArray(new KeyedTuple[0]), 10);
            evaluatePopulationFitness();
            
            progress.progress("Era " + i + ", mean fitness of population = " + getMeanFitness(population), i);
            
            Map<KeyedTuple<DecValChromosome, Double>, Integer> dom = getDominanceRelations();
            updateParetoFront(dom);

            breedCrossOver(selectReproductors(dom));
            mutate(mutationPercent);
        }

        return paretoFront;
    }

    private void createNewPopulation(int count, DecValChromosome prototype) {
        population = new KeyedTuple[count];
        for (int i = 0; i < count; i++) {
            DecValChromosome c = prototype.getClone();
            c.randomize();
            population[i] = new KeyedTuple<DecValChromosome, Double>(c, null);
        }
        evaluatePopulationFitness();
    }

    private void evaluatePopulationFitness() {
        for (int i = 0; i < population.length; i++) {
            KeyedTuple<DecValChromosome, Double> curr = population[i];
            if (curr.getValues() == null) {
                curr.setValues(of.evaluateChromosomeFitness(curr.getKey()));
            }
        }
    }

    private Map<KeyedTuple<DecValChromosome, Double>, Integer> getDominanceRelations() {
        // 0 - undominated
        Set<KeyedTuple<DecValChromosome, Double>> all = new HashSet<KeyedTuple<DecValChromosome, Double>>();
        all.addAll(paretoFront);
        all.addAll(Arrays.asList(population));
        Map<KeyedTuple<DecValChromosome, Double>, Integer> keys =
                new IdentityHashMap<KeyedTuple<DecValChromosome, Double>, Integer>();

        for (KeyedTuple<DecValChromosome, Double> e : all) {
            keys.put(e, 0);
            for (KeyedTuple<DecValChromosome, Double> e2 : all) {
                if (e.isBelow(e2)) {
                    keys.put(e, keys.get(e) + 1);
                }
            }
        }

        return keys;
    }

    public KeyedTuple<DecValChromosome, Double>[] selectReproductors(
            Map<KeyedTuple<DecValChromosome, Double>, Integer> dominance) {
        // Simple roulette, fitness can be negative!

        int sumVals = 0;
        Integer max = Collections.max(dominance.values());
        for (Integer i : dominance.values()) {
            sumVals += (-i) + max + 1;
        }

        int numRep = population.length/2;
        KeyedTuple<DecValChromosome, Double>[] breeders = new KeyedTuple[numRep];
        for (int i = 0; i < numRep; i++) {
            double roulette = Randomizer.nextDouble() * sumVals;
            for (KeyedTuple<DecValChromosome, Double> e : population) {
                roulette -= (-dominance.get(e) + max + 1);
                if (roulette < 0) {
                    breeders[i] = e;
                    break;
                }
            }
        }

        return breeders;
    }

    public void breedCrossOver(KeyedTuple<DecValChromosome, Double>[] reproductors) {
        Set<DecValChromosome> toAdd = new HashSet<DecValChromosome>();
        for (int i = 0; i + 1 < reproductors.length; i++) {
            KeyedTuple<DecValChromosome, Double> mother = reproductors[i];
            KeyedTuple<DecValChromosome, Double> father = reproductors[i + 1];

            DecValChromosome child = mother.getKey().getClone();
            child.crossOverWith(father.getKey().getClone());

            toAdd.add(child);
        }

        for (DecValChromosome chr : toAdd) {
            population[Randomizer.nextInt(population.length)] = new KeyedTuple<DecValChromosome, Double>(chr, null);
        }
    }

    public void mutate(double probability) {
        for (KeyedTuple<DecValChromosome, Double> chr : population) {
            chr.getKey().randomizeGenes(probability);
        }
    }

    public void insertClonesIntoPopulation(KeyedTuple<DecValChromosome, Double>[] src, int numberOfClones) {
        if (src.length == 0) {
            return;
        }
        KeyedTuple<DecValChromosome, Double>[] pfl = Arrays.copyOf(src, src.length);

        for (int i = 0; i < numberOfClones; i++) {
            KeyedTuple<DecValChromosome, Double> c = pfl[Randomizer.nextInt(pfl.length)];
            population[i] = new KeyedTuple<DecValChromosome, Double>(c.getKey().getClone(),
                    (c.getValues() == null) ? null : Arrays.copyOf(c.getValues(), c.getValues().length));
        }
    }
}
