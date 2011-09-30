/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.sdm.metrics;

import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import dk.sdu.mmmi.srcUtils.sdm.test.TestBenchTests;
import dk.sdu.mmmi.srcUtils.testbench.accessorsandconstructors.Student;
import dk.sdu.mmmi.srcUtils.testbench.briandcouplings.Garden;
import dk.sdu.mmmi.srcUtils.testbench.briandcouplings.Pine;
import dk.sdu.mmmi.srcUtils.testbench.composition.Baker;
import dk.sdu.mmmi.srcUtils.testbench.interfaces.Drinkable;
import dk.sdu.mmmi.srcUtils.testbench.interfaces.OrangeJuice;
import dk.sdu.mmmi.srcUtils.testbench.interfaces.innerpkg.OldOrangeJuice;

public class Tests {
    private static StaticDependencyModel dm;

    public static void test(StaticDependencyModel dm){
        Tests.dm = dm;

        report("", "Package count", 7, dm.getPackages().size());
        int typeCount = 0;
        for(JPackage p : dm.getPackages()){
            typeCount += p.getAllTypes().size();
        }
        report("", "Type count", 21, typeCount);

        testTypeAndMethod(Student.class, 11, 3, 6);
        testTypeAndMethod(Garden.class, 5, 2, 3);
        testTypeAndMethod(Pine.class, 2, 2, 1);
        testTypeAndMethod(Baker.class, 2, 1, 0);
        testTypeAndMethod(Drinkable.class, 1, 0, 0);
        testTypeAndMethod(OrangeJuice.class, 4, 1, 0);
        testTypeAndMethod(OldOrangeJuice.class, 1, 1, 0);

        assertTrue("PCoh(accessorsandconstructors)", 
                new PCoh().PCohp(dm.getOrAddPackageByName("accessorsandconstructors"))==1/12.0);
        assertTrue("PCoh(briandCoupling)", 
                new PCoh().PCohp(dm.getOrAddPackageByName("briandCouplings"))==1/26.0);
        assertTrue("PCoh(briandCoupling.ancestors)", 
                new PCoh().PCohp(dm.getOrAddPackageByName("briandCouplings.ancestors"))==0/12.0);
        assertTrue("PCoh(composition)", 
                new PCoh().PCohp(dm.getOrAddPackageByName("composition"))==6/42.0);
        assertTrue("PCoh(interfaces)", 
                new PCoh().PCohp(dm.getOrAddPackageByName("interfaces"))==4/64.0);
        assertTrue("PCoh(interfaces.innerpkg)", 
                new PCoh().PCohp(dm.getOrAddPackageByName("interfaces.innerpkg"))==0/5.0);

        assertTrue("PCoupI(accessorsandconstructors)",
                new PCoupImport().calculate(dm, dm.getOrAddPackageByName("accessorsandconstructors"))==0);
        assertTrue("PCoupI(briandCoupling)"+new PCoupImport().calculate(dm, dm.getOrAddPackageByName("briandCouplings")),
                new PCoupImport().calculate(dm, dm.getOrAddPackageByName("briandCouplings"))==15);
        assertTrue("PCoupI(briandCoupling.ancestors)"+new PCoupImport().calculate(dm, dm.getOrAddPackageByName("briandCouplings.ancestors")),
                new PCoupImport().calculate(dm, dm.getOrAddPackageByName("briandCouplings.ancestors"))==0);
        assertTrue("PCoupI(composition)",
                new PCoupImport().calculate(dm, dm.getOrAddPackageByName("composition"))==0);
        assertTrue("PCoupI(interfaces)",
                new PCoupImport().calculate(dm, dm.getOrAddPackageByName("interfaces"))==0);
        assertTrue("PCoupI(interfaces.innerpkg)",
                new PCoupImport().calculate(dm, dm.getOrAddPackageByName("interfaces.innerpkg"))==3);

        assertTrue("PCoupE(accessorsandconstructors)",
                new PCoupExport().calculate(dm, dm.getOrAddPackageByName("accessorsandconstructors"))==0);
        assertTrue("PCoupE(briandCoupling)"+new PCoupExport().calculate(dm, dm.getOrAddPackageByName("briandCouplings")),
                new PCoupExport().calculate(dm, dm.getOrAddPackageByName("briandCouplings"))==0);
        assertTrue("PCoupE(briandCoupling.ancestors)"+new PCoupExport().calculate(dm, dm.getOrAddPackageByName("briandCouplings.ancestors")),
                new PCoupExport().calculate(dm, dm.getOrAddPackageByName("briandCouplings.ancestors"))==9);
        assertTrue("PCoupE(composition)",
                new PCoupExport().calculate(dm, dm.getOrAddPackageByName("composition"))==0);
        assertTrue("PCoupE(interfaces)",
                new PCoupExport().calculate(dm, dm.getOrAddPackageByName("interfaces"))==3);
        assertTrue("PCoupE(interfaces.innerpkg)",
                new PCoupExport().calculate(dm, dm.getOrAddPackageByName("interfaces.innerpkg"))==0);

        System.out.println("Testing done.");
		
    }

    private static void testTypeAndMethod(Class clazz, int mCount, int fCount, int accCount) {
        JPackage p = dm.getOrAddPackageByName(clazz.getPackage().getName());
        JType t = p.getOrAddTypeByQualName(clazz.getName());
            report(t.toString(), "Method count", mCount, t.getMethodCount() + t.getConstructorCount());
            report(t.toString(), "Field count", fCount, t.getFieldCount());
            report(t.toString(), "Est accessor count", accCount, t.getEstAccessorCount());
    }

    private static void assertTrue(String desc, boolean c){
        TestBenchTests.assertTrue(desc, c);
    }

    private static void report(String typeSign, String concern, int expected, int actual){
            Boolean passed = new Boolean(expected==actual);
            if(!passed){
                    System.out.println(typeSign + " :" + concern + " - " + passed + ".\n" );
                    System.out.println(" ERROR expected: " + expected + ", was: " + actual + "\n" );
        throw new RuntimeException("Test failed");
            }
    }
}
