/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.sdm.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author ao
 */
public class HashList<E> extends ArrayList<E> implements Serializable{

    @Override
    public boolean contains(Object o) {
        for(E el : this){
            if(el.equals(o)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int indexOf(Object o) {
        for(int i = 0; i<this.size(); i++){
            if(this.get(i).equals(o)){
                return i;
            }
        }
        return -1;
    }
}
