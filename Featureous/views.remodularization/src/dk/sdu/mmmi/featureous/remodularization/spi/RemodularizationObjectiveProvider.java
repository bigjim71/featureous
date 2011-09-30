/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.spi;

import dk.sdu.mmmi.featureous.metrics.AbstractMetric;

/**
 *
 * @author ao
 */
public interface RemodularizationObjectiveProvider {
    boolean isMinimization();
    String getObjectiveName();
    AbstractMetric createObjective();
}
