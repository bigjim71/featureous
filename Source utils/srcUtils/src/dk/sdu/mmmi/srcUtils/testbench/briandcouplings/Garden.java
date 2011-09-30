/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.testbench.briandcouplings;

import dk.sdu.mmmi.srcUtils.testbench.briandcouplings.ancestors.Berry;
import dk.sdu.mmmi.srcUtils.testbench.briandcouplings.ancestors.Fruit;
import dk.sdu.mmmi.srcUtils.testbench.briandcouplings.ancestors.Plant;
import java.util.List;
import java.util.Map;


public class Garden {
	Map<Plant, List<Fruit>> fruits;
	Fruit bestFruitAround;
	
	public Fruit getBestFruitAround(){
		return bestFruitAround;
	}
	
	public void setBestFruitAround(Fruit f){
		bestFruitAround = f;
	}
	
	public boolean doPlantsExist(List<Plant> p){
		return false;
	}
	
	public Map<Plant, List<Fruit>> getFruits(){
		class Worker{
			public void pickFruits(){}
		}
		
		Worker w = new Worker();
		Berry p = new Berry();
		return fruits;
	}
	
	private class Storage{
		private List<Fruit> storedFruit;
		public void store(Fruit f){
			storedFruit.add(f);
		}
	}
}
