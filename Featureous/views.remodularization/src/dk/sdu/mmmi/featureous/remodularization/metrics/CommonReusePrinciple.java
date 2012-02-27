/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.metrics;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.ui.OutputUtil;
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
public class CommonReusePrinciple extends AbstractMetric{

    // Classes that are not reused together should not be packaged together
    
    public CommonReusePrinciple() {
        super("Common reuse principle", AbstractMetric.Scope.PACKAGE);
    }
    
    @Override
    public void calculateAll(Set<TraceModel> ftms, StaticDependencyModel sdm) {
        
        for(JPackage pkg : sdm.getPackages()){
            if(pkg.getAllTypes().size()==0){
                continue;
            }
            
            Set<JPackage> depPkgs = new HashSet<JPackage>();
            for(JType t : pkg.getAllTypes()){
                for(JType depType : t.getIncomingDeps()){
                    JPackage depPkg = dk.sdu.mmmi.srcUtils.sdm.model.Util.getTypesPackage(depType, sdm);
                    depPkgs.add(depPkg);
                }
            }
            
            depPkgs.remove(pkg);
            depPkgs.remove(null);
            
            float degreeOfUsage = 0f;
            
            for(JPackage depPkg : depPkgs){
                Set<JType> reffedTypes = new HashSet<JType>();
                for(JType tt : depPkg.getAllTypes()){
                    for(JDependency dep : tt.getDependencies()){
                        JPackage pkg2 = dk.sdu.mmmi.srcUtils.sdm.model.Util.getTypesPackage(dep.getReferencedType(), sdm);
                        if(pkg2!=null && pkg2.getQualName().equals(pkg.getQualName())){
                            reffedTypes.add(dep.getReferencedType());
                        }
                    }
                }
                if(reffedTypes.size()>pkg.getAllTypes().size()){
                    OutputUtil.log("Bug in common reuse principle");
                }
                degreeOfUsage+=reffedTypes.size()/pkg.getAllTypes().size();
            }
            
            if(depPkgs.size()>0){
                setResultForSubject(degreeOfUsage/depPkgs.size(), pkg.getQualName());
            }
        }
    }

    @Override
    public float getResult() {
        return getMeanVal();
    }
}
