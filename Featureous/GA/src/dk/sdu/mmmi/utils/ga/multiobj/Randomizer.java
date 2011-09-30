/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.utils.ga.multiobj;

import java.util.Random;

/**
 *
 * @author ao
 */
public class Randomizer {
    private static long SEED = 12345;
    private static Random rand = new Random(SEED);

    public static void setSEED(long SEED) {
        Randomizer.SEED = SEED;
    }
    
    public static int nextInt(){
        return rand.nextInt();
    }
    
    public static int nextInt(int n){
        return rand.nextInt(n);
    }
    
    public static float nextFloat(){
        return rand.nextFloat();
    }
    
    public static double nextDouble(){
        return rand.nextDouble();
    }
    
}
