/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.sdm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ao
 */
public class JType implements Serializable{
    private final String qualName;
    private boolean processed = false;
    private boolean topLevel = false;
    private boolean interfaceType = false;
    private boolean abstractType = false;
    private boolean publicAccess;
    private Map<StaticDependencyModel, JPackage> pkgs = new HashMap<StaticDependencyModel, JPackage>();
    private List<JType> incomingDeps = new ArrayList<JType>();
    private List<JDependency> refTypes = new ArrayList<JDependency>();
    private List<JDependency> superTypes = new ArrayList<JDependency>();
    private List<JType> enclosedTypes = new ArrayList<JType>();
    private int fieldCount;
    private int methodCount;
    private int constructorCount;
    private int estAccessorCount = 0;
    private int loc = 0;

    public JType(String qualName) {
        this.qualName = qualName;
    }

    public void setLoc(int loc) {
        if(loc>0){
            this.loc = loc;
        }
    }

    public int getLoc() {
        return loc;
    }

    public void setPkg(JPackage pkg, StaticDependencyModel sdm) {
        pkgs.put(sdm, pkg);
    }

    public JPackage getPkg(StaticDependencyModel sdm) {
        return pkgs.get(sdm);
    }

    public boolean isAbstractType() {
        return abstractType;
    }
    
    public void setAbstractType(boolean aAbstract) {
        abstractType = aAbstract;
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

    public List<JType> getIncomingDeps() {
        return incomingDeps;
    }

    public List<JDependency> getSuperDependencies() {
        return superTypes;
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
    
    public int getDepsTowardsCount(JType t){
        int count = 0;
        for(JDependency d : this.refTypes){
            if(d.getReferencedType().getQualName().equals(t.getQualName())){
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        
        if(this==obj){
            return true;
        }
        
        return hashCode()==obj.hashCode();
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
