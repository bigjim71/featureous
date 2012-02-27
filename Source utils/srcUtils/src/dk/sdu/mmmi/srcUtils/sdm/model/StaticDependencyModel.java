/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.sdm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author ao
 */
public class StaticDependencyModel implements Serializable{

    private Map<String, JPackage> packages = new HashMap<String, JPackage>();
    
    public void reset(){
        packages.clear();
    }
    
    public void syncPkgNames(){
        Set<JPackage> pkgs = new HashSet<JPackage>();
        pkgs.addAll(packages.values());
        if(pkgs.size()!=packages.values().size()){
            throw new RuntimeException("error in pkgs");
        }
        reset();
        for(JPackage pkg : pkgs){
            packages.put(pkg.getQualName(), pkg);
        }
    }
    
    public JPackage getOrAddPackageByName(String qualName){
        if(packages.containsKey(qualName)){
            return packages.get(qualName);
        }else{
            JPackage p = new JPackage(qualName, this);
            packages.put(qualName, p);
            return p;
        }
    }

    public void doRandomizePackages(){
        List<JType> types = new ArrayList<JType>();
        for(JPackage p : packages.values()){
            types.addAll(p.getTopLevelTypes());
            p.getModifiableTypes().clear();
        }
        int pre = types.size();

        ArrayList<JPackage> pkgsList = new ArrayList<JPackage>(packages.values());
        
        for(JType t : new ArrayList<JType>(types)){
            if(types.contains(t)){
                Random r = new Random(new Date().getTime());
                int idx = r.nextInt(pkgsList.size());
                types.removeAll(Util.deepInsertType(pkgsList.get(idx), t));
            }
        }

        types.clear();
        for(JPackage p : packages.values()){
            types.addAll(p.getTopLevelTypes());
        }
        if(pre!=types.size()){
            throw new RuntimeException("Packages randomization error.");
        }
    }

    public JType getTypeByNameOrNull(String qualName){
        for(JPackage p : packages.values()){
            JType t = p.getTypeByQualNameOrNull(qualName);
            if(t!=null){
                return t;
            }
        }
        return null;
    }

    public int getAllTypesCount(){
        int i=0;
        for(JPackage p : packages.values()){
            i+=p.getAllTypes().size();
        }
        return i;
    }

    public int getTopLevelTypesCount(){
        int i=0;
        for(JPackage p : packages.values()){
            i+= p.getTopLevelTypes().size();
        }
        return i;
    }
    
    public JPackage getOrAddPackage(JPackage pkg){
        if(!packages.containsKey(pkg.getQualName())){
            packages.put(pkg.getQualName(), pkg);
        }
        return packages.get(pkg.getQualName());
    }

    public Collection<JPackage> getPackages() {
        return packages.values();
    }

    public void cleanup(){
        for(JPackage p : new ArrayList<JPackage>(packages.values())){
            for(JType t : new ArrayList<JType>(p.getAllTypes())){
                for(JDependency dep : new ArrayList<JDependency>(t.getDependencies())){
                    if(!dep.getReferencedType().isProcessed() || dep.getReferencedType().equals(t)){
                        t.getDependencies().remove(dep);
                    }
                }
            }
        }

        for(JPackage p : new ArrayList<JPackage>(packages.values())){
            for(JType t : new ArrayList<JType>(p.getAllTypes())){
                if(!t.isProcessed()){
                    p.getModifiableTypes().remove(t.getQualName());
                }
            }
        }
        for(JPackage p : new ArrayList<JPackage>(packages.values())){
            if(p.getAllTypes().size()==0){
                packages.remove(p.getQualName());
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
