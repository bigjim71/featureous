/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.util;

import dk.sdu.mmmi.featuretracer.lib.featureLocation.model.Execution;
import dk.sdu.mmmi.featuretracer.lib.featureLocation.model.FeatureTraceModel;
import dk.sdu.mmmi.featuretracer.lib.featureLocation.model.PersistenceManager;
import dk.sdu.mmmi.featuretracer.lib.featureLocation.model.Type;
import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import java.io.File;

import java.io.FileNotFoundException;
import java.io.IOException;

public class FTReader {

    private File ftFile;
    private String errorString;

    public FTReader(File ftFile) {
        this.ftFile = ftFile;
    }

    @Deprecated
    public TraceModel createModel() {

        FeatureTraceModel ftm;
        try {
            ftm = PersistenceManager.load(ftFile.getPath());
            TraceModel traceModel = new TraceModel(ftm, ftFile.getPath());

            for (Type t : ftm.getTypes()) {
                ClassModel classModel = new ClassModel(t);
                for (String i : t.getInstancesUsed()) {
                    classModel.addInstanceUsed(i);
                }
                for (Execution e : t.getExecutions()) {

                    classModel.addMethod(e.getSignature());

                    if (e.isConstructor()) {
                        classModel.addConstructor(e.getSignature());
                    }
                    if (e.isFeatureEntryPoint()) {
                        classModel.addFep(e.getSignature());
                    }
                }
                traceModel.addClass(classModel);
            }

            return traceModel;

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            errorString = "File not found: " + ftFile.getAbsolutePath() + "':\n" + ex.getMessage();
        } catch (IOException ex) {
            ex.printStackTrace();
            errorString = "IO exception for file: " + ftFile.getAbsolutePath() + "':\n" + ex.getMessage();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            errorString = "Class not found.\n" + ex.getMessage();
        }

        return null;
    }

    public TraceModel createWrappedModel() {

        FeatureTraceModel ftm;
        try {
            ftm = PersistenceManager.load(ftFile.getPath());
            TraceModel model = new TraceModel(ftm, ftFile.getPath());

            return model;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            errorString = "File not found: " + ftFile.getAbsolutePath() + "':\n" + ex.getMessage();
        } catch (IOException ex) {
            ex.printStackTrace();
            errorString = "IO exception for file: " + ftFile.getAbsolutePath() + "':\n" + ex.getMessage();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            errorString = "Class not found.\n" + ex.getMessage();
        }

        return null;
    }

    public String getErrorString() {
        return errorString;
    }
}
