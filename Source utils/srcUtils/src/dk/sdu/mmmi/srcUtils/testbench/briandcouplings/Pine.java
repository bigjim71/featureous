/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.testbench.briandcouplings;

import dk.sdu.mmmi.srcUtils.testbench.briandcouplings.ancestors.Berry.Leaf;
import dk.sdu.mmmi.srcUtils.testbench.briandcouplings.ancestors.Plant;

public class Pine implements Plant{
	
	private Plant returnedSeed;
	private Leaf leaf;
	
	
	public Plant getSeed(){
		returnedSeed = new Pine();
		return returnedSeed;
	}
}
