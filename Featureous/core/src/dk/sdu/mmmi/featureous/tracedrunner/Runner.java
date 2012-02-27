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

            File destFeatureTracer = new File(proj.getProjectDirectory().getPath() + sep + "lib" + sep + "feature-tracer.jar");

            Map<String, String> properties = getProjectProperties(proj);
            String buildDir = properties.get("build.dir");
            File destAgent = new File(proj.getProjectDirectory().getPath() + sep + "lib" + sep + "btrace-agent-ft.jar");
            File destAgentBoot = new File(proj.getProjectDirectory().getPath() + sep + "lib" + sep + "btrace-boot.jar");
            File destScript = new File(proj.getProjectDirectory().getPath() + sep + buildDir + sep + "FeatureTracer.class");

            copyFilesIfNeeded(destFeatureTracer, destAgent, destAgentBoot, destScript);
            
            makeProjectDependency(jproj, destFeatureTracer);
            makeProjectDependency(jproj, destAgent);
            makeProjectDependency(jproj, destAgentBoot);
            
            Properties pp = createAgentArgsProperty(destAgent, destScript, destFeatureTracer, proj);

//            deleteBuildAops(proj, buildDir);

            if (createXmlConfigIfNeeded(proj)) {
                return;
            }

            try {
                ActionUtils.runTarget(buildScript, new String[]{command}, pp);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if (proj instanceof NbModuleProject) {
            NbModuleProject modProj = (NbModuleProject) proj;
            //run.args.extra=-J-Dorg.netbeans.ProxyClassLoader.level=1000 -J-Xmx512m -J-XX:MaxPermSize=256m
            throw new RuntimeException("Not supported yet...");
        }
    }

    private void makeProjectDependency(J2SEProject jproj, File jar) throws UnsupportedOperationException, IOException {
        FileObject antRoot = jproj.getProjectDirectory();
        FileObject libRoot = antRoot.getFileObject("src");
        URI jarURI;
        try {
            jarURI = FileUtil.urlForArchiveOrDir(jar).toURI();
            URI[] newLibs = new URI[]{jarURI};
            ProjectClassPathModifier.addRoots(newLibs, libRoot, ClassPath.COMPILE);
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private boolean createXmlConfigIfNeeded(Project proj) throws IOException {
        File f = new File(proj.getProjectDirectory().getPath() + sep
                + "dk.sdu.mmmi.featuretracer.xml");
        if (!f.exists()) {
            OutputUtil.log("Creating default aop.xml file for the project.");
            FileUtil.createData(f);
            InputStream sourceXMLFileStream = Util.getXMLDescriptor();
            copy(sourceXMLFileStream, f);
//            createDefAopXml(f, proj);
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message("Created template dk.sdu.mmmi.featuretracer.xml descriptor in the project's directory.\n Please supply your project's root package."));
            return true;
        }
        return false;
    }

    private void deleteBuildAops(Project proj, String bd) {
        File f = new File(proj.getProjectDirectory().getPath() + sep + bd + sep + "classes"
                + sep + "META-INF" + sep + "aop.xml");
        if (f.exists()) {
            f.delete();
        }
        f = new File(proj.getProjectDirectory().getPath() + sep + bd + sep + "test"
                + sep + "classes" + sep + "META-INF" + sep + "aop.xml");
        if (f.exists()) {
            f.delete();
        }
    }

    private Properties createAgentArgsProperty(File destAgent, File destScript, File desFeatureTracer, Project proj) {
        File xmlDir = new File(proj.getProjectDirectory().getPath());
        Properties pp = new Properties();
        String args = "";
        // TODO: preserve existing command line
        // -Xbootclasspath/a:./FeatureTracer.jar 
        //-javaagent:../build/btrace-agent.jar=unsafe=true,debug=false,noServer=true,stdout=true,scriptdir=./precomp/dk/sdu/mmmi/featuretracer/btrace,probeDescPath=.
        args += " -Xbootclasspath/a:\"" + desFeatureTracer.getAbsolutePath() + "\""
                + " -javaagent:\"" + destAgent.getAbsolutePath()
                + "=unsafe=true,debug=false,noServer=true,stdout=true,scriptdir="
                + destScript.getParentFile().getAbsolutePath() + ",probeDescPath="+xmlDir.getAbsolutePath()+"\" ";
        pp.put(JavaRunner.PROP_RUN_JVMARGS, args);
        return pp;
    }

    private void copyFilesIfNeeded(File destFt, File destAgent, File destAgentBoot, File destScript) throws IOException {
        if (!destAgent.exists() || !destAgentBoot.exists() || !destFt.exists() || !destScript.exists()) {
            FileUtil.createData(destFt);
            InputStream sourceFTFileStream = Util.getFTJarStream();
            copy(sourceFTFileStream, destFt);

            FileUtil.createData(destScript);
            InputStream sourceClassFileStream = Util.getFTClassStream();
            copy(sourceClassFileStream, destScript);

            FileUtil.createData(destAgent);
            InputStream sourceAgentFileStream = Util.getBtraceAgentStream();
            copy(sourceAgentFileStream, destAgent);

            FileUtil.createData(destAgentBoot);
            InputStream sourceAgentBootFileStream = Util.getBtraceAgentBootStream();
            copy(sourceAgentBootFileStream, destAgentBoot);
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
