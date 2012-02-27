package dk.sdu.mmmi.featureous.remodularization.transform;

import recoder.abstraction.Type;
import recoder.java.CompilationUnit;
import recoder.java.declaration.AnnotationUseSpecification;
import recoder.java.declaration.TypeDeclaration;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;
import recoder.kit.UnitKit;

/**
 *
 * @author ao
 */
public class ReadAnnotationValue extends TwoPassTransformation {

    private final String typeName;
    private CompilationUnit srcCompUnit;
    private TypeDeclaration typeDecl;
    private Type srcType;
    private final String annotationName;
    private String value = null;

    public ReadAnnotationValue(String typeName, String annotationName) {
        this.typeName = typeName;
        this.annotationName = annotationName;
    }

    @Override
    public ProblemReport analyze() {
        System.out.println("***** reading annotation: " + typeName);
        srcType = getNameInfo().getType(typeName);
        typeDecl = (TypeDeclaration) getNameInfo().getClassType(typeName);
        srcCompUnit = UnitKit.getCompilationUnit(typeDecl);

        return setProblemReport(EQUIVALENCE);
    }

    @Override
    public void transform() {
        super.transform();

        Type t = getNameInfo().getType("dk.sdu.mmmi.annotations." + annotationName);
        if(t==null){
            System.err.println("Annotation not found");
            return;
        }

        AnnotationUseSpecification old = null;
        for (AnnotationUseSpecification aus : typeDecl.getAnnotations()) {
            if (getCrossReferenceSourceInfo().getType(aus.getTypeReference()).getFullName().equals(t.getFullName())) {
                old = aus;
                break;
            }
        }

        if(old!=null){
            String sl = (String) old.getElementValuePairs().get(0).getValue();
            value = sl.replace("\"", "");
        }else{
            System.err.println("parameter has not value");
        }
    }

    public String getValue() {
        return value;
    }

    public String getTypeName() {
        return typeName;
    }
}
