/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.model;

import dk.sdu.mmmi.featuretracer.lib.featureLocation.model.FeatureTraceModel;
import dk.sdu.mmmi.featuretracer.lib.featureLocation.model.Invocation;
import dk.sdu.mmmi.featuretracer.lib.featureLocation.model.Type;
import dk.sdu.mmmi.featureous.core.controller.Controller;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TraceModel implements Comparable<TraceModel> {

    private final String name;
    private HashMap<String, ClassModel> classes;
    private Set<TraceModel> subTraces;
    private Set<String> createdInstances;
    private Set<OrderedBinaryRelation<String, Integer>> interTypeInvocations;
    private Set<OrderedBinaryRelation<String, Integer>> methodInvocations;
    private final String srcFilePath;

    public TraceModel(FeatureTraceModel model, String srcFilePath) {
        this(model, new HashSet(), srcFilePath);
    }

    public TraceModel(FeatureTraceModel model, Set<TraceModel> subTraces, String srcFilePath) {
        this.srcFilePath = srcFilePath;
        this.name = model.getFeatureID();
        this.classes = new HashMap<String, ClassModel>();
        this.subTraces = new HashSet<TraceModel>();
        this.interTypeInvocations = new HashSet<OrderedBinaryRelation<String, Integer>>();
        this.methodInvocations = new HashSet<OrderedBinaryRelation<String, Integer>>();
        this.createdInstances = new HashSet<String>();

        for(Type t : model.getTypes()){
            this.addClass(new ClassModel(t));
        }

        for(ClassModel c : this.getClassSet()){
            for(String i : c.getInstancesCreated()){
                createdInstances.add(i);
            }
        }

        for(Invocation e : model.getInvocations()){
            if(e.getCaller()!=null && e.getCalled()!=null){
                boolean callerValid = this.getClass(e.getCaller().getContainingType())!=null
                        && this.getClass(e.getCaller().getContainingType()).getAllMethods().contains(e.getCaller().getSignature());
                boolean calledValid = this.getClass(e.getCalled().getContainingType())!=null
                        && this.getClass(e.getCalled().getContainingType()).getAllMethods().contains(e.getCalled().getSignature());
                if(callerValid && calledValid){
                    interTypeInvocations.add(new OrderedBinaryRelation<String, Integer>(
                            e.getCaller().getContainingType(), e.getCalled().getContainingType(), 1));
                    methodInvocations.add(new OrderedBinaryRelation<String, Integer>(
                            e.getCaller().getSignature(),
                            e.getCalled().getSignature(),
                            1));
                }else{
                    // Currently these will be: invocations with inner classes
                    //OutputUtil.log("Found not valid: " + e.toString());
                }
            }
        }

        this.subTraces = subTraces;

        updateGroupTrace();
    }

    public String getSrcFilePathOrNull() {
        return srcFilePath;
    }

    public String getName() {
        return name;
    }

    public Set<OrderedBinaryRelation<String, Integer>> getInterTypeInvocations() {
        return interTypeInvocations;
    }

    public Set<OrderedBinaryRelation<String, Integer>> getMethodInvocations() {
        return methodInvocations;
    }

    public boolean hasCreated(String instanceId) {
        return getCreatedInstances().contains(instanceId);
    }

    public void addClass(ClassModel classModel) {
        if (!classModel.getName().contains("$")) {
            if (classes.containsKey(classModel.getName())) {
                classes.get(classModel.getName()).mergeFromOther(classModel);
            } else {
                classes.put(classModel.getName(), classModel);
            }
        }
    }

    public void removeClass(String className){
        if(hasSubTraces()){
            throw new RuntimeException("Not allowed for group traces");
        }
        classes.remove(className);
    }

    public void removeDeepClass(String className){
        Set<OrderedBinaryRelation<String, Integer>> invs = new HashSet();
        invs.addAll(interTypeInvocations);

        ClassModel m = getClass(className);
        if(m!=null){
            for(OrderedBinaryRelation<String, Integer> i : invs){
                if(m.getAllMethods().contains(i.getFirst()) || m.getAllMethods().contains(i.getSecond())){
                    interTypeInvocations.remove(i);
                }
            }
        }
        classes.remove(className);
        
        for(TraceModel s : subTraces){
            s.removeDeepClass(className);
        }
        updateGroupTrace();
    }

    public HashMap<String, ClassModel> getClasses() {
        return classes;
    }

    public Set<String> getCreatedInstances() {
        return createdInstances;
    }

    public Set<ClassModel> getClassSet() {
        Set<ClassModel> classList = new HashSet<ClassModel>();
        classList.addAll(classes.values());
        return classList;
    }
    
    public Set<String> getPackageNames(){
        Set<String> pkgs = new HashSet<String>();
        for(ClassModel cm : getClassSet()){
            pkgs.add(cm.getPackageName());
        }
        return pkgs;
    }

    public int getNrOfMethods() {
        int nrOfMethods = 0;
        for (ClassModel classModel : classes.values()) {
            nrOfMethods += classModel.getAllMethods().size();
        }
        return nrOfMethods;
    }

    public int getNrOfCreatedInstances() {
        if (!hasSubTraces()) {
            return createdInstances.size();
        } else {
            int nrOfCreatedInstances = 0;
            for (TraceModel traceModel : subTraces) {
                nrOfCreatedInstances += traceModel.createdInstances.size();
            }
            return nrOfCreatedInstances;
        }
    }

    public int getNrOfInstances() {
        int nrOfInstances = 0;
        for (ClassModel classModel : classes.values()) {
            nrOfInstances += classModel.getInstancesUsed().size();
        }
        return nrOfInstances;
    }

    public boolean hasClass(String className) {
        return classes.containsKey(className);
    }

    public ClassModel getClass(String className) {
        return classes.get(className);
    }

    public int shareObjects(TraceModel traceModel) {
        int count =0;
        for (ClassModel classModel : traceModel.getClassSet()) {
            if (classes.containsKey(classModel.getName())) {
                count += classModel.hasSameInstanceUsed(classes.get(classModel.getName()));
            }
        }
        return count;
    }

    public Set<String> shareObjectsClasswise(TraceModel traceModel) {
        Set<String> count = new HashSet<String>();
        for (ClassModel classModel : traceModel.getClassSet()) {
            if (classes.containsKey(classModel.getName())) {
                if(classModel.hasSameInstanceUsed(classes.get(classModel.getName()))>0){
                    count.add(classModel.getName());
                }
            }
        }
        return count;
    }

    public int dependsOn(TraceModel traceModel) {
        Controller c = Controller.getInstance();
        int count = 0;
        for(ClassModel cm : getClassSet()){
            for (String instance : cm.getInstancesUsed()) {
                if (traceModel.hasCreated(instance) && !this.hasCreated(instance)) {
                    count++;
                }
            }
        }
        return count;
    }

    public Set<String> dependsOnClasswise(TraceModel traceModel) {
        Controller c = Controller.getInstance();
        Set<String> count = new HashSet<String>();
        for(ClassModel cm : getClassSet()){
            for (String instance : cm.getInstancesUsed()) {
                if (traceModel.hasCreated(instance) && !this.hasCreated(instance)) {
                    count.add(cm.getName());
                    break;
                }
            }
        }
        return count;
    }

    public boolean dependsOn(TraceModel traceModel, String type) {
        if(this.getClass(type)==null){
            throw new RuntimeException("Trace " + this.getName() + " has no type: " + type);
        }
        for (String instance : getClass(type).getInstancesUsed()) {
            if (traceModel.hasCreated(instance) && !this.hasCreated(instance)) {
                return true;
            }
        }
        return false;
    }

    public Collection<String> getAllInstances() {
        HashSet<String> allInstances = new HashSet<String>();
        for (ClassModel classModel : getClassSet()) {
            allInstances.addAll(classModel.getInstancesUsed());
        }
        return allInstances;
    }

    public boolean hasSubTraces() {
        return subTraces!=null && subTraces.size()>0;
    }

    public Set<TraceModel> getChildren() {
        return subTraces;
    }

    public boolean isAncestorOrSameAsTrace(String trace){
        if(this.getName().equals(trace)){
            return true;
        }
        if(!hasSubTraces()){
            return false;
        }
        boolean parent = false;
        for(TraceModel st : getChildren()){
            if(!parent){
                parent |= st.isAncestorOrSameAsTrace(trace);
            }
        }
        return parent;
    }

    @Override
    public int compareTo(TraceModel model) {
        return getName().compareTo(model.getName());
    }

    public String toString() {
        return getName() /*+ ":\n\t" + classes*/;
    }

    public boolean equals(Object o) {
        if (o instanceof TraceModel) {
            TraceModel model = (TraceModel) o;
            return getName().equals(model.getName());
        } else {
            return super.equals(o);
        }
    }

    public void updateGroupTrace() {
        if(hasSubTraces()){

            this.createdInstances = new HashSet<String>();
            this.classes = new HashMap<String, ClassModel>();

            for (TraceModel traceModel : subTraces) {
                for (ClassModel classModel : traceModel.getClassSet()) {
                    addClass(classModel);
                }
            }

            for(ClassModel c : this.getClassSet()){
                for(String i : c.getInstancesCreated()){
                    createdInstances.add(i);
                }
            }

        }
    }
}
