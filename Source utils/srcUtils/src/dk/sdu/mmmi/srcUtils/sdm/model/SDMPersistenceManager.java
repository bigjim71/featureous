/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.sdm.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ao
 */
public class SDMPersistenceManager {

    private static final String FILE_EXT = ".sdm";

	public static void save(StaticDependencyModel m, String destFile) throws FileNotFoundException, IOException{
		if(!destFile.endsWith(FILE_EXT)){
			destFile += FILE_EXT;
		}
		ObjectOutputStream oos = new ObjectOutputStream(
				new FileOutputStream(destFile));
		oos.writeObject(m);
		oos.close();
	}

	public static StaticDependencyModel load(String sourceFile) throws FileNotFoundException, IOException, ClassNotFoundException{
		if(!sourceFile.endsWith(FILE_EXT)){
			throw new RuntimeException("Bad format.");
		}
		ObjectInputStream ois = new ObjectInputStream(
				new FileInputStream(sourceFile));
		StaticDependencyModel m = (StaticDependencyModel)ois.readObject();
		ois.close();
		return m;
	}

    public static List<StaticDependencyModel> loadAllFromDir(String dir) throws FileNotFoundException, IOException, ClassNotFoundException{
		List<StaticDependencyModel> res = new ArrayList<StaticDependencyModel>();
		File fdir = new File(dir);
		if(!fdir.isDirectory()){
			throw new RuntimeException("Directory not found.");
		}
		for(File f : fdir.listFiles()){
			if(!f.getName().endsWith(FILE_EXT)){
				continue;
			}
			StaticDependencyModel ff = load(f.getPath());
			res.add(ff);
		}
		return res;
	}
}
