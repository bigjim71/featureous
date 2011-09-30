/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.controller;

import dk.sdu.mmmi.featureous.core.affinity.AffinityProvider;
import dk.sdu.mmmi.featureous.core.affinity.canonical.CanonicalGroupsAffinity;
import dk.sdu.mmmi.featureous.core.model.TraceSet;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.similarity.FunctionsInCommonSimilarity;
import dk.sdu.mmmi.featureous.core.similarity.SimilarityRelation;
import dk.sdu.mmmi.featureous.core.util.FTReader;
import dk.sdu.mmmi.featureous.core.util.FileWrite;
import dk.sdu.mmmi.featureous.core.util.XmlWriter;
import dk.sdu.mmmi.featureous.core.util.arff.FtmARFFFile;
import java.io.File;
import java.util.Map;
import java.util.Set;

public class Controller {

    private final TraceSet traceList;
    private AffinityProvider categories;
    private SimilarityRelation similarityRelation;

    private Controller() {
        traceList = new TraceSet();
        categories = new CanonicalGroupsAffinity();
        traceList.addChangeListener(categories);
        similarityRelation = new FunctionsInCommonSimilarity();
    }

    public AffinityProvider getAffinity() {
        return categories;
    }

    public SimilarityRelation getSimilarityRelation() {
        return similarityRelation;
    }

    public void traceListChanged(TraceSet tl) {
        categories.traceListChanged(tl);
    }

    private static class SingletonHolder {

        private final static Controller instance = new Controller();
    }

    public static Controller getInstance() {
        return SingletonHolder.instance;
    }

    public String addTraces(File[] chosenFiles) {
        for (File file : chosenFiles) {
            if (file.getName().endsWith(".ftf")) {
                FTReader ftReader = new FTReader(file);
//                TraceModel traceModel = ftReader.createModel();
                TraceModel traceModel = ftReader.createWrappedModel();
                if (ftReader.getErrorString() != null) {
                    return ftReader.getErrorString();
                }
                traceList.addTrace(traceModel);
            } else if (file.getName().endsWith(".arff")) {
                FtmARFFFile arffReader = new FtmARFFFile(file.getPath());
                arffReader.read();
                Map<String, TraceModel> ftms = arffReader.getFtms();
                for (TraceModel tm : ftms.values()) {
                    traceList.addTrace(tm);
                }
            } else {
                return "Bad file extension.";
            }
        }
        return null;
    }

    public void removeTraces(Set<TraceModel> selectedTraces) {
        traceList.removeTraces(selectedTraces);
    }

    public void mergeTraces(String newTraceName, Set<TraceModel> selectedTraces) {
        traceList.mergeTraces(newTraceName, selectedTraces);
    }

    public void splitTrace(TraceModel traceModel) {
        traceList.splitTrace(traceModel);
    }

    public boolean traceExists(String traceName) {
        return traceList.containsTrace(traceName);
    }

    public String saveXml(File chosenFolder) {
        for (TraceModel traceModel : traceList.getFirstLevelTraces()) {
            String errorMessage = FileWrite.writeTextToFile(chosenFolder + System.getProperty("file.separator") + traceModel.getName() + ".xml", XmlWriter.traceToXml(traceModel));
            if (errorMessage != null) {
                return errorMessage;
            }
        }
        return null;
    }

    public TraceSet getTraceSet() {
        return traceList;
    }
}
