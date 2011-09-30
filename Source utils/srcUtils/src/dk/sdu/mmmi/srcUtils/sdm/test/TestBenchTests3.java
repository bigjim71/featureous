/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.sdm.test;

import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;

/**
 *
 * @author ao
 */
public class TestBenchTests3 {
    private static int testCount = 0;

    public static void testEx1(StaticDependencyModel p){

//        assertTrue("Package count ", p.getPackages().size() == 1);
//
//        JPackage pack = p.getOrAddPackageByName("dk.sdu.mmmi.featuretracer.test.example1");
//
//        assertTrue("Type count", pack.getTypes().size() == 3);
//        JType destType = pack.getOrAddTypeByQualName(Dest.class.getName());
//        JType mainType = pack.getOrAddTypeByQualName(Main.class.getName());
//
//        assertTrue("Field count", destType.getFieldCount()== 0);
//        assertTrue("Method count", destType.getMethodCount()== 3);
//        assertTrue("Dependency count ", destType.getDependencies().size()==0);
//
//        assertTrue("Field count2", mainType.getFieldCount()== 1);
//        assertTrue("Method count2", mainType.getMethodCount()== 4);
//        assertTrue("Dependency count2", mainType.getDependencies().size()==3);
//
//        System.out.println("-------\n All " + testCount +" tests ran successfully.");
    }

    public static void testEx2(StaticDependencyModel p){

        assertTrue("Package count ", p.getPackages().size() == 1);

        JPackage pack = p.getOrAddPackageByName("dk.sdu.mmmi.featuretracer.test.example2");

        assertTrue("Type count" + pack.getAllTypes().size(), pack.getAllTypes().size() == 5 + 2);

        System.out.println("-------\n All " + testCount +" tests ran successfully.");
    }

    public static void assertTrue(String desc, boolean c){
        testCount ++;
        if(!c)
            throw new RuntimeException("Bug in " + desc);
    }
}
