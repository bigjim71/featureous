/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.model;

import dk.sdu.mmmi.featuretracer.lib.featureLocation.model.Execution;
import dk.sdu.mmmi.featuretracer.lib.featureLocation.model.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClassModel implements Comparable<ClassModel> {

    private String name;
    private String packageName;
    private Set<String> instancesUsed;
    private Set<String> instancesCreated;
    private Set<String> allExecutables;
    private Set<String> feps;
    private Set<String> constructors;
    private Set<String> fieldWrites;
    private Set<String> fieldReads;

    public ClassModel(Type model) {
        this.name = model.getQualName();
        this.packageName = model.getPackageName();
        this.instancesUsed = new HashSet<String>();
        this.instancesCreated = new HashSet<String>();
        this.allExecutables = new HashSet<String>();
        this.fieldReads = new HashSet<String>();
        this.fieldWrites = new HashSet<String>();
        this.feps = new HashSet<String>();
        this.constructors = new HashSet<String>();

        for(String i : model.getInstancesCreated()){
            this.addInstanceCreated(i);
        }

        instancesUsed.addAll(instancesCreated);
        for(String i : model.getInstancesUsed()){
            this.addInstanceUsed(i);
        }


        for(Execution e : model.getExecutions()){
            //Skip method if name begins with digit
            String sign = e.getSignature();
            String name = sign.substring(0, sign.indexOf("("));
            int nameDot = name.lastIndexOf(".");
            char fl = name.charAt(nameDot+1);
            if(Character.isDigit(fl)){
                continue;
            }
            
            this.addMethod(e.getSignature());
            if(e.isConstructor()){
                addConstructor(e.getSignature());
            }
            if(e.isFeatureEntryPoint()){
                addFep(e.getSignature());
            }
        }
    }

    public int hasSameInstanceUsed(ClassModel classModel) {
        int count = 0;
        for (String instanceId : classModel.getInstancesUsed()) {
            if (getInstancesUsed().contains(instanceId)) {
                count ++;
            }
        }
        return count;
    }

    public boolean isFep(String method){
        return feps.contains(method);
    }

    public boolean isConstructor(String method){
        return constructors.contains(method);
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public Set<String> getInstancesUsed() {
        return instancesUsed;
    }

    public Set<String> getInstancesCreated() {
        return instancesCreated;
    }

    public void addInstanceUsed(String instanceId) {
        if (!instancesUsed.contains(instanceId)) {
            instancesUsed.add(instanceId);
        }
    }

    public void addInstanceCreated(String instanceId) {
        if (!instancesCreated.contains(instanceId)) {
            instancesCreated.add(instanceId);
        }
    }

    public void addMethod(String method) {
        if (!allExecutables.contains(method)) {
            allExecutables.add(method);
        }
    }

    public void addFep(String method) {
        if (!feps.contains(method)) {
            feps.add(method);
        }
    }

    public void addConstructor(String method) {
        if (!constructors.contains(method)) {
            constructors.add(method);
        }
    }

    public Set<String> getAllMethods() {
        return allExecutables;
    }

    public void mergeFromOther(ClassModel other){
        if(!other.getName().equals(this.name)){
            throw new RuntimeException();
        }
        this.instancesCreated.addAll(other.getInstancesCreated());
        this.instancesUsed.addAll(other.getInstancesUsed());
        this.allExecutables.addAll(other.getAllMethods());
        this.feps.addAll(other.feps);
        this.constructors.addAll(other.constructors);
    }
//	public boolean isSpecific(TraceModel traceModel) {
//		for (TraceModel traceM : traces) {
//			if(!traceM.equals(traceModel))
//				return false;
//		}
//		return true;
//	}
    @Override
    public int compareTo(ClassModel model) {
        return getName().compareTo(model.getName());
    }

    public String toString() {
        return getName() /*+ ":\n\t" + traces*/;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    public boolean equals(Object o) {
        if (o instanceof ClassModel) {
            ClassModel model = (ClassModel) o;
            return getName().equals(model.getName());
        } else {
            return super.equals(o);
        }
    }

    public void addFieldWrite(String fSign) {
        this.fieldWrites.add(fSign);
    }

    public void addFieldRead(String fSign) {
        this.fieldReads.add(fSign);
    }

    public Set<String> getFieldReads() {
        return fieldReads;
    }

    public Set<String> getFieldWrites() {
        return fieldWrites;
    }
    
    public Set<String> getAllFields(){
        Set<String> af = new HashSet<String>();
        af.addAll(getFieldReads());
        af.addAll(getFieldWrites());
        return af;
    }
}
