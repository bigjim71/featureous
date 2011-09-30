/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.sdm.test;

import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import dk.sdu.mmmi.srcUtils.testbench.accessorsandconstructors.Student;
import dk.sdu.mmmi.srcUtils.testbench.briandcouplings.Garden;
import dk.sdu.mmmi.srcUtils.testbench.briandcouplings.Pine;
import dk.sdu.mmmi.srcUtils.testbench.interfaces.Pourable;
import dk.sdu.mmmi.srcUtils.testbench.interfaces.innerpkg.OldOrangeJuice;


/**
 *
 * @author ao
 */
public class TestBenchTests {
    private static int testCount = 0;

    public static void test(StaticDependencyModel p){

        assertTrue("Package count", p.getPackages().size() == 7);

        JPackage accConPkg = p.getOrAddPackageByName("accessorsandconstructors");
        JPackage bCoupPkg = p.getOrAddPackageByName("briandCouplings");
        JPackage interfacesPkg = p.getOrAddPackageByName("interfaces");
        JPackage iInnerPkg = p.getOrAddPackageByName("interfaces.innerpkg");

        assertTrue("Type count", accConPkg.getAllTypes().size() == 2);
        assertTrue("Type count2", bCoupPkg.getAllTypes().size() == 3);
        JType studentType = accConPkg.getOrAddTypeByQualName(Student.class.getName());
        JType studentType2 = accConPkg.getOrAddTypeByQualName(Student.class.getName());
        assertTrue("Identity ", studentType == studentType2);

        assertTrue("Field count", studentType.getFieldCount()==Student.class.getDeclaredFields().length);
        assertTrue("Method count", studentType.getMethodCount()==Student.class.getDeclaredMethods().length);
        assertTrue("Dependency count " + studentType.getDependencies().size(), studentType.getDependencies().size()==1);

        assertTrue("Dependency count2 "+bCoupPkg.getOrAddTypeByQualName(Garden.class.getName())
                .getDependencies().size(), bCoupPkg.getOrAddTypeByQualName(Garden.class.getName())
                .getDependencies().size()==4 + 5);
        assertTrue("Dependency count3 "+bCoupPkg.getOrAddTypeByQualName(Pine.class.getName())
                .getDependencies().size(), bCoupPkg.getOrAddTypeByQualName(Pine.class.getName())
                .getDependencies().size()==3 + 1);
        assertTrue("Dependency count4 "+interfacesPkg.getOrAddTypeByQualName(Pourable.class.getName())
                .getDependencies().size(), interfacesPkg.getOrAddTypeByQualName(Pourable.class.getName())
                .getDependencies().size()==1);
        assertTrue("Dependency count5 "+iInnerPkg.getOrAddTypeByQualName(OldOrangeJuice.class.getName())
                .getDependencies().size(), iInnerPkg.getOrAddTypeByQualName(OldOrangeJuice.class.getName())
                .getDependencies().size()==2);

        JType gardenType = bCoupPkg.getOrAddTypeByQualName(Garden.class.getName());
        assertTrue("Top level", gardenType.isTopLevel());
        assertTrue("Type enclosion", gardenType.getEnclosedTypes().size() == 1);

        System.out.println("-------\n All " + testCount +" tests ran successfully.");
    }

    public static void assertTrue(String desc, boolean c){
        testCount ++;
        if(!c)
            throw new RuntimeException("Bug in " + desc);
    }
}
