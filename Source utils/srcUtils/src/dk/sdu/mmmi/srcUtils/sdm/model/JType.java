/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.sdm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ao
 */
public class JType implements Serializable{
    private final String qualName;
    private boolean processed = false;
    private boolean topLevel = false;
    private boolean interfaceType = false;
    private boolean publicAccess;
    private List<JDependency> refTypes = new HashList<JDependency>();
    private List<JType> enclosedTypes = new HashList<JType>();
    private int fieldCount;
    private int methodCount;
    private int constructorCount;
    private int estAccessorCount = 0;

    public JType(String qualName) {
        this.qualName = qualName;
    }

    public boolean isInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(boolean interfaceType) {
        this.interfaceType = interfaceType;
    }

    public String getQualName() {
        return qualName;
    }

    public List<JDependency> getDependencies() {
        return refTypes;
    }

    public int getFieldCount() {
        return fieldCount;
    }

    public int getMethodCount() {
        return methodCount;
    }

    public void setFieldCount(int fields) {
        this.fieldCount = fields;
    }

    public void setMethodCount(int methods) {
        this.methodCount = methods;
    }

    public void setConstructorCount(int constructors) {
        this.constructorCount = constructors;
    }

    public int getConstructorCount() {
        return constructorCount;
    }

    public int getEstAccessorCount() {
        return estAccessorCount;
    }

    public void setEstAccessorCount(int estAccessorCount) {
        this.estAccessorCount = estAccessorCount;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public void setPublicAccess(boolean publicAccess) {
        this.publicAccess = publicAccess;
    }

    public void setTopLevel(boolean topLevel) {
        this.topLevel = topLevel;
    }

    public boolean isTopLevel() {
        return topLevel;
    }

    public boolean isProcessed() {
        return processed;
    }

    public boolean isPublicAccess() {
        return publicAccess;
    }

    public List<JType> getEnclosedTypes() {
        return enclosedTypes;
    }

    public void setEnclosedTypes(List<JType> enclosedTypes) {
        this.enclosedTypes = enclosedTypes;
    }
    
    public List<JDependency> getDependenciesTowards(JType t, boolean excludeAccessors){
        List<JDependency> res = new ArrayList<JDependency>();
        for(JDependency d : this.refTypes){
            if(d.getReferencedType().equals(t)){
                if(!excludeAccessors && d.isAccessor()){
                    continue;
                }
                res.add(d);
            }
        }
        return res;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if(obj instanceof JType){
            final JType other = (JType) obj;
            if ((this.qualName == null) ? (other.qualName != null) : !this.qualName.equals(other.qualName)) {
                return false;
            }
            return true;
        }else
        if(obj instanceof String){
            final String other = (String) obj;
            if ((this.qualName == null) ? (other != null) : !this.qualName.equals(other)) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = (this.qualName != null ? this.qualName.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "\n\t" + this.qualName;
    }
}
