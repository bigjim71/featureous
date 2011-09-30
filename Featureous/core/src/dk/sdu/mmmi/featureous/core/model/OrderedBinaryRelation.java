/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.model;

/**
 * @author ao
 */
public class OrderedBinaryRelation<Subject, Value> {
    private final Subject first;
    private final Subject second;
    private final Value val;

    public OrderedBinaryRelation(Subject first, Subject second, Value val) {
        this.first = first;
        this.second = second;
        this.val = val;
    }

    public OrderedBinaryRelation(Subject first, Subject second) {
        this.first = first;
        this.second = second;
        this.val = null;
    }

    public Subject getFirst() {
        return first;
    }

    public Subject getSecond() {
        return second;
    }

    public Value getVal() {
        return val;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OrderedBinaryRelation<Subject, Value> other = (OrderedBinaryRelation<Subject, Value>) obj;
        if (this.first == null || !this.first.equals(other.first)) {
            return false;
        }
        if (this.second == null || !this.second.equals(other.second)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.first != null ? this.first.hashCode() : 0);
        hash = 29 * hash + (this.second != null ? this.second.hashCode() : 0);
        return hash;
    }
}
