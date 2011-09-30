/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.utils.ga;

import java.util.List;

public interface ObjectiveFunction {
	double evaluateChromosomeFitness(DecValChromosome h);
    List<Double> getStatsForChromosome(DecValChromosome h);
    String[] getStatsLabels();
}
