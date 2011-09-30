/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.sdm.model;

import java.io.Serializable;

/**
 *
 * @author ao
 */
public class JDependency implements Serializable{

    private JType referencedType;
    private boolean accessor = false;

    public JDependency(JType referencedType) {
        this.referencedType = referencedType;
    }

    public void setAccessor(boolean accessor) {
        this.accessor = accessor;
    }

    public boolean isAccessor() {
        return accessor;
    }

    public boolean isPackageLimited(){
        return !referencedType.isPublicAccess();
    }

    public JType getReferencedType() {
        return referencedType;
    }

    @Override
    public String toString() {
        return "-> " + referencedType;
    }
}
