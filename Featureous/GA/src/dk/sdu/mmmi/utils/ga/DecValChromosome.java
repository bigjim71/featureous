/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.utils.ga;

import dk.sdu.mmmi.utils.ga.multiobj.Randomizer;
import java.util.Arrays;

public class DecValChromosome {

    private int[] genes = new int[0];
    private final int maxGeneVal;
    private final int minGeneVal;

    public DecValChromosome(int length, int minGeneVal, int maxGeneVal) {
        this.minGeneVal = minGeneVal;
        this.maxGeneVal = maxGeneVal;
        genes = new int[length];
        for (int i = 0; i < length; i++) {
            genes[i] = generateValidRandomValue();
        }
    }

    public void randomize() {
        for (int i = 0; i < getLength(); i++) {
            setGeneVal(i, generateValidRandomValue());
        }
    }

    private int generateValidRandomValue() {
        int val = minGeneVal + Randomizer.nextInt(maxGeneVal-minGeneVal+1);
        return val;
    }

    public int getGeneVal(int index) {
        return genes[index];
    }

    public void setGenes(int[] genes) {
        this.genes = genes;
    }

    public void setGeneVal(int index, int value) {
//        if (value > maxGeneVal || value < minGeneVal) {
//            throw new RuntimeException("Gene value out range. = " + value);
//        }
        genes[index] = value;
    }

    public void crossOverWith(DecValChromosome otherChromosome) {
        int cutPoint = Randomizer.nextInt(genes.length);
        for (int i = 0; i <= cutPoint; i++) {
            int thisVal = this.genes[i];
            int otherVal = otherChromosome.getGeneVal(i);

            this.setGeneVal(i, otherVal);
            otherChromosome.setGeneVal(i, thisVal);
        }
    }

    public DecValChromosome getClone() {
        DecValChromosome clone = new DecValChromosome(genes.length, minGeneVal, maxGeneVal);
        clone.setGenes(Arrays.copyOf(genes, genes.length));
        return clone;
    }

    public void randomizeGenes(double geneMutationProb) {
        for (int i = 0; i < genes.length; i++) {
            if (Randomizer.nextDouble() < geneMutationProb) {
                setGeneVal(i, generateValidRandomValue());
            }
        }
    }

    public int getLength() {
        return genes.length;
    }

    public int getMinGeneVal() {
        return minGeneVal;
    }

    public int getMaxGeneVal() {
        return maxGeneVal;
    }

    @Override
    public String toString() {
        return Arrays.asList(genes).toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((genes == null) ? 0 : genes.hashCode());
        result = prime * result + maxGeneVal;
        result = prime * result + minGeneVal;
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
        DecValChromosome other = (DecValChromosome) obj;
        if (genes == null) {
            if (other.genes != null) {
                return false;
            }
        } else if (!genes.equals(other.genes)) {
            return false;
        }
        if (maxGeneVal != other.maxGeneVal) {
            return false;
        }
        if (minGeneVal != other.minGeneVal) {
            return false;
        }
        return true;
    }
}
