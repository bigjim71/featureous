/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.similarity;

import dk.sdu.mmmi.featureous.core.model.TraceModel;

/**
 * @author ao
 */
public interface SimilarityRelation {
    Double getSimilarity(TraceModel f1, TraceModel f2);
}
