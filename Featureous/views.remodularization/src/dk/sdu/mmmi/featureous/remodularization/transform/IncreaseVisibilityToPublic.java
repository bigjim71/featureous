/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.transform;

import dk.sdu.mmmi.featureous.core.ui.OutputUtil;
import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.TreeWalker;
import recoder.java.Declaration;
import recoder.java.declaration.DeclarationSpecifier;
import recoder.java.declaration.MemberDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.modifier.Private;
import recoder.java.declaration.modifier.Protected;
import recoder.java.declaration.modifier.Public;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;
import recoder.kit.UnitKit;
import recoder.list.generic.ASTArrayList;
import recoder.list.generic.ASTList;

/**
 *
 * @author ao
 */
public class IncreaseVisibilityToPublic extends TwoPassTransformation {

    private final String typeName;
    private TypeDeclaration typeDecl;

    public IncreaseVisibilityToPublic(CrossReferenceServiceConfiguration crs, String typeName) {
        super(crs);
        this.typeName = typeName;
    }

    @Override
    public ProblemReport analyze() {
        OutputUtil.log("***** incVis: " + typeName);
        typeDecl = (TypeDeclaration) getNameInfo().getClassType(typeName);

        return setProblemReport(NO_PROBLEM);
    }

    @Override
    public void transform() {
        super.transform();

        ASTList<Declaration> mds = new ASTArrayList<Declaration>();
        TreeWalker w = new TreeWalker(UnitKit.getCompilationUnit(typeDecl));
        while (w.next()) {
            if (w.getProgramElement() instanceof MemberDeclaration) {
                mds.add((Declaration)w.getProgramElement());
            }else if (w.getProgramElement() instanceof TypeDeclaration) {
                mds.add((Declaration)w.getProgramElement());
            }
        }
        for (Declaration md : mds) {
//                    typeDecl.getProgramModelInfo().isVisibleFor((Member) md, typeDecl);
            ASTList<DeclarationSpecifier> dss = md.getDeclarationSpecifiers();
            if(dss==null){
                dss = new ASTArrayList<DeclarationSpecifier>();
            }
            ASTList<DeclarationSpecifier> toRemove = new ASTArrayList<DeclarationSpecifier>();
            boolean add = true;
            for (DeclarationSpecifier ds : dss) {
                if (ds instanceof Private
                        || ds instanceof Protected) {
                    toRemove.add(ds);
                }
                if (ds instanceof Public) {
                    add = false;
                }
            }
            dss.removeAll(toRemove);
            if (add) {
                dss.add(new Public());
            }
            md.setDeclarationSpecifiers(dss);
        }
    }
}
