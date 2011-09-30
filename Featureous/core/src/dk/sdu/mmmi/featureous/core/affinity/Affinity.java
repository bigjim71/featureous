/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.affinity;

import java.awt.Color;

/**
 *
 * @author ao
 */
public enum Affinity {

    SINGLE_FEATURE(new Color(141, 222, 14), "Single-feature", 5),
    SINGLE_GROUP(new Color(75, 198, 187), "Infra-group", 4),
    INTER_GROUP(new Color(112, 188, 249), "Intra-group", 3),
    GROUP_CORE(new Color(112, 188, 249), "Group-core", 1),
    CORE(new Color(35, 126, 217), "Core", 0);

    public final Color color;
    public final String name;
    public final int weigth;

    private Affinity(Color c, String name, int weigth) {
        this.color = c;
        this.name = name;
        this.weigth = weigth;
    }
    
    public static class Comparator implements java.util.Comparator<Affinity> {
        @Override
        public int compare(Affinity o1, Affinity o2) {
            return ((Integer)o1.weigth).compareTo(o2.weigth);
        }
        
    }
}
