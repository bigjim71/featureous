/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.utils.ga;

import dk.sdu.mmmi.utils.ga.multiobj.Randomizer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Deprecated
public class GA {

    private List<Pair<DecValChromosome, Double>> population = new ArrayList<Pair<DecValChromosome, Double>>();
    private int era = 0;
    final double mutationPercent;
    private ObjectiveFunction of;
    private List<List<Double>> evolutionStats = new LinkedList<List<Double>>();

    public GA(ObjectiveFunction f, int populationCount, double mutationProb, DecValChromosome prototype) {
        this.of = f;
        this.mutationPercent = mutationProb;
        createNewPopulation(populationCount, prototype);
    }

    public Pair<DecValChromosome, Double> getCurrentlyBestChromosomeEntry() {
        Pair<DecValChromosome, Double> maxEntry = population.get(0);
        for (Pair<DecValChromosome, Double> e : population) {
            if (e.getSecond() > maxEntry.getSecond()) {
                maxEntry = e;
            }
        }
        return maxEntry;
    }

    public Double getMeanFitness() {
        Double sum = 0d;
        for (Pair<DecValChromosome, Double> e : population) {
            sum += e.getSecond();
        }
        return sum / (double) population.size();
    }

    public Pair<DecValChromosome, Double> evolve(Integer stopOnEra, Double stopOnFitness) {
        if (stopOnEra == null && stopOnFitness == null) {
            throw new RuntimeException("Choose a stop criteria!");
        }

        if (era > 0) {
            throw new RuntimeException("Evolution already ran for this population.");
        }

        Pair<DecValChromosome, Double> bestChromosomeEver = population.get(0);

        if (stopOnEra == null) {
            // A reasonable value?
            stopOnEra = 1000;
        }

        evaluatePopulationFitness();

        for (int i = 0; i < stopOnEra; i++) {
            era = i;
            breedCrossOver(selectReproductors());
            mutate(mutationPercent);
            evaluatePopulationFitness();
            Pair<DecValChromosome, Double> currBest = getCurrentlyBestChromosomeEntry();

            if (bestChromosomeEver.getSecond() < currBest.getSecond()) {
                bestChromosomeEver = new Pair<DecValChromosome, Double>(currBest.getFirst().getClone(), currBest.getSecond());
            }

            evolutionStats.add(getMeanPopulationStats());

            insertClonesIntoPopulation(bestChromosomeEver, 5);

//            System.out.print("Era " + i + ", mean fitness = " + getMeanFitness() + ", ");

            if (stopOnFitness != null && currBest.getSecond() > stopOnFitness) {
                break;
            }
        }

        return bestChromosomeEver;
    }

    private void createNewPopulation(int count, DecValChromosome prototype) {
        for (int i = 0; i < count; i++) {
            DecValChromosome c = prototype.getClone();
            c.randomize();
            population.add(new Pair(c, null));
        }

        evaluatePopulationFitness();
    }

    private void evaluatePopulationFitness() {
        for (int i = 0; i < population.size(); i++) {
            if (population.get(i).getSecond() == null) {
                population.get(i).setSecond(of.evaluateChromosomeFitness(population.get(i).getFirst()));
            }
        }
    }

    private List<Double> getMeanPopulationStats() {
        List<List<Double>> stats = new ArrayList<List<Double>>();
        for (int i = 0; i < population.size(); i++) {
            stats.add(of.getStatsForChromosome(population.get(i).getFirst()));
        }
        List<Double> sums = new ArrayList<Double>();
        List<Double> divs = new ArrayList<Double>();
        // Init sums list
        for (int i = 0; i < stats.get(0).size(); i++) {
            sums.add(0d);
            divs.add(0d);
        }

        // Calc sums
        for (List<Double> stat : stats) {
            for (int i = 0; i < stat.size(); i++) {
                if (stat.get(i) != null) {
                    sums.set(i, sums.get(i) + stat.get(i));
                    divs.set(i, divs.get(i) + 1d);
                }
            }
        }
        // Calc Avgs
        for (int i = 0; i < sums.size(); i++) {
            if (divs.get(i) != 0) {
                sums.set(i, sums.get(i) / divs.get(i));
            } else {
                sums.set(i, null);
            }
        }

        return sums;
    }

    private List<Pair<DecValChromosome, Double>> selectReproductors() {
        // Simple roulette, fitness can be negative!
        double min = population.get(0).getSecond();
        double max = population.get(0).getSecond();
        List<Pair<DecValChromosome, Double>> populationList = new ArrayList(population);
        for (Pair<DecValChromosome, Double> e : populationList) {
            if (e.getSecond() > max) {
                max = e.getSecond();
            }
            if (e.getSecond() < min) {
                min = e.getSecond();
            }
        }
        double sumFitnessAboveMin = 0;
        for (Pair<DecValChromosome, Double> e : populationList) {
            sumFitnessAboveMin += e.getSecond() - min;
        }

        int numRep = population.size();
        List<Pair<DecValChromosome, Double>> breeders = new LinkedList<Pair<DecValChromosome, Double>>();
        for (int i = 0; i < numRep; i++) {
            double roulette = Randomizer.nextDouble() * sumFitnessAboveMin;
            for (Pair<DecValChromosome, Double> e : populationList) {
                roulette -= e.getSecond() - min;
                if (roulette < 0) {
                    breeders.add(e);
                    break;
                }
            }
        }

        return breeders;
    }

    private void breedCrossOver(List<Pair<DecValChromosome, Double>> reproductors) {
        int preSize = population.size();
        List<DecValChromosome> toAdd = new LinkedList<DecValChromosome>();
        while (reproductors.size() > 1) {
            Pair<DecValChromosome, Double> mother = reproductors.remove(0);
            Pair<DecValChromosome, Double> father = reproductors.remove(0);

            if (mother.getSecond() > father.getSecond()) {
                population.remove(father);
            } else {
                population.remove(mother);
            }

            DecValChromosome child = mother.getFirst().getClone();
            child.crossOverWith(father.getFirst().getClone());

            toAdd.add(child);
        }

        for (DecValChromosome chr : toAdd) {
            if (population.size() < preSize) {
                population.add(new Pair(chr, null));
            }
        }

        if (population.size() != preSize) {
            throw new RuntimeException("Breeding bug.");
        }
    }

    private void mutate(double probability) {
        for (Pair<DecValChromosome, Double> chr : population) {
            chr.getFirst().randomizeGenes(probability);
        }
    }

    public void insertClonesIntoPopulation(Pair<DecValChromosome, Double> ch, int numberOfClones) {
        for (int i = 0; i < numberOfClones; i++) {
            population.get(i).setFirst(ch.getFirst().getClone());
            population.get(i).setSecond(ch.getSecond());
        }
    }

    public List<List<Double>> getEvolutionStats() {
        return evolutionStats;
    }
}
