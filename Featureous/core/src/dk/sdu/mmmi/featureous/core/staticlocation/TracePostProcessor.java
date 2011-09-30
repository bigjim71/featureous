/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.staticlocation;

import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.model.TraceSet;
import dk.sdu.mmmi.featuretracer.lib.featureLocation.model.FeatureTraceModel;
import dk.sdu.mmmi.srcUtils.SrcAnalysis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import recoder.abstraction.ClassType;
import recoder.abstraction.Constructor;
import recoder.abstraction.Field;
import recoder.abstraction.Method;
import recoder.abstraction.Type;
import recoder.convenience.ForestWalker;
import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.ProgramElement;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.FieldSpecification;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.reference.ConstructorReference;
import recoder.java.reference.FieldReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.TypeReference;
import recoder.kit.MethodKit;
import recoder.kit.MiscKit;
import recoder.kit.ProblemReport;

/**
 *
 * @author ao
 */
public class TracePostProcessor extends recoder.kit.Transformation implements SrcAnalysis {

    @Override
    public ProblemReport execute() {
        ForestWalker builder = new ForestWalker(targets);
        while(builder.next()){
            builder.getProgramElement();
        }
        getServiceConfiguration().getCrossReferenceSourceInfo();
        ForestWalker iter = new ForestWalker(targets);
        while (iter.next()) {
            ProgramElement pe = iter.getProgramElement();
            if(pe instanceof MethodDeclaration || pe instanceof ConstructorDeclaration){
                String mSign = null;
                String encClassName = null;
                if(pe instanceof ConstructorDeclaration){
                    Constructor c = getCrossReferenceSourceInfo().getConstructor((ConstructorDeclaration)pe);
                    c.getProgramModelInfo();
                    c.getContainingClassType().getProgramModelInfo();
                    
                    mSign = getSignatureFromName(c);
                    encClassName = c.getContainingClassType().getFullName();
                }else{
                    Method m = getCrossReferenceSourceInfo().getMethod((MethodDeclaration)pe);
                    m.getProgramModelInfo();
                    m.getContainingClassType().getProgramModelInfo();
                    
                    mSign = getSignatureFromName(m);
                    encClassName = m.getContainingClassType().getFullName();
                }
                
                //not in map -none, 1-read only, 2-write, 3-read+write
                Map<Field, Integer> fToWrite = new HashMap<Field, Integer>();
                TreeWalker tw = new TreeWalker(pe);
                while(tw.next()){
                    ProgramElement pei = tw.getProgramElement();
                    if (pei instanceof FieldReference) {
                        FieldReference fr = (FieldReference) pei;
                        boolean isWrite = recoder.kit.ExpressionKit.isLValue(fr);
                        Field f = getCrossReferenceSourceInfo().getField(fr);
                        if(!isMissing(f)){
                            if(f instanceof FieldSpecification){
                                FieldSpecification fs = (FieldSpecification) f;
                                int curr = (fToWrite.get(fs)!=null)?fToWrite.get(fs):0;
                                if(isWrite){
                                    curr = curr|2;
                                }else{
                                    curr = curr|1;
                                }
                                fToWrite.put(fs, curr);
                            }
                        }
                    }
                }
                
                TraceSet ts = Controller.getInstance().getTraceSet();
                for(TraceModel tm : ts.getAllTraces()){
                    if(tm.getClass(encClassName)!=null && tm.getClass(encClassName).getAllMethods().contains(mSign)){
                        for(Map.Entry<Field, Integer> e : fToWrite.entrySet()){
                            String fClass = e.getKey().getContainingClassType().getFullName();
                            String pkg = e.getKey().getContainingClassType().getPackage().getFullName();
                            String encClass = (e.getKey().getContainingClassType().getContainingClassType()!=null)?
                                    e.getKey().getContainingClassType().getContainingClassType().getFullName()
                                    : null;
                            if(tm.getClass(fClass)==null){
                                dk.sdu.mmmi.featuretracer.lib.featureLocation.model.Type tmm = 
                                        new dk.sdu.mmmi.featuretracer.lib.featureLocation.model.Type(fClass, pkg, encClass);
                                ClassModel cm = new ClassModel(tmm);
                                tm.addClass(cm);
                            }
                            
                            ClassModel cm = tm.getClass(fClass);
                            String fSign = e.getKey().getType().getFullName() + " " + e.getKey().getFullName();
                            if(e.getValue()==1 || e.getValue()==3){
                                cm.addFieldRead(fSign);
//                                OutputUtil.log("In feature " + tm.getName() +" field is read: " + fSign);
                            }
                            if(e.getValue()==2 || e.getValue()==3){
                                cm.addFieldWrite(fSign);
//                                OutputUtil.log("In feature " + tm.getName() +" field is written: " + fSign);
                            }
                            if(e.getValue()!=1 && e.getValue()!=2 && e.getValue()!=3){
                                throw new RuntimeException("Error determining field access");
                            }
                        }
                    }
                }
            }
        }

        return NO_PROBLEM;
    }

    private String getSignatureFromName(Method fep) {
        StringBuilder sb = new StringBuilder();
        if (fep.getReturnType() != null && fep.getReturnType().getFullName().contains(".")) {
            sb.append(fep.getReturnType().getFullName() + " ");
        }

        if (fep instanceof Constructor) {
            sb.append(fep.getContainingClassType().getFullName() + "(");
            //At runtime constructors of inner classes get implicit param - the enclosing obj
            if (fep.getContainingClassType().getContainingClassType() != null && !fep.getContainingClassType().isStatic()) {
                sb.append(fep.getContainingClassType().getContainingClassType().getFullName() + ",");
            }
        } else {
            sb.append(fep.getFullName() + "(");
        }

        for (Type t : fep.getSignature()) {
            //TODO: generics?
            sb.append(t.getFullName());
            sb.append(",");
        }
        if (sb.lastIndexOf(",") != -1) {
            sb.replace(sb.length() - 1, sb.length(), "");
        }
        sb.append(")");
        return sb.toString();
    }

    private List<Method> findOutCalls(MethodDeclaration md, boolean skipPolymorphism, Map<Method, List<Method>> allCalls) {
        //TODO: polymorphism based on lib types...
        boolean superCallAdded = false;
        List<Method> calls = new ArrayList<Method>();
        TreeWalker mtw = new TreeWalker(md.getBody());
//        ControlFlowWalker mtw = new ControlFlowWalker(md, getCrossReferenceSourceInfo());
        while (mtw.next()) {
            ProgramElement mpe = mtw.getProgramElement();
            if (mpe instanceof ConstructorReference) {
                ConstructorReference cr = (ConstructorReference) mpe;
                Constructor cc = getCrossReferenceSourceInfo().getConstructor(cr);
                //We do not record calls to libraries
                if (!isMissing(cc) && (getCrossReferenceSourceInfo().getConstructorDeclaration(cc) != null)) {
                    calls.add(cc);
                }
            } else if (mpe instanceof MethodReference) {
                MethodReference mr = (MethodReference) mpe;
                Method mm = getCrossReferenceSourceInfo().getMethod(mr);
                //We do not record calls to libraries and abstract methods
                if (!isMissing(mm) && !mm.isAbstract() && getCrossReferenceSourceInfo().getMethodDeclaration(mm) != null) {
                    calls.add(mm);
                }

                if (!isMissing(mm) && getCrossReferenceSourceInfo().getMethodDeclaration(mm) != null) {

                    List<Method> redefs = MethodKit.getRedefiningMethods(getCrossReferenceSourceInfo(), mm);
                    if (mm.isAbstract() && redefs.size() == 1) {
                        calls.addAll(redefs);
                    } else {
                        if (!skipPolymorphism && redefs.size()>0) {
                            calls.addAll(redefs);
                        }
                    }
                }
            }
        }

        return calls;
    }

    private List<Field> findAccessedFields(MethodDeclaration md) {
        List<Field> fields = new ArrayList<Field>();
        TreeWalker mtw = new TreeWalker(md.getBody());
        while (mtw.next()) {
            ProgramElement mpe = mtw.getProgramElement();
            if (mpe instanceof FieldReference) {
                FieldReference fr = (FieldReference) mpe;
                Field ff = getCrossReferenceSourceInfo().getField(fr);
                //We do not record access to libraries
                if (!isMissing(ff)) {
                    fields.add(ff);
                }
            }
        }
        return fields;
    }

    private boolean isMissing(Method mr) {
        return mr == null || mr.getName().contains("<unknown");
    }

    private boolean isMissing(Type tt) {
        return tt == null || tt.getName().contains("<unknown");
    }

    private boolean isMissing(Field ff) {
        return ff == null || ff.getName().contains("<unknown");
    }
    private List<CompilationUnit> targets;

    @Override
    public void setScope(List<CompilationUnit> cus) {
        this.targets = cus;
    }

    private boolean isClassReferencedInTrace(ClassType containingClassType, FeatureTraceModel thinFtm) {
        for (TypeReference tr : getCrossReferenceSourceInfo().getReferences(containingClassType)) {
            TypeDeclaration td = MiscKit.getParentTypeDeclaration(tr);
            if (td != null) {
                if (thinFtm.getTypeByName(td.getFullName()) != null) {
                    return true;
                }
            }
        }
        return false;
    }
}
