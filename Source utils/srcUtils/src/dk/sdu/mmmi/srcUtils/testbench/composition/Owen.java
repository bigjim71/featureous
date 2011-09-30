/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.testbench.composition;

public class Owen {

	private Flour f;
	
	public void putIn(Flour f) {
		this.f = f;
	}

	public Bread takeOut() {
		return new Bread();
	}

}
