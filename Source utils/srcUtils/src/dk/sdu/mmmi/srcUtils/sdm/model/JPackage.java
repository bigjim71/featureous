/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.sdm.model;

import java.util.Collections;
import java.util.List;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ao
 */
public class JPackage implements Serializable{
    private String qualName;
    private Map<String, JType> types = new HashMap<String, JType>();
    private final StaticDependencyModel sdm;
    
    JPackage(String qualName, StaticDependencyModel sdm) {
        this.qualName = qualName;
        this.sdm = sdm;
    }

    public void setQualName(String qualName) {
        this.qualName = qualName;
    }

    public String getQualName() {
        return qualName;
    }

    public JType getOrAddTypeByQualName(String name){
        if(types.containsKey(name)){
            return types.get(name);
        }else{
            JType t = new JType(name);
            t.setPkg(this, sdm);
            types.put(name, t);
            return t;
        }
    }

    public JType getTypeByQualNameOrNull(String name){
        return types.get(name);
    }
    
    public JType getOrAddType(JType c){
        if(!types.containsKey(c.getQualName())){
            types.put(c.getQualName(), c);
            c.setPkg(this,sdm);
            return c;
        }
        return types.get(c.getQualName());
    }

    public Collection<JType> getAllTypes() {
        return types.values();
    }

    public Set<String> getAllTypeNames() {
        return types.keySet();
    }

    public List<JType> getTopLevelTypes() {
        List<JType> res = new ArrayList<JType>();
        for(JType t : types.values()){
            if(t.isTopLevel()){
                res.add(t);
            }
        }
        return res;
    }
    
    Map<String, JType> getModifiableTypes() {
        return types;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if(obj instanceof JPackage){
            final JPackage other = (JPackage) obj;
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
        int hash = 3;
        hash = 37 * hash + (this.qualName != null ? this.qualName.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "\n +" + this.qualName + this.types;
    }
}
