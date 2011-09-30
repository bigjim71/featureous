/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.model;

import dk.sdu.mmmi.featuretracer.lib.featureLocation.model.FeatureTraceModel;
import dk.sdu.mmmi.featureous.core.affinity.AffinityProvider;
import dk.sdu.mmmi.featureous.core.controller.Controller;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class TraceSet {

    private LinkedList<TraceListChangeListener> changeListeners = new LinkedList<TraceListChangeListener>();
    private HashSet<TraceModel> firstLevelTraces;
    private TraceModel focus = null;
    private SelectionManager selectionManager = new SelectionManager();

    public TraceSet() {
        firstLevelTraces = new HashSet<TraceModel>();
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public void addTrace(TraceModel traceModel) {
        insertTrace(traceModel);
        notifyChanged();
    }

    public void removeTraces(Set<TraceModel> traceModels) {
        firstLevelTraces.removeAll(traceModels);
        notifyChanged();
    }
    
    public void removeTrace(TraceModel traceModel) {
        firstLevelTraces.remove(traceModel);
        notifyChanged();
    }

    @Deprecated
    public void setFocusOn(TraceModel groupModel) {
        if (groupModel != null && !groupModel.hasSubTraces()) {
            throw new RuntimeException("The argument must be a group trace or null");
        }

        this.focus = groupModel;

        notifyChanged();
    }

    public TraceModel getFirstLevelTraceByName(String name) {
        for (TraceModel tm : getFirstLevelTraces()) {
            if (name.equals(tm.getName())) {
                return tm;
            }
        }
        return null;
    }

    public void mergeTraces(String newName, Set<TraceModel> traces) {
        TraceModel newTrace = new TraceModel(new FeatureTraceModel(newName), traces, null);

        for (TraceModel tm : traces) {
            firstLevelTraces.remove(tm);
        }

        insertTrace(newTrace);

        notifyChanged();
    }

    public void splitTrace(TraceModel traceModel) {
        if (traceModel.hasSubTraces()) {
            for (TraceModel tm : traceModel.getChildren()) {
                this.insertTrace(tm);
            }

            firstLevelTraces.remove(traceModel);
        }

        notifyChanged();
    }

    public boolean containsTrace(String traceName) {
        for (TraceModel tempTrace : getFirstLevelTraces()) {
            if (tempTrace.getName().equals(traceName)) {
                return true;
            }
            if (tempTrace.isAncestorOrSameAsTrace(traceName)) {
                return true;
            }
        }
        return false;
    }

    public Set<ClassModel> getAllClassIDs() {
        Set<ClassModel> classSet = new HashSet<ClassModel>();
        for (TraceModel traceModel : getFirstLevelTraces()) {
            classSet.addAll(traceModel.getClassSet());
        }
        return classSet;
    }

//	public TraceModel getTrace(String traceName) {
//		for (TraceModel traceModel : traces) {
//			if(traceModel.getName().equals(traceName))
//				return traceModel;
//		}
//		return null;
//	}
    public Set<TraceModel> getFirstLevelTraces() {
        if (focus == null) {
            return firstLevelTraces;
        } else {
            Set<TraceModel> traces = new HashSet<TraceModel>();
            traces.addAll(focus.getChildren());
            TraceModel rest = new TraceModel(new FeatureTraceModel("[externals]"), null);

            Controller c = Controller.getInstance();
            for (TraceModel m : firstLevelTraces) {
                if (m.getName().equals(focus.getName())) {
                    continue;
                }
                boolean toSkip = false;
                for (TraceModel cm : focus.getChildren()) {
                    if (cm.getName().equals(m.getName())) {
                        toSkip = true;
                        break;
                    }
                }
                if (toSkip) {
                    continue;
                }

                Set<ClassModel> cms = new HashSet<ClassModel>();
                cms.addAll(m.getClassSet());
                for (ClassModel cm : cms) {
                    rest.addClass(cm);
                }
            }
            traces.add(rest);

            return traces;
        }
    }

    public Set<TraceModel> getAllTraces() {
        Set<TraceModel> tms = new HashSet<TraceModel>();
        for (TraceModel t : getFirstLevelTraces()) {
            tms.addAll(getSubTraces(t));
        }
        return tms;
    }

    private Set<TraceModel> getSubTraces(TraceModel t) {
        Set<TraceModel> tms = new HashSet<TraceModel>();
        tms.add(t);
        for (TraceModel i : t.getChildren()) {
            tms.addAll(getSubTraces(i));
        }
        return tms;
    }

    private void insertTrace(TraceModel traceModel) {
        if (!getFirstLevelTraces().contains(traceModel)) {
            firstLevelTraces.add(traceModel);
        }
    }

    public void notifyChanged() {
        if (changeListeners.size() > 0) {
            changeListeners.get(0).traceListChanged(this);
        }

        for (TraceListChangeListener l : changeListeners) {
            if (l == changeListeners.get(0)) {
                continue;
            }
            final TraceListChangeListener lf = l;

            lf.traceListChanged(TraceSet.this);
        }
    }

    public void addChangeListener(TraceListChangeListener l) {
        changeListeners.add(l);
    }

    @Deprecated
    public void addChangeListenerCategory(TraceListChangeListener l) {
        if (!(l instanceof AffinityProvider)) {
            throw new RuntimeException("Class interface misused by caller.");
        }
        changeListeners.add(0, l);
    }

    public void removeChangeListener(TraceListChangeListener l) {
        changeListeners.remove(l);
    }
}
