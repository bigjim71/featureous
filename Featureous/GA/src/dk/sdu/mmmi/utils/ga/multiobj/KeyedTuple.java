/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.utils.ga.multiobj;

import dk.sdu.mmmi.utils.ga.DecValChromosome;
import java.util.Arrays;

public class KeyedTuple<E, F extends Number> implements Comparable<KeyedTuple> {

    private E key;
    private Object[] values;

    public KeyedTuple(E first, F[] second) {
        this.key = first;
        this.values = null;
        if (second != null) {
            this.values = Arrays.copyOf(second, second.length);
        }
    }

    public E getKey() {
        return key;
    }

    public F[] getValues() {
        return (F[]) values;
    }

    public void setFirst(E first) {
        this.key = first;
    }

    public void setValues(F[] second) {
        this.values = Arrays.copyOf(second, second.length);
    }

    @Override
    public String toString() {
        return key.toString() + " - " + values.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        KeyedTuple other = (KeyedTuple) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        return true;
    }

    public boolean isAbove(KeyedTuple<DecValChromosome, Double> other){
        boolean above = true;
        boolean allEqual = true;
        for (int i = 0; i < getValues().length; i++) {
            allEqual = allEqual && getValues()[i].doubleValue() == other.getValues()[i].doubleValue();
            if (other.getValues()[i].doubleValue() > getValues()[i].doubleValue()) {
                above = false;
                break;
            }
        }
        if(allEqual){
            return false;
        }
        return above;
    }

    public boolean isBelowOrEqual(KeyedTuple<DecValChromosome, Double> other){
        boolean below = true;
        boolean allEqual = true;
        for (int i = 0; i < getValues().length; i++) {
            allEqual = allEqual && getValues()[i].doubleValue() == other.getValues()[i].doubleValue();
            if (getValues()[i].doubleValue() > other.getValues()[i].doubleValue()) {
                below = false;
                break;
            }
        }
        if(allEqual){
            return true;
        }
        return below;
    }

    public boolean isBelow(KeyedTuple<DecValChromosome, Double> other){
        boolean below = true;
        boolean allEqual = true;
        for (int i = 0; i < getValues().length; i++) {
            allEqual = allEqual && getValues()[i].doubleValue() == other.getValues()[i].doubleValue();
            if (getValues()[i].doubleValue() > other.getValues()[i].doubleValue()) {
                below = false;
                break;
            }
        }
        if(allEqual){
            return false;
        }
        return below;
    }

    public int compareTo(KeyedTuple o) {

        if (o.getValues().length != getValues().length) {
            throw new RuntimeException(o.getValues().length + " != " + getValues().length);
        }
        
        if (isAbove(o)) {
            return 1;
        } else if (isBelow(o)) {
            return -1;
        } else {
            return 0;
        }
    }
}
