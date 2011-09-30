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

/**
 *
 * @author ao
 */
public class JPackage implements Serializable{
    private String qualName;
    private List<JType> types = new HashList<JType>();

    JPackage(String qualName) {
        this.qualName = qualName;
    }

    public String getQualName() {
        return qualName;
    }

    public JType getOrAddTypeByQualName(String cName){
        String name = cName;
        int index = types.indexOf(name);
        if(index != -1){
            return types.get(index);
        }else{
            JType t = new JType(name);
            types.add(t);
            return t;
        }
    }

    public JType getTypeByQualNameOrNull(String cName){
        String name = cName;
        int index = types.indexOf(name);
        if(index != -1){
            return types.get(index);
        }else{
            return null;
        }
    }
    
    public JType getOrAddType(JType c){
        int index = types.indexOf(c);
        if(index == -1){
            types.add(c);
        }
        return types.get(types.indexOf(c));
    }

    public List<JType> getAllTypes() {
        return Collections.unmodifiableList(types);
    }

    public List<JType> getTopLevelTypes() {
        List<JType> res = new ArrayList<JType>();
        for(JType t : types){
            if(t.isTopLevel()){
                res.add(t);
            }
        }
        return Collections.unmodifiableList(res);
    }
    
    List<JType> getModifiableTypes() {
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
