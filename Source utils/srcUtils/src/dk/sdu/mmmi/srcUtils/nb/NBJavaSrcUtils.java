/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.nb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import recoder.io.DefaultSourceFileRepository;
import recoder.util.FileCollector;

/**
 *
 * @author ao
 */
public class NBJavaSrcUtils {

    public static Project getMainProject() {
        Project p = OpenProjects.getDefault().getMainProject();
        if(!(p instanceof J2SEProject)){
            return null;
        }
        return p;
    }

    public static String getProjName(Project p){
        return ProjectUtils.getInformation(p).getName();
    }

    public static String[] getSrcDirs(Project p){
        if(p==null || !(p instanceof J2SEProject)){
            return new String[]{null};
        }
        String path = p.getProjectDirectory().getPath();
        String src = getProjectProp(p, "src.dir");
        if(src==null || !new File(src).isDirectory()){
            src = getProjectProp(p, "src.src.dir");
        }
        if(src==null || !new File(src).isDirectory()){
            src = "src";
        }
        String[] srcDirs = new String[]{path + System.getProperty("file.separator") + src};
        return srcDirs;
    }

    public static String getProjectProp(Project p, String dir) {
        Project project = p;
        FileObject projectDirectoryFO = project.getProjectDirectory();
        File projectDirectory = FileUtil.toFile(projectDirectoryFO);
        File projectPropertiesFile = PropertyUtils.resolveFile(
        projectDirectory, AntProjectHelper.PROJECT_PROPERTIES_PATH);
        PropertyProvider propertyProvider =
        PropertyUtils.propertiesFilePropertyProvider(projectPropertiesFile);
        return (String) propertyProvider.getProperties().get(dir);
    }
    
    public static String[] findJavaFiles(String dir) {
        FileCollector col = new FileCollector(dir);
        List<String> list = new ArrayList<String>();
        while (col.next(DefaultSourceFileRepository.JAVA_FILENAME_FILTER)) {
            String path;
            try {
                path = col.getFile().getCanonicalPath();
            } catch (IOException ioe) {
                path = col.getFile().getAbsolutePath();
            }
            list.add(path);
        }
        return list.toArray(new String[list.size()]);
    }

    public static String[] findJars(String dir) {
        FileCollector col = new FileCollector(dir);
        List<String> list = new ArrayList<String>();
        while (col.next(".jar")) {
            String path;
            try {
                path = col.getFile().getCanonicalPath();
            } catch (IOException ioe) {
                path = col.getFile().getAbsolutePath();
            }
            list.add(path);
        }
        return list.toArray(new String[list.size()]);
    }

    public static File[] findJarFiles(String dir) {
        String[] f = findJars(dir);
        File[] fils = new File[f.length];
        for(int i = 0; i<f.length; i++){
            fils[i] = new File(f[i]);
        }
        return fils;
    }

}
