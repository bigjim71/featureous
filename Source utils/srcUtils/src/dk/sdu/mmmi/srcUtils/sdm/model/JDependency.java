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

    public static enum Kind{
        GENERAL, TO_SUPER
    }
    
    private JType referencedType;
    private boolean accessor = false;
    private final Kind kind;

    public JDependency(JType referencedType, Kind kind) {
        this.kind = kind;
        this.referencedType = referencedType;
    }

    public Kind getKind() {
        return kind;
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
