/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.staticlocation;

import dk.sdu.mmmi.featureous.core.ui.OutputUtil;
import dk.sdu.mmmi.featuretracer.lib.Context;
import dk.sdu.mmmi.featuretracer.lib.featureLocation.model.FeatureTraceModel;
import dk.sdu.mmmi.featuretracer.lib.featureLocation.model.PersistenceManager;
import dk.sdu.mmmi.srcUtils.SrcAnalysis;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openide.util.Exceptions;
import recoder.abstraction.AnnotationUse;
import recoder.abstraction.ClassType;
import recoder.abstraction.Constructor;
import recoder.abstraction.Field;
import recoder.abstraction.Method;
import recoder.abstraction.Type;
import recoder.convenience.ForestWalker;
import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.ProgramElement;
import recoder.java.declaration.AnnotationElementValuePair;
import recoder.java.declaration.AnnotationUseSpecification;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.FieldSpecification;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.expression.literal.StringLiteral;
import recoder.java.reference.ConstructorReference;
import recoder.java.reference.FieldReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.SuperConstructorReference;
import recoder.java.reference.TypeReference;
import recoder.kit.MethodKit;
import recoder.kit.MiscKit;
import recoder.kit.ProblemReport;
import recoder.kit.TypeKit;

/**
 *
 * @author ao
 */
public class ControlFlowExtractor extends recoder.kit.Transformation implements SrcAnalysis {

    private String mainProjPath;

    public ControlFlowExtractor(String mainProjPath) {
        this.mainProjPath = mainProjPath;
    }

    @Override
    public ProblemReport execute() {
        Map<Method, List<Method>> outWideCalls = new HashMap<Method, List<Method>>();
        Map<Method, List<Method>> outThinCalls = new HashMap<Method, List<Method>>();
        Map<Method, List<Method>> outPolyCalls = new HashMap<Method, List<Method>>();
        Map<Method, List<Field>> fieldUsage = new HashMap<Method, List<Field>>();
        Set<Method> system = new HashSet<Method>();
        //1st pass: construct minimal certain tree 
        ForestWalker iter = new ForestWalker(targets);
        while (iter.next()) {
            ProgramElement pe = iter.getProgramElement();
            if (pe instanceof ConstructorDeclaration) {
                ConstructorDeclaration cd = (ConstructorDeclaration) pe;
                Constructor c = getCrossReferenceSourceInfo().getConstructor(cd);

                List<Method> wideCalls = findOutCalls(cd, false, outWideCalls);
                outWideCalls.put(c, wideCalls);

                List<Method> thinCalls = findOutCalls(cd, true, outThinCalls);
                outThinCalls.put(c, thinCalls);

                List<Method> polyCalls = new ArrayList<Method>();
                polyCalls.addAll(wideCalls);
                polyCalls.removeAll(thinCalls);
                outPolyCalls.put(c, polyCalls);

                List<Field> access = findAccessedFields(cd);
                fieldUsage.put(c, access);
                system.add(c);
            } else if (pe instanceof MethodDeclaration) {
                MethodDeclaration md = (MethodDeclaration) pe;
                Method m = getCrossReferenceSourceInfo().getMethod(md);

                List<Method> wideCalls = findOutCalls(md, false, outWideCalls);
                outWideCalls.put(m, wideCalls);

                List<Method> thinCalls = findOutCalls(md, true, outThinCalls);
                outThinCalls.put(m, thinCalls);

                List<Method> polyCalls = new ArrayList<Method>();
                polyCalls.addAll(wideCalls);
                polyCalls.removeAll(thinCalls);
                outPolyCalls.put(m, polyCalls);

                List<Field> access = findAccessedFields(md);
                fieldUsage.put(m, access);
                system.add(m);
            }
        }

        Map<String, FeatureTraceModel> fToWideModel = new HashMap<String, FeatureTraceModel>();
        Map<String, FeatureTraceModel> fToThinModel = new HashMap<String, FeatureTraceModel>();
        

        Set<Method> feps = findFeps(outWideCalls.keySet());
        for (Method fep : feps) {
            String fepParam = getFepParam(fep);
            FeatureTraceModel thinFtm = getFtm(fToThinModel, fepParam, fep);
            FeatureTraceModel wideFtm = getFtm(fToWideModel, fepParam, fep);

            breadthFirstModelConstr(outThinCalls, thinFtm, fep, feps, new HashSet<Method>());
            breadthFirstModelConstr(outWideCalls, wideFtm, fep, feps, new HashSet<Method>());
        }


        try {
            File stat = new File(mainProjPath, "FeatureTraces");
            stat.mkdir();
            Calendar c = Calendar.getInstance();
            String dateStamp = c.get(Calendar.YEAR) + "." + makeTwoDigit((c.get(Calendar.MONTH) + 1)) + "."
                    + makeTwoDigit(c.get(Calendar.DAY_OF_MONTH)) + "." + makeTwoDigit(c.get(Calendar.HOUR_OF_DAY))
                    + "." + makeTwoDigit(c.get(Calendar.MINUTE)) + "." + makeTwoDigit(c.get(Calendar.SECOND));
            File nt = new File(stat, "traces " + dateStamp + "[thin][s]");
            nt.mkdir();
            PersistenceManager.saveAllToDir(new ArrayList<FeatureTraceModel>(fToThinModel.values()),
                    nt.getPath());
            nt = new File(stat, "traces " + dateStamp + "[s]");
            nt.mkdir();
            PersistenceManager.saveAllToDir(new ArrayList<FeatureTraceModel>(fToWideModel.values()),
                    nt.getPath());
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        boolean add = true;
        while(add){
            add = false;
            for (Method fep : feps) {
                String fepParam = getFepParam(fep);
                FeatureTraceModel thinFtm = getFtm(fToThinModel, fepParam, fep);
                add = add || breadthFirstModelExpander(outWideCalls, outPolyCalls, thinFtm, fep, feps, new HashSet<Method>(), false);
            }
            OutputUtil.log(".");
        }
        
        OutputUtil.log("Done expanding");
        
        FeatureTraceModel ftm = createSystemFtm("System[s]", system);
        fToThinModel.put("System[s]", ftm);
        fToWideModel.put("System[s]", ftm);

        try {
            File stat = new File(mainProjPath, "FeatureTraces");
            stat.mkdir();
            Calendar c = Calendar.getInstance();
            String dateStamp = c.get(Calendar.YEAR) + "." + makeTwoDigit((c.get(Calendar.MONTH) + 1)) + "."
                    + makeTwoDigit(c.get(Calendar.DAY_OF_MONTH)) + "." + makeTwoDigit(c.get(Calendar.HOUR_OF_DAY))
                    + "." + makeTwoDigit(c.get(Calendar.MINUTE)) + "." + makeTwoDigit(c.get(Calendar.SECOND));
            File nt = new File(stat, "traces " + dateStamp + "[expanded][s]");
            nt.mkdir();
            PersistenceManager.saveAllToDir(new ArrayList<FeatureTraceModel>(fToThinModel.values()),
                    nt.getPath());
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }


        return NO_PROBLEM;
    }

    private FeatureTraceModel getFtm(Map<String, FeatureTraceModel> fToWideModel, String fepParam, Method fep) {
        FeatureTraceModel wideFtm = fToWideModel.get(fepParam);
        if (wideFtm == null) {
            wideFtm = new FeatureTraceModel(fepParam);
            fToWideModel.put(fepParam, wideFtm);
        }
        dk.sdu.mmmi.featuretracer.lib.featureLocation.model.Type fepType = constructFtmType(fep);

        if (wideFtm.getTypeByName(fepType.getQualName()) == null
                || wideFtm.getTypeByName(fepType.getQualName()).getExecutionByNamePart(fep.getFullName()) == null) {
            wideFtm.addOrUpdateExecution(null, null, null,
                    fepType, null, getSignatureFromName(fep),
                    fepParam, fep instanceof Constructor, new Context());
        }

        return wideFtm;
    }
    
    private FeatureTraceModel createSystemFtm(String name, Set<Method> ms){

        FeatureTraceModel ftm = new FeatureTraceModel(name);
        for(Method m : ms){
            if(m.isAbstract() || m.getContainingClassType().isInner() || !m.getContainingClassType().isOrdinaryClass()
                    || m.getName() == null || m.getContainingClassType().getName() == null
                    || Character.isDigit(m.getContainingClassType().getName().charAt(0)) 
                    || Character.isDigit(m.getName().charAt(0))){
                continue;
            }
            dk.sdu.mmmi.featuretracer.lib.featureLocation.model.Type fepType = constructFtmType(m);
            ftm.addOrUpdateExecution(null, null, null,
                    fepType, null, getSignatureFromName(m),
                    null, m instanceof Constructor, new Context());
        }
        return ftm;
    }

    private void handleImplicitSuperConstrs(Constructor cc, Map<Method, List<Method>> allCalls) {
        if (!isMissing(cc) && getCrossReferenceSourceInfo().getConstructorDeclaration(cc)==null) {

            // Handle def no-arg implicit constr cc
            ClassType ct = TypeKit.getSuperClass(getNameInfo(), cc.getContainingClassType());
            if (!isMissing(ct) && getCrossReferenceSourceInfo().getTypeDeclaration(ct) != null) {
                Constructor dc = getServiceConfiguration().getImplicitElementInfo().getDefaultConstructor(ct);
                List<Method> ccc = new ArrayList<Method>();
                ccc.add(dc);
                allCalls.put(cc, ccc);
                OutputUtil.log("Added super call: " + dc.getFullName());
                handleImplicitSuperConstrs(dc, allCalls);
            }
        }
    }

    private String makeTwoDigit(int d) {
        if (d < 10) {
            return "0" + d;
        } else {
            return "" + d;
        }
    }

    private void breadthFirstModelConstr(Map<Method, List<Method>> outCalls, FeatureTraceModel ftm, Method current, Set<Method> feps, Set<Method> added) {

        if (added.contains(current)) {
            return;
        }

        added.add(current);

        if (!outCalls.containsKey(current)) {
            return;
        }
        dk.sdu.mmmi.featuretracer.lib.featureLocation.model.Type currType = constructFtmType(current);



        for (Method ocm : outCalls.get(current)) {

            dk.sdu.mmmi.featuretracer.lib.featureLocation.model.Type calledType = constructFtmType(ocm);

            String feat = null;
            if (feps.contains(ocm)) {
                feat = getFepParam(ocm);
            }

            ftm.addOrUpdateExecution(currType, null, getSignatureFromName(current),
                    calledType, null, getSignatureFromName(ocm),
                    feat, ocm instanceof Constructor, new Context());
        }

        for (Method ocm : outCalls.get(current)) {
            if (ocm != null) {
                breadthFirstModelConstr(outCalls, ftm, ocm, feps, added);
            }
        }
    }

    private boolean breadthFirstModelExpander(Map<Method, List<Method>> outWideCalls,
            Map<Method, List<Method>> outPolyCalls,
            FeatureTraceModel thinFtm, Method current, Set<Method> feps, Set<Method> added, boolean add) {

        if (added.contains(current)) {
            return false;
        }

        added.add(current);

        if (!outWideCalls.containsKey(current)) {
            return false;
        }
        
        dk.sdu.mmmi.featuretracer.lib.featureLocation.model.Type currType = constructFtmType(current);
        if(thinFtm.getTypeByName(currType.getQualName())!=null){
            currType = thinFtm.getTypeByName(currType.getQualName());
        }

        List<Method> outCallsToFollow = new ArrayList<Method>();

        for (Method ocm : outWideCalls.get(current)) {

            if (outPolyCalls.containsKey(current) && outPolyCalls.get(current).contains(ocm)) {
                // ocm is a candidate for adding

                boolean referenced = isClassReferencedInTrace(ocm.getContainingClassType(), thinFtm);

                //Follow if we add it
                if (referenced) {
                    outCallsToFollow.add(ocm);
                    String feat = null;
                    if (feps.contains(ocm)) {
                        feat = getFepParam(ocm);
                    }
                    dk.sdu.mmmi.featuretracer.lib.featureLocation.model.Type calledType = constructFtmType(ocm);
                    if(thinFtm.getTypeByName(calledType.getQualName())==null 
                            || thinFtm.getTypeByName(calledType.getQualName()).getExecutionByNamePart(getSignatureFromName(ocm))==null){
                        thinFtm.addOrUpdateExecution(currType, null, getSignatureFromName(current),
                                calledType, null, getSignatureFromName(ocm),
                                feat, ocm instanceof Constructor, new Context());
                        add = true;
                        OutputUtil.log("resolved poly: " + getSignatureFromName(ocm));
                    }
                }
            } else {
                //not a poly call - follow
                outCallsToFollow.add(ocm);
            }
        }

        for (Method ocm : outCallsToFollow) {
            if (ocm != null) {
                add = add || breadthFirstModelExpander(outWideCalls, outPolyCalls, thinFtm, ocm, feps, added, add);
            }
        }
        
        return add;
    }

    private dk.sdu.mmmi.featuretracer.lib.featureLocation.model.Type constructFtmType(Method fep) {
        dk.sdu.mmmi.featuretracer.lib.featureLocation.model.Type fepType = new dk.sdu.mmmi.featuretracer.lib.featureLocation.model.Type(
                fep.getContainingClassType().getFullName(),
                fep.getContainingClassType().getPackage().getFullName(),
                (fep.getContainingClassType().getContainingClassType() != null)
                ? fep.getContainingClassType().getContainingClassType().getFullName() : null);
        return fepType;
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
                    if (cr instanceof SuperConstructorReference) {
                        superCallAdded = true;
                    }
                    handleImplicitSuperConstrs(cc, allCalls);
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
                            OutputUtil.log("Addid poly calls: " + redefs.size());
                            calls.addAll(redefs);
                        }
                    }
                }
            }
        }

        if (md instanceof ConstructorDeclaration && !superCallAdded) {
            TypeDeclaration td = MiscKit.getParentTypeDeclaration(md);
            if (td instanceof ClassType && ((ClassType)td).getBaseClassType()!=null) {
                ClassType ct = ((ClassType)td).getBaseClassType();
                if (!isMissing(ct) && getCrossReferenceSourceInfo().getTypeDeclaration(ct) != null) {
                    Constructor c = getServiceConfiguration().getImplicitElementInfo().getDefaultConstructor(ct);
                    calls.add(c);
                    handleImplicitSuperConstrs(c, allCalls);
                }
            }
        }

        return calls;
    }

    private Set<Method> findFeps(Set<Method> ms) {
        Set<Method> feps = new HashSet<Method>();
        for (Method mm : ms) {
            if (!isDeclAvail(mm)) {
                continue;
            }
            for (AnnotationUse au : mm.getAnnotations()) {
                if (au instanceof AnnotationUseSpecification) {
                    AnnotationUseSpecification aus = (AnnotationUseSpecification) au;
                    String annName = aus.getTypeReference().getName();
                    if (annName.contains("FeatureEntryPoint")) {
                        feps.add(mm);
                    }
                }
            }
        }
        return feps;
    }

    private boolean isDeclAvail(Method m) {
        if (m instanceof Constructor) {
            return getCrossReferenceSourceInfo().getConstructorDeclaration((Constructor) m) != null;
        } else {
            return getCrossReferenceSourceInfo().getMethodDeclaration(m) != null;
        }
    }

    private String getFepParam(Method mm) {
        for (AnnotationUse au : mm.getAnnotations()) {
            if (au instanceof AnnotationUseSpecification) {
                AnnotationUseSpecification aus = (AnnotationUseSpecification) au;
                String annName = aus.getTypeReference().getName();
                if (annName.contains("FeatureEntryPoint")) {
                    for (AnnotationElementValuePair evp : aus.getElementValuePairs()) {
                        ProgramElement child = evp.getChildAt(evp.getChildCount() - 1);
                        if (child instanceof FieldReference) {
                            FieldReference fr = (FieldReference) child;
                            FieldSpecification fs = (FieldSpecification) getCrossReferenceSourceInfo().getField(fr);
                            StringLiteral sl = (StringLiteral) fs.getChildAt(1);
                            return sl.getValue().replaceAll("\"", "") + "[s]";
                        } else {
                            return ((String) evp.getValue()).replaceAll("\"", "") + "[s]";
                        }
                    }
                }
            }
        }

        throw new RuntimeException("No FEP annotation found on " + mm.getFullName());
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
