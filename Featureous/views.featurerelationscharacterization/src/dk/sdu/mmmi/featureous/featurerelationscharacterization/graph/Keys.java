/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.featurerelationscharacterization.graph;

/**
 *
 * @author ao
 */
public enum Keys {

    ID("id", int.class),
    NODE_NAME("name", String.class),
    NODE_ICON("icon", String.class),
    EDGE_SOURCE("source", int.class),
    EDGE_TARGET("target", int.class),
    EDGE_STRENGTH("strength", float.class),
    EDGE_CONTENTS("contents", String.class),
    EDGE_DIRECTED("directed", boolean.class);
    private final String name;
    private final Class type;

    public String getName() {
        return name;
    }

    public Class getType() {
        return type;
    }

    private Keys(String name, Class type) {
        this.name = name;
        this.type = type;
    }
}
