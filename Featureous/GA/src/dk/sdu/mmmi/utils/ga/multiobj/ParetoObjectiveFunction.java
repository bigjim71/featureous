/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.utils.ga.multiobj;

import dk.sdu.mmmi.utils.ga.DecValChromosome;

public interface ParetoObjectiveFunction {
    Double[] evaluateChromosomeFitness(DecValChromosome h);
}
