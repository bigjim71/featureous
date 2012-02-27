/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.api.ManifestManager.PackageExport;
import org.netbeans.modules.apisupport.project.ModuleDependency;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.ProjectXMLManager.CyclicDependencyException;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGenerator;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author aolszak
 */
public class NbModulesAPI {

    public static NbModuleProject createModule(String codeBaseID, String name, String dir, SuiteProject suite) throws IOException {
        NbModuleProjectGenerator.createSuiteComponentModule(new File(dir), codeBaseID, name,
                "bundle.conf", "layer.xml", suite.getProjectDirectoryFile(), false, false);
        NbModuleProject mod = getModule(dir);
        mod.getPrimaryConfigurationData();
        return mod;
    }

    public static NbModuleProject createLibraryModule(String codeBaseID, String name, String dir, SuiteProject suite, File[] jars) throws IOException {
        NbModuleProjectGenerator.createSuiteLibraryModule(new File(dir), codeBaseID, name,
                "bundle.conf", suite.getProjectDirectoryFile(), null, jars);
        NbModuleProject mod = getModule(dir);
        mod.getPrimaryConfigurationData();
        return mod;
    }

    public static SuiteProject createSuite(String dir) throws IOException {
        SuiteProjectGenerator.createSuiteProject(new File(dir), NbPlatform.getDefaultPlatform().getID(), true);
//        SuiteUtils.findSuite(null)
        SuiteProject suite = getSuite(dir);
        return suite;
    }

    public static void setupSuiteBranding(SuiteProject suite) throws IOException {
        SuiteProperties suiteProps = new SuiteProperties(suite,
                suite.getHelper(), suite.getEvaluator(), SuiteUtils.getSubProjects(suite));
        suiteProps.getBrandingModel().setBrandingEnabled(true);
        suiteProps.getBrandingModel().setName("suite");
        suiteProps.getBrandingModel().setTitle("suite");
        suiteProps.getBrandingModel().store();
        suiteProps.storeProperties();
    }

    public static void addModuleToSuite(NbModuleProject module, SuiteProject suite) {
        try {
            SuiteUtils.addModule(suite, module);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static NbModuleProject getModule(String dir) throws IOException {
        Project p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(new File(dir)));
        return (NbModuleProject) p;
    }

    public static SuiteProject getSuite(String dir) throws IOException {
        Project p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(new File(dir)));
        return (SuiteProject) p;
    }

    public static void addPublicPackageDecl(NbModuleProject mod, String[] pkg) throws IOException {
        ProjectXMLManager xmlMan = ProjectXMLManager.getInstance(mod.getProjectDirectoryFile());
        PackageExport[] pes = xmlMan.getPublicPackages();
        Set<String> pkgs = new HashSet<String>();
        for (PackageExport pe : pes) {
            pkgs.add(pe.getPackage());
        }
        for (String p : pkg) {
            pkgs.add(p);
        }
        xmlMan.replacePublicPackages(pkgs);
    }

    public static void addDependency(NbModuleProject from, NbModuleProject to) throws IOException, CyclicDependencyException {
        ProjectXMLManager fromXmlMan = ProjectXMLManager.getInstance(from.getProjectDirectoryFile());
        ModuleDependency dep = new ModuleDependency(to.getModuleList().getEntry(to.getCodeNameBase()), null, null, true, false);
        fromXmlMan.addDependency(dep);
    }
}
