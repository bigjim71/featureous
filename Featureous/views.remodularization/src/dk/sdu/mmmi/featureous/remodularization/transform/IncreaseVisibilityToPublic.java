package dk.sdu.mmmi.featureous.remodularization.transform;

import recoder.convenience.ForestWalker;
import recoder.java.Declaration;
import recoder.java.declaration.DeclarationSpecifier;
import recoder.java.declaration.MemberDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.modifier.Private;
import recoder.java.declaration.modifier.Protected;
import recoder.java.declaration.modifier.Public;
import recoder.kit.MiscKit;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;
import recoder.list.generic.ASTArrayList;
import recoder.list.generic.ASTList;

/**
 *
 * @author ao
 */
public class IncreaseVisibilityToPublic extends TwoPassTransformation {

    @Override
    public ProblemReport analyze() {
        return setProblemReport(NO_PROBLEM);
    }

    @Override
    public void transform() {
        super.transform();
        ASTList<Declaration> mds = new ASTArrayList<Declaration>();
        ForestWalker w = new ForestWalker(getSourceFileRepository().getCompilationUnits());
        while (w.next()) {
            if (w.getProgramElement() instanceof TypeDeclaration) {
                TypeDeclaration td = (TypeDeclaration) w.getProgramElement();
                if (VisibilityModificationUtils.canIncreaseTypeVisibility(td, getSourceFileRepository())) {
                    mds.add(td);
                }
            } else if (w.getProgramElement() instanceof MemberDeclaration) {
                MemberDeclaration md = (MemberDeclaration) w.getProgramElement();
                if (VisibilityModificationUtils.canIncreaseMemberVisibility(md)) {
                    mds.add(md);
                }
            }
        }
        for (Declaration md : mds) {
            ASTList<DeclarationSpecifier> dss = md.getDeclarationSpecifiers();
            ASTList<DeclarationSpecifier> toRemove = new ASTArrayList<DeclarationSpecifier>();
            boolean add = true;
            if (dss == null) {
                dss = new ASTArrayList<DeclarationSpecifier>();
            }
            for (DeclarationSpecifier ds : dss) {
                if (ds instanceof Private
                        || ds instanceof Protected) {
                    toRemove.add(ds);
                }
                if (ds instanceof Public) {
                    add = false;
                }
            }

            
            if (add) {
                DeclarationSpecifier ds = getProgramFactory().createPublic();
                MiscKit.unindent(ds);
                MiscKit.unindent(md);
                dss.add(0, ds);
                ds.setParent(md);
                MiscKit.unindent(md);
//                    getChangeHistory().attached(md);
            }

            for (DeclarationSpecifier ds : toRemove) {
                MiscKit.unindent(ds);
                dss.remove(ds);
//                    MiscKit.remove(getChangeHistory(), ds);
            }

            md.setDeclarationSpecifiers(dss);
        }
    }
}
