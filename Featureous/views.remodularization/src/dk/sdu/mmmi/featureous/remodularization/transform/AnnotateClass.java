package dk.sdu.mmmi.featureous.remodularization.transform;

import java.util.logging.Level;
import java.util.logging.Logger;
import recoder.ParserException;
import recoder.abstraction.Type;
import recoder.java.CompilationUnit;
import recoder.java.Import;
import recoder.java.declaration.AnnotationElementValuePair;
import recoder.java.declaration.AnnotationUseSpecification;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.expression.literal.StringLiteral;
import recoder.java.reference.AnnotationPropertyReference;
import recoder.java.reference.TypeReference;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;
import recoder.kit.TypeKit;
import recoder.kit.UnitKit;
import recoder.list.generic.ASTArrayList;

/**
 *
 * @author ao
 */
public class AnnotateClass extends TwoPassTransformation {

    private final String typeName;
    private CompilationUnit srcCompUnit;
    private TypeDeclaration typeDecl;
    private Type srcType;
    private final String annotationName;
    private final String value;

    public AnnotateClass(String typeName, String annotationName, String value) {
        this.typeName = typeName;
        this.annotationName = annotationName;
        this.value = value;
    }

    @Override
    public ProblemReport analyze() {
        System.out.println("***** annotating: " + typeName);
        srcType = getNameInfo().getType(typeName);
        typeDecl = (TypeDeclaration) getNameInfo().getClassType(typeName);
        srcCompUnit = UnitKit.getCompilationUnit(typeDecl);

        return setProblemReport(EQUIVALENCE);
    }

    @Override
    public void transform() {
        super.transform();

        Type t = getNameInfo().getType("dk.sdu.mmmi.annotations." + annotationName);
        if (t == null) {
            String annotation = "package dk.sdu.mmmi.annotations; \n"
                    + "import java.lang.annotation.ElementType;\n"
                    + "import java.lang.annotation.Target;\n\n"
                    + "@Target(ElementType.TYPE)\n"
                    + "public @interface " + annotationName + " {\n"
                    + "String value();\n"
                    + "}\n";
            try {
                CompilationUnit cu = getProgramFactory().parseCompilationUnit(annotation);
                attach(cu);
                t = getNameInfo().getType("dk.sdu.mmmi.annotations." + annotationName);
            } catch (ParserException ex) {
                Logger.getLogger(AnnotateClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        AnnotationUseSpecification old = null;
        for (AnnotationUseSpecification aus : typeDecl.getAnnotations()) {
            if (getCrossReferenceSourceInfo().getType(aus.getTypeReference()).getFullName().equals(t.getFullName())) {
                old = aus;
                break;
            }
        }

        if (old == null) {
            Import imp = getProgramFactory().createImport(TypeKit.createTypeReference(getProgramFactory(), t), false);
            srcCompUnit.getImports().add(imp);
            srcCompUnit.makeAllParentRolesValid();
            TypeReference tr = TypeKit.createTypeReference(getProgramFactory(), t);
            tr.replaceChild(tr.getPackageReference(), null);
            AnnotationUseSpecification aus = new AnnotationUseSpecification(tr);
            AnnotationPropertyReference r = getProgramFactory().createAnnotationPropertyReference("value");
            StringLiteral sl = getProgramFactory().createStringLiteral("\""+value+"\"");
            AnnotationElementValuePair vp = new AnnotationElementValuePair(r, sl);
            aus.setElementValuePairs(new ASTArrayList<AnnotationElementValuePair>(vp));
            aus.makeAllParentRolesValid();

            typeDecl.getDeclarationSpecifiers().add(0, aus);
            typeDecl.makeParentRoleValid();
        }else{
            old.getElementValuePairs().get(0).setElementValue(getProgramFactory().createStringLiteral("\""+value+"\""));
            old.makeAllParentRolesValid();
        }
        

        getChangeHistory().updateModel();
    }
}
