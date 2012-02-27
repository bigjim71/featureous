package dk.sdu.mmmi.featureous.remodularization.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import recoder.ParserException;
import recoder.abstraction.ClassType;
import recoder.abstraction.Package;
import recoder.convenience.ForestWalker;
import recoder.java.Declaration;
import recoder.java.Reference;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.DeclarationSpecifier;
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.FieldSpecification;
import recoder.java.declaration.MemberDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.modifier.Private;
import recoder.java.declaration.modifier.Protected;
import recoder.java.declaration.modifier.Public;
import recoder.kit.ModifierKit;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;
import recoder.list.generic.ASTArrayList;
import recoder.list.generic.ASTList;

/**
 *
 * @author ao
 */
public class MinimizeVisibility extends TwoPassTransformation {

    @Override
    public ProblemReport analyze() {
        return setProblemReport(NO_PROBLEM);
    }

    @Override
    public void transform() {
        try {
            super.transform();

            ASTList<Declaration> mds = new ASTArrayList<Declaration>();
            ForestWalker w = new ForestWalker(getSourceFileRepository().getAllCompilationUnitsFromPath());
            while (w.next()) {
                if (w.getProgramElement() instanceof TypeDeclaration) {
                    TypeDeclaration td = (TypeDeclaration) w.getProgramElement();
                    if(VisibilityModificationUtils.canDecreaseTypeVisibility(td, getSourceFileRepository())){
                        mds.add(td);
                    }
                }else if (w.getProgramElement() instanceof MemberDeclaration) {
                    MemberDeclaration md = (MemberDeclaration) w.getProgramElement();
                    if(VisibilityModificationUtils.canDecreaseMemberVisibility(md, getCrossReferenceSourceInfo())){
                        mds.add(md);
                    }
                }
            }
            
            for (Declaration md : mds) {
                
                List refList = new ArrayList();
                
                if(md instanceof TypeDeclaration){
                    refList = getCrossReferenceSourceInfo().getReferences(getCrossReferenceSourceInfo().getType(md));
                }else if(md instanceof ConstructorDeclaration){
                    refList = getCrossReferenceSourceInfo().getReferences(getCrossReferenceSourceInfo().getConstructor((ConstructorDeclaration)md));
                }else if(md instanceof MethodDeclaration){
                    refList = getCrossReferenceSourceInfo().getReferences(getCrossReferenceSourceInfo().getMethod((MethodDeclaration)md));
                }else if(md instanceof FieldDeclaration){
                    FieldDeclaration fd = (FieldDeclaration) md;
                    ASTList<FieldSpecification> fss = fd.getFieldSpecifications();
                    for(FieldSpecification fs : fss){
                        refList.addAll(getCrossReferenceSourceInfo().getReferences(fs));
                    }
                }else {
                    throw new RuntimeException("Incomplete");
                }
                
                int modifier = ModifierKit.PRIVATE;
                
                ClassType currClass = getSourceInfo().getContainingClassType(md);
                if(currClass == null){
                    currClass = (ClassType) getCrossReferenceSourceInfo().getType(md);
                    modifier = ModifierKit.PACKAGE;
                }
                Package currPkg = currClass.getPackage();
                
                for(Object refo : refList){
                    Reference inRef = (Reference) refo;
                    ClassType referencingClass = getSourceInfo().getContainingClassType(inRef);
                    if(referencingClass == null){
                        continue;
                    }
                    Package referencingPackage = referencingClass.getPackage();
                    if(!currClass.getFullName().equals(referencingClass.getFullName()) 
                            && !currClass.getTypes().contains(referencingClass)
                            && !referencingClass.getTypes().contains(currClass)){
                        if(currPkg.getFullName().equals(referencingPackage.getFullName()) 
                                && modifier == ModifierKit.PRIVATE){
                            modifier = ModifierKit.PACKAGE;
                        }
                        if(!currPkg.getFullName().equals(referencingPackage.getFullName()) 
                                && getSourceInfo().isSupertype(currClass, referencingClass)
                                //Disable this case for now, TODO
                                && false){
                            modifier = ModifierKit.PROTECTED;
                        }
                        if(!currPkg.getFullName().equals(referencingPackage.getFullName())
                                //Disable this for now (no protected), TODO
                                /*&& !getSourceInfo().isSupertype(currClass, referencingClass)*/){
                            modifier = ModifierKit.PUBLIC;
                        }
                    }
                }
                
                ASTList<DeclarationSpecifier> dss = md.getDeclarationSpecifiers();
                ASTList<DeclarationSpecifier> toRemove = new ASTArrayList<DeclarationSpecifier>();
                if(dss == null){
                    dss = new ASTArrayList<DeclarationSpecifier>();
                }
                for (DeclarationSpecifier ds : dss) {
                    if (ds instanceof Private
                            || ds instanceof Protected
                            || ds instanceof Public) {
                        toRemove.add(ds);
                    }
                }
                dss.removeAll(toRemove);

                if(modifier!=ModifierKit.PACKAGE){
                    dss.add(ModifierKit.createModifier(getProgramFactory(), modifier));
                }
                
                md.setDeclarationSpecifiers(dss);
                
                getChangeHistory().updateModel();
            }
        } catch (ParserException ex) {
            Logger.getLogger(MinimizeVisibility.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
