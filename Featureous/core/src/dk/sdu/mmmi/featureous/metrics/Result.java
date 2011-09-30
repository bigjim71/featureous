/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.metrics;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author ao
 */
public class Result implements Comparable<Result>{

    public final Float value;
    public final String name;

    public Result(Float res, String name) {
        this.value = res;
        this.name = name;
    }

    public int compareTo(Result o) {
        return o.value.compareTo(this.value);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public static void sortByName(List<Result> ress){
        Collections.sort(ress, new Comparator<Result>(){

            @Override
            public int compare(Result o1, Result o2) {
                return o1.name.compareTo(o2.name);
            }
        });
    }
}
