/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.affinity;

import dk.sdu.mmmi.featureous.core.model.TraceListChangeListener;
import dk.sdu.mmmi.featureous.core.model.TraceSet;

/**
 *
 * @author ao
 */
public interface AffinityProvider extends TraceListChangeListener {

    Affinity getClassAffinity(String className);
    Affinity getPkgAffinity(String pkgName);
    Affinity getMethodAffinity(String methodName);
    Affinity getFieldAffinity(String fieldName);

    void traceListChanged(TraceSet tl);
}
