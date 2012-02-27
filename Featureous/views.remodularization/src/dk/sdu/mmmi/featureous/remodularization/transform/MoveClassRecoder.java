package dk.sdu.mmmi.featureous.remodularization.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import recoder.abstraction.ClassType;
import recoder.abstraction.Package;
import recoder.abstraction.Type;
import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.Import;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.reference.PackageReference;
import recoder.java.reference.TypeReference;
import recoder.kit.MiscKit;
import recoder.kit.PackageKit;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;
import recoder.kit.TypeKit;
import recoder.kit.UnitKit;
import recoder.kit.transformation.RemoveUnusedImports;

/**
 *
 * @author ao
 */
public class MoveClassRecoder extends TwoPassTransformation {

    private final String typeName;
    private final String destPkg;
    private CompilationUnit srcCompUnit;
    private TypeDeclaration typeDecl;
    private Type srcType;
    List<TypeReference> typeInRefs = new ArrayList<TypeReference>();
    Map<TypeReference, Type> typeOutRefs = new IdentityHashMap<TypeReference, Type>();
    Map<TypeReference, TypeReference> newToOrgRefs = new IdentityHashMap<TypeReference, TypeReference>();
    Map<TypeReference, TypeReference> orgToNewRefs = new IdentityHashMap<TypeReference, TypeReference>();
    Map<TypeReference, Type> typeOutRefs2 = new IdentityHashMap<TypeReference, Type>();

    public MoveClassRecoder(String typeName, String destPkg) {
        this.typeName = typeName;
        this.destPkg = destPkg;
    }

    @Override
    public ProblemReport analyze() {
        System.out.println("***** refactoring: " + typeName);
        srcType = getNameInfo().getType(typeName);
        typeDecl = (TypeDeclaration) getNameInfo().getClassType(typeName);
        srcCompUnit = UnitKit.getCompilationUnit(typeDecl);
        TreeWalker w = new TreeWalker(srcCompUnit);
        while (w.next()) {
            if (w.getProgramElement() instanceof TypeDeclaration) {
                TypeDeclaration typeDecl2 = (TypeDeclaration) w.getProgramElement();
                Type atype = getNameInfo().getArrayType(typeDecl2);
                for (CompilationUnit cu : getSourceFileRepository().getCompilationUnits()) {
                    if (cu.getID() != srcCompUnit.getID()) {
                        typeInRefs.addAll(TypeKit.getReferences(getCrossReferenceSourceInfo(), typeDecl2, cu, false));
                        if (atype != null) {
                            typeInRefs.addAll(getCrossReferenceSourceInfo().getReferences(atype));
                        }
                    }
                }
            }
        }

        return setProblemReport(EQUIVALENCE);
    }

    @Override
    public void transform() {
        super.transform();

        TreeWalker w = new TreeWalker(typeDecl);
        while (w.next()) {
            Type type = null;
            TypeReference ref = null;
            if (w.getProgramElement() instanceof TypeReference) {
                ref = (TypeReference) w.getProgramElement();
                type = getCrossReferenceSourceInfo().getType(ref);
                if (type instanceof ClassType) {
                    TypeDeclaration td = getCrossReferenceSourceInfo().getTypeDeclaration((ClassType) type);
                    if (type != null && td != null) {
                        if (!td.getFullSignature().equals(typeDecl.getFullSignature()) && UnitKit.getCompilationUnit(td)!=UnitKit.getCompilationUnit(typeDecl)
                                && typeDecl.getPackage().getFullName().equals(td.getPackage().getFullName())) {
                            typeOutRefs.put(ref, type);
                        }
                    }
                }
            }
        }

        //move
        PackageReference oldPr = PackageKit.createPackageReference(getProgramFactory(), typeDecl.getPackage());

        CompilationUnit srcCu = UnitKit.getCompilationUnit(typeDecl);

        Package p = getNameInfo().getPackage(destPkg);
        if (p == null) {
            p = getNameInfo().createPackage(destPkg);
        }

        PackageReference newPr = PackageKit.createPackageReference(getProgramFactory(), p);
        replace(srcCu.getPackageSpecification(), getProgramFactory().createPackageSpecification(newPr));

        getChangeHistory().updateModel();

        MiscKit.unindent(oldPr);
        replaceTypeRefs(oldPr, typeOutRefs.keySet());
        MiscKit.unindent(newPr);
        replaceTypeRefs(newPr, typeInRefs);

//        ASTList<Import> imps = new ASTArrayList<Import>();
//
//        TreeWalker w = new TreeWalker(typeDecl);
//        while (w.next()) {
//            Type type = null;
//            TypeReference ref = null;
//         /*   if (w.getProgramElement() instanceof TypeArgument) {
//                w.next();
////                TypeReference rr = (TypeReference) w.getProgramElement();
////                ref = rr;
////                type = getNameInfo().getType(rr.getName());
//            } else*/ if (w.getProgramElement() instanceof TypeReference) {
//                ref = (TypeReference) w.getProgramElement();
//                type = getCrossReferenceSourceInfo().getType(ref);
//                if (type != null) {
////                    if(tt instanceof ParameterizedType){
////                        ParameterizedType pt = (ParameterizedType) tt;
////                        tt = pt.getBaseClassType();
////                    }
//                    if (!type.getFullSignature().equals(typeDecl.getFullSignature())) {
//                        typeOutRefs.put(ref, type);
//                    }
//                }
//            }
//
//            if (type != null) {
//                Type ttt = type;
//                if(type instanceof ParameterizedType){
//                    ParameterizedType pt = (ParameterizedType) type;
//                    ttt = pt.getBaseClassType();
//                }
//                ref = TypeKit.createTypeReference(getProgramFactory(), ttt);
//                boolean contains = false;
//                for (Import i : imps) {
//                    if (i.getTypeReference().toString().equals(ref.toString())) {
//                        contains = true;
//                        break;
//                    }
//                }
//                if (!contains && !srcType.getFullName().equals(ttt.getFullName())) {
//                    imps.add(new Import(ref.deepClone(), false));
//                }
//            }
//        }
//
//        Package p = getNameInfo().getPackage(destPkg);
//        if (p == null) {
//            p = getNameInfo().createPackage(destPkg);
//        }
//
//        TypeDeclaration destTypeDecl = typeDecl.deepClone();
//        ASTList<TypeDeclaration> pkgTypeDecls = new ASTArrayList<TypeDeclaration>();
////        for(ClassType ct : p.getTypes()){
////            pkgTypeDecls.add((TypeDeclaration)ct);
////        }
//        if (pkgTypeDecls.contains(destTypeDecl)) {
//            throw new RuntimeException("Attempting to add the same type twice!");
//        }
//        pkgTypeDecls.add(destTypeDecl);
//        PackageReference pr = PackageKit.createPackageReference(getProgramFactory(), p);
//        CompilationUnit destCu = UnitKit.getCompilationUnit(pr);
//        imps.addAll(UnitKit.getCompilationUnit(typeDecl).getImports().deepClone());
//        if (destCu == null) {
//            destCu = getProgramFactory().createCompilationUnit(new PackageSpecification(pr),
//                    imps, pkgTypeDecls);
//            attach(destCu);
//        } else {
//            destCu.setDeclarations(pkgTypeDecls);
//        }
//
//        destTypeDecl.setParent(destCu);
//
//        ASTArrayList<TypeReference> refsArray = new ASTArrayList<TypeReference>();
//        TreeWalker w2 = new TreeWalker(destTypeDecl);
//        while (w2.next()) {
//            if (w2.getProgramElement() instanceof TypeReference) {
//                TypeReference newRef = (TypeReference) w2.getProgramElement();
//                for (TypeReference orgRef : typeOutRefs.keySet()) {
//                    if (orgRef.toString().equals(newRef.toString())) {
//                        Type t = typeOutRefs.get(orgRef);
//                        typeOutRefs2.put(newRef, t);
//                        newToOrgRefs.put(newRef, orgRef);
//                        orgToNewRefs.put(orgRef, newRef);
//                        refsArray.add(newRef);
//                        break;
//                    }
//                }
//            }
//        }
//
//        for (TypeReference ref : refsArray) {
//            Type t = typeOutRefs2.get(ref);
//            TypeReference tr = TypeKit.createTypeReference(
//                    getSourceInfo(),
//                    t,
//                    destCu);
//
////            if (t instanceof ParameterizedType) {
////                ASTList<TypeArgumentDeclaration> tads = new ASTArrayList<TypeArgumentDeclaration>();
////                for (TypeArgument ta : newToOrgRefs.get(ref).getTypeArguments()) {
////                    Type tt = getNameInfo().getType(ta.getTypeName());
////                    TypeArgumentDeclaration tad =
////                            getProgramFactory().createTypeArgumentDeclaration(
////                            TypeKit.createTypeReference(getSourceInfo(),
////                            tt,
////                            destTypeDecl));
////                    tads.add(tad);
////                }
////                tr.setTypeArguments(tads);
////                tr.makeParentRoleValid();
////            }
//            replace(ref, tr);
//        }
//        
//        getChangeHistory().updateModel();
//        
//        for (TypeReference ref : typeInRefs) {
//            getProgramFactory();
//            TypeReference tr = TypeKit.createTypeReference(getProgramFactory(), destTypeDecl);
//            replace(ref, tr);
//        }
//        destTypeDecl.makeParentRoleValid();
//
//        CompilationUnit srcCompUnit = UnitKit.getCompilationUnit(typeDecl);
//        detach(srcCompUnit);

//        getChangeHistory().updateModel();
//
//        RemoveUnusedImports rui = new RemoveUnusedImports(getServiceConfiguration(), getSourceFileRepository().getCompilationUnits());
//        rui.analyze();
//        rui.transform();

        getChangeHistory().updateModel();

        removeDoubledImports();

        getChangeHistory().updateModel();

//        IncreaseVisibilityToPublic incVis = new IncreaseVisibilityToPublic();
//        incVis.setServiceConfiguration(getServiceConfiguration());
//        incVis.analyze();
//        incVis.transform();
//        
//        getChangeHistory().updateModel();
    }

    private void replaceTypeRefs(PackageReference newPr, Collection<TypeReference> refs) {
        for (TypeReference tr : refs) {
            if (tr != null) {
                if (tr.getPackageReference() != null) {
                    replace(tr.getPackageReference(), newPr.deepClone());
                } else {
                    TypeReference trr = tr.deepClone();
                    attach(newPr.deepClone(), trr);
                    trr.makeAllParentRolesValid();

                    if (tr.getDimensions() > 0) {
                        trr.setDimensions(0);
                    }
                    MiscKit.unindent(trr);
                    Import imp = getProgramFactory().createImport(trr, false);
                    UnitKit.getCompilationUnit(tr).getImports().add(imp);
                    UnitKit.getCompilationUnit(tr).makeAllParentRolesValid();
                }
            }
        }
    }

    private void removeDoubledImports() {
        for (CompilationUnit cu : getSourceFileRepository().getCompilationUnits()) {
            Set<String> refedTypes = new HashSet<String>();
            Set<Import> toRemove = new HashSet<Import>();
            for (Import im : cu.getImports()) {
                if (im.getTypeReference() != null && getCrossReferenceSourceInfo().getType(im.getTypeReference()) != null) {
                    String fullName = getCrossReferenceSourceInfo().getType(im.getTypeReference()).getFullName();
                    if (refedTypes.contains(fullName)) {
                        toRemove.add(im);
                    } else {
                        refedTypes.add(fullName);
                    }
                }
            }
            for (Import imp : toRemove) {
                detach(imp);
            }
        }
    }
}
