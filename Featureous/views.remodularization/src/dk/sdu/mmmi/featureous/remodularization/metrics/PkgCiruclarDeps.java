/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.metrics;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.metrics.AbstractMetric;
import dk.sdu.mmmi.srcUtils.sdm.model.JDependency;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ao
 */
public class PkgCiruclarDeps extends AbstractMetric{

    private StaticDependencyModel sdm;
    private Set<Set<JPackage>> foundCycles = new HashSet<Set<JPackage>>();

    public PkgCiruclarDeps() {
        super("No. of circular deps among packages", AbstractMetric.Scope.SYSTEM);
    }
    
    @Override
    public void calculateAll(Set<TraceModel> ftms, StaticDependencyModel sdm) {
        this.sdm = sdm;
        for(JPackage pkg : sdm.getPackages()){
            for(JPackage dep : getOtherDependentPkgs(pkg)){
                visitDep(pkg, dep, new HashSet<JPackage>());
            }
        }
        
        Set<Set<JPackage>> toRemove = new HashSet<Set<JPackage>>();
        
        for(Set<JPackage> cycle : foundCycles){
            if(toRemove.contains(cycle)){
                continue;
            }
            for(Set<JPackage> cycle2 : foundCycles){
                if(cycle != cycle2 
                        && cycle.size() == cycle2.size()
                        && cycle.containsAll(cycle2)
                        && cycle2.containsAll(cycle)){
                    toRemove.add(cycle2);
                }
            }
        }
        
        foundCycles.removeAll(toRemove);
        
        setResultForSubject(foundCycles.size(), "system");
    }
    
    private void visitDep(JPackage starting, JPackage current, Set<JPackage> visited){
        if(current == starting){
            visited.add(current);
            foundCycles.add(visited);
            return;
        }
        if(visited.contains(current)){
            return;
        }
        
        visited.add(current);
        
        int sum = 0;
        
        for(JPackage dep : getOtherDependentPkgs(current)){
            visitDep(starting, dep, visited);
        }
    }
    
    private Set<JPackage> getOtherDependentPkgs(JPackage p){
        Set<JPackage> pkgs = new HashSet<JPackage>();
        for(JType t : p.getAllTypes()){
            for(JDependency dep : t.getDependencies()){
                JPackage pkg = dk.sdu.mmmi.srcUtils.sdm.model.Util.getTypesPackage(dep.getReferencedType(), sdm);
                if(pkg==null){
                    continue;
                }
                pkgs.add(pkg);
            }
        }
        
        pkgs.remove(p);
        return pkgs;
    }

    @Override
    public float getResult() {
        return getSumVal();
    }

}
