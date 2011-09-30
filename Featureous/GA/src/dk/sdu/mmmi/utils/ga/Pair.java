/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.utils.ga;

public class Pair<E, F extends Number> extends Number{

	private E first;
	private F second;

	public Pair(E first, F second){
		this.first = first;
		this.second = second;
	}
	
	public E getFirst() {
		return first;
	}
	
	public F getSecond() {
		return second;
	}
	
	public void setFirst(E first) {
		this.first = first;
	}
	
	public void setSecond(F second) {
		this.second = second;
	}
	
	@Override
	public String toString() {
		return first.toString() + " - " + second.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair other = (Pair) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		return true;
	}

	@Override
	public double doubleValue() {
		return second.doubleValue();
	}

	@Override
	public float floatValue() {
		return second.floatValue();
	}

	@Override
	public int intValue() {
		return second.intValue();
	}

	@Override
	public long longValue() {
		return second.longValue();
	}

}
