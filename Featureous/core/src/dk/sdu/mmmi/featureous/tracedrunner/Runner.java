/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.tracedrunner;

import dk.sdu.mmmi.featureous.core.ui.OutputUtil;
import dk.sdu.mmmi.featuretracer.Util;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author ao
 */
public class Runner {

    private final String sep = System.getProperty("file.separator");
    
    public void execute(Project proj, String command) throws IOException {

        if (proj instanceof J2SEProject) {
            J2SEProject jproj = (J2SEProject) proj;
            FileObject buildScript = J2SEProjectUtil.getBuildXml(jproj);

            Map<String, String> properties = getProjectProperties(proj);
            String buildDir = properties.get("build.dir");
            File dest = new File(proj.getProjectDirectory().getPath() + sep + buildDir + sep+ "aspectjweaver_ft.jar");
            File destFt = new File(proj.getProjectDirectory().getPath() + sep + "lib"+sep+"ft.jar");

            createJarsIfNeeded(dest, destFt);
            
            FileObject antRoot = jproj.getProjectDirectory();
            FileObject libRoot = antRoot.getFileObject("src");
            URI jarURI; 
            try {
                jarURI = FileUtil.urlForArchiveOrDir(destFt).toURI();
                URI[] newLibs = new URI[]{jarURI};
                ProjectClassPathModifier.addRoots(newLibs, libRoot, ClassPath.COMPILE); 
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }

            Properties pp = createAgentArgsProperty(dest);

            deleteBuildAops(proj, buildDir);
            
            if (createAopXmlIfNeeded(proj)) {
                return;
            }

            try {
                ActionUtils.runTarget(buildScript, new String[]{command}, pp);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            }
        }else if(proj instanceof NbModuleProject){
            NbModuleProject modProj = (NbModuleProject) proj;
            //run.args.extra=-J-Dorg.netbeans.ProxyClassLoader.level=1000 -J-Xmx512m -J-XX:MaxPermSize=256m
            throw new RuntimeException("Not supported yet...");
        }
    }

    private boolean createAopXmlIfNeeded(Project proj) throws IOException {
        File f = new File(proj.getProjectDirectory().getPath() + sep 
                + "src"+sep+"META-INF");
        if (!f.exists() || !f.isDirectory()) {
            f.mkdir();
        }
        f = new File(proj.getProjectDirectory().getPath() + sep+"src"+sep
                +"META-INF"+sep+"aop.xml");
        if (!f.exists()) {
            OutputUtil.log("Creating default aop.xml file for the project.");
            createDefAopXml(f, proj);
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message("Created template aop.xml in src/META-INF.\n Please supply your project information."));
            return true;
        }
        return false;
    }

    private void deleteBuildAops(Project proj, String bd) {
        File f = new File(proj.getProjectDirectory().getPath() + sep + bd + sep + "classes"
                +sep+"META-INF"+sep+"aop.xml");
        if (f.exists()) {
            f.delete();
        }
        f = new File(proj.getProjectDirectory().getPath() + sep + bd + sep + "test"
                +sep+"classes"+sep+"META-INF"+sep+"aop.xml");
        if (f.exists()) {
            f.delete();
        }
    }

    private Properties createAgentArgsProperty(File dest) {
        Properties pp = new Properties();
        String args = "";
        // TODO: preserve existing command line
        args += " -javaagent:\"" + dest.getAbsolutePath() + "\"";
        args += " -Daj.weaving.verbose=true";
        args += " -Daj.weaving.debug=true";
        pp.put(JavaRunner.PROP_RUN_JVMARGS, args);
        return pp;
    }

    private void createJarsIfNeeded(File dest, File destFt) throws IOException {
        if (!dest.exists() || !destFt.exists()) {
            FileUtil.createData(dest);
            InputStream sourceFileStream = getClass().getResourceAsStream("/dk/sdu/mmmi/featureous/tracedrunner/aspectjweaver.jar");
            copy(sourceFileStream, dest);
            FileUtil.createData(destFt);
            InputStream sourceFTFileStream = Util.getFTStream();
            copy(sourceFTFileStream, destFt);
        }
    }

    public static Map<String, String> getProjectProperties(Project p) {
        Project project = p;
        FileObject projectDirectoryFO = project.getProjectDirectory();
        File projectDirectory = FileUtil.toFile(projectDirectoryFO);
        File projectPropertiesFile = PropertyUtils.resolveFile(
                projectDirectory, AntProjectHelper.PROJECT_PROPERTIES_PATH);
        PropertyProvider propertyProvider =
                PropertyUtils.propertiesFilePropertyProvider(projectPropertiesFile);
        return propertyProvider.getProperties();
    }

//    private String getProjectBuildDir(Project p) {
//        Project project = p;
//        FileObject projectDirectoryFO = project.getProjectDirectory();
//        File projectDirectory = FileUtil.toFile(projectDirectoryFO);
//        File projectPropertiesFile = PropertyUtils.resolveFile(
//                projectDirectory, AntProjectHelper.PROJECT_PROPERTIES_PATH);
//        PropertyProvider propertyProvider =
//                PropertyUtils.propertiesFilePropertyProvider(projectPropertiesFile);
//        return (String) propertyProvider.getProperties().get("build.dir");
//    }
//    private String findFilePath(Project p, String fileExtName) {
//        Enumeration<? extends FileObject> e = p.getProjectDirectory().getChildren(true);
//        while (e.hasMoreElements()) {
//            FileObject f = e.nextElement();
//            if (f.getNameExt().equals(fileExtName)) {
//                return f.getPath();
//            }
//        }
//        return null;
//    }
    public boolean isSupported(String command, Map<String, ?> properties) {
        return true;
    }

    void copy(InputStream srcStream, File dst) throws IOException {
        InputStream in = srcStream;
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    private void createDefAopXml(File f, Project p) throws FileNotFoundException, IOException {
        FileOutputStream fos = null;
        DataOutputStream dos = null;
        FileUtil.createData(f);
        try {
            fos = new FileOutputStream(f);
            dos = new DataOutputStream(fos);
            dos.write(new String("<aspectj>\n\n"
                    + "\t<aspects>\n"
                    + "\t\t<concrete-aspect name=\"dk.sdu.mmmi.aspects." + ProjectUtils.getInformation(p).getDisplayName().replaceAll(" ", "_")
                    + "\" extends=\"dk.sdu.mmmi.featuretracer.lib.ExecutionTracer\">\n"
                    + "\t\t\t<pointcut name=\"packages\" expression=\"within(<YOUR_ROOT_PACKAGE>..*)\"/>\n"
                    + "\t\t</concrete-aspect>\n"
                    + "\t</aspects>\n\n"
                    + "\t<weaver>\n"
                    + "\t\t<exclude within=\"! <YOUR_ROOT_PACKAGE>..*\"/>\n"
                    + "\t\t<include within=\"<YOUR_ROOT_PACKAGE>..*\"/>\n"
                    + "\t</weaver>\n\n"
                    + "</aspectj>").getBytes());
        } finally {
            if (dos != null) {
                dos.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }
}
