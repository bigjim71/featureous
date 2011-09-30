/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.sdm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 *
 * @author ao
 */
public class StaticDependencyModel implements Serializable{

    private List<JPackage> packages = new HashList<JPackage>();
    
    public JPackage getOrAddPackageByName(String qualName){
        int index = packages.indexOf(qualName);
        if(index != -1){
            return packages.get(index);
        }else{
            JPackage p = new JPackage(qualName);
            packages.add(p);
            return p;
        }
    }

    public void doRandomizePackages(){
        List<JType> types = new ArrayList<JType>();
        for(JPackage p : packages){
            types.addAll(p.getTopLevelTypes());
            p.getModifiableTypes().clear();
        }
        int pre = types.size();

        for(JType t : new ArrayList<JType>(types)){
            if(types.contains(t)){
                Random r = new Random(new Date().getTime());
                int idx = r.nextInt(packages.size());
                types.removeAll(Util.deepInsertType(packages.get(idx), t));
            }
        }

        types.clear();
        for(JPackage p : packages){
            types.addAll(p.getTopLevelTypes());
        }
        if(pre!=types.size()){
            throw new RuntimeException("Packages randomization error.");
        }
    }

    public JType getTypeByNameOrNull(String qualName){
        for(JPackage p : packages){
            if(p.getAllTypes().indexOf(qualName)!=-1){
                return p.getAllTypes().get(p.getAllTypes().indexOf(qualName));
            }
        }
        return null;
    }

    public int getAllTypesCount(){
        int i=0;
        for(JPackage p : packages){
            i+=p.getAllTypes().size();
        }
        return i;
    }

    public int getTopLevelTypesCount(){
        int i=0;
        for(JPackage p : packages){
            i+= p.getTopLevelTypes().size();
        }
        return i;
    }
    
    public JPackage getOrAddPackage(JPackage pkg){
        int index = packages.indexOf(pkg);
        if(index == -1){
            packages.add(pkg);
        }
        return pkg;
    }

    public List<JPackage> getPackages() {
        return Collections.unmodifiableList(packages);
    }

    public void cleanup(){
        for(JPackage p : new ArrayList<JPackage>(packages)){
            for(JType t : new ArrayList<JType>(p.getAllTypes())){
                for(JDependency dep : new ArrayList<JDependency>(t.getDependencies())){
                    if(!dep.getReferencedType().isProcessed() || dep.getReferencedType().equals(t)){
                        t.getDependencies().remove(dep);
                    }
                }
            }
        }

        for(JPackage p : new ArrayList<JPackage>(packages)){
            for(JType t : new ArrayList<JType>(p.getAllTypes())){
                if(!t.isProcessed()){
                    p.getModifiableTypes().remove(t);
                }
            }
        }
        for(JPackage p : new ArrayList<JPackage>(packages)){
            if(p.getAllTypes().size()==0){
                packages.remove(p);
            }
        }
    }

    public List<String> getTopLevelTypes() {
        List<String> topLevelTypes = new ArrayList<String>();
        for(JPackage p : getPackages()){
            for(JType t : p.getTopLevelTypes()){
                topLevelTypes.add(t.getQualName());
            }
        }
        return topLevelTypes;
    }
    
    @Override
    public String toString() {
        return packages.toString();
    }
}
