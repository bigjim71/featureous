/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.featurecallgraph;

import java.awt.Color;

/**
 *
 * @author ao
 */
public enum Keys {

    ID("id", int.class),
    NODE_NAME("name", String.class),
    NODE_ICON("icon", String.class),
    NODE_TYPE("type", String.class),
    NODE_CLASS("class", String.class),
    NODE_AFFINITY("affinity", Color.class),
    NODE_SIZE("size", int.class),
    NODE_CONSTR("constructor", boolean.class),
    EDGE_SOURCE("source", int.class),
    EDGE_TARGET("target", int.class),
    EDGE_STRENGTH("strength", float.class);
    private final String name;
    private final Class type;

    public static final String TYPE_FEATURE = "feature";
    public static final String TYPE_PACKAGE = "package";
    public static final String TYPE_CLASS = "class";
    public static final String TYPE_METHOD = "method";

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
