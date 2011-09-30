/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.testbench.composition;

public class Baker {
	Owen o = new Owen();
	public Bread bake(Flour f){
		o.putIn(f);
		return o.takeOut();
	}
}
