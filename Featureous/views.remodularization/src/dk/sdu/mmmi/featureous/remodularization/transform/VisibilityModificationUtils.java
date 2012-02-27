package dk.sdu.mmmi.featureous.remodularization.transform;

import recoder.CrossReferenceServiceConfiguration;
import recoder.abstraction.Constructor;
import recoder.abstraction.Method;
import recoder.io.SourceFileRepository;
import recoder.java.declaration.ClassInitializer;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.MemberDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.kit.MethodKit;
import recoder.service.CrossReferenceSourceInfo;

/**
 *
 * @author ao
 */
public class VisibilityModificationUtils {

    public static boolean canModifyTypeVisibility(TypeDeclaration td) {
        return //Rule out method and constructor inner classes
                !(td.getContainer() instanceof Constructor)
                && !(td.getContainer() instanceof Method)
                //Rule out anonymous members classes
                && !(td.getName() == null);
    }

    public static boolean canIncreaseTypeVisibility(TypeDeclaration td, SourceFileRepository sfr) {
        return canModifyTypeVisibility(td)
                //Rule out secondary classes
                && !(sfr.getCompilationUnit(td.getFullName()) == null);
    }
    
    public static boolean canDecreaseTypeVisibility(TypeDeclaration td, SourceFileRepository sfr) {
        return canModifyTypeVisibility(td)
                //Rule out main
                && sfr.getCompilationUnit(td.getFullName()) == null
                //Rule out inner type of interfaces
                && !(td.getContainingClassType()!=null && td.getContainingClassType().isInterface());
    }

    public static boolean canIncreaseMemberVisibility(MemberDeclaration md) {
        return 
                //Rule out constructors of enums
                !((md instanceof ConstructorDeclaration) && (((ConstructorDeclaration) md).getContainingClassType().isEnumType()))
                //Rule out statis inits
                && !(md instanceof ClassInitializer);
    }
    
    public static boolean canDecreaseMemberVisibility(MemberDeclaration md, CrossReferenceSourceInfo crsi) {
        return canIncreaseMemberVisibility(md) 
                // Rule out abstract methods and methods in interfaces
                && !((md instanceof MethodDeclaration) && ((((MethodDeclaration) md).isAbstract()) ||
                ((MethodDeclaration) md).getContainingClassType().isInterface()))
                //Rule out fields of interfaces
                && !((md instanceof FieldDeclaration) && ((FieldDeclaration) md).getASTParent().isInterface())
                //Rule out public static final
                && !((md instanceof FieldDeclaration) && ((FieldDeclaration) md).isStatic() && ((FieldDeclaration) md).isFinal() && ((FieldDeclaration) md).isPublic())
                //Rule out constructors, for now! TODO
                && !(md instanceof ConstructorDeclaration)
                // Rule out overwriting methods
                && !((md instanceof MethodDeclaration) && !MethodKit.getAllRedefinedMethods(crsi.getMethod((MethodDeclaration)md)).isEmpty());
    }
}
