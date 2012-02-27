package dk.sdu.mmmi.featureous.metrics;

import java.util.IdentityHashMap;
import java.util.List;

/**
 *
 * @author ao
 */
public class CoefficientRelations {
    private IdentityHashMap<String, List<String>> outgoing = new IdentityHashMap<String, List<String>>();
    private IdentityHashMap<String, List<String>> incoming = new IdentityHashMap<String, List<String>>();

    public IdentityHashMap<String, List<String>> getIncoming() {
        return incoming;
    }

    public IdentityHashMap<String, List<String>> getOutgoing() {
        return outgoing;
    }
}
