/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.testbench.interfaces;

public class OrangeJuice implements Drinkable{

	protected OrangeJuice j;
	
	public void drink() {}
	
	public void checkColour(){}

	public void smell(){
		drink();
	}
	
}
