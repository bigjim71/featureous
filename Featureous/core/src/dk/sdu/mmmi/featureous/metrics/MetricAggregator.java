package dk.sdu.mmmi.featureous.metrics;

import dk.sdu.mmmi.featureous.core.ui.OutputUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ao
 */
public class MetricAggregator {

    //version, metric, target
    private List<List<String>> coeffs = new ArrayList<List<String>>();
    private Map<List<String>, Double> vals = new HashMap<List<String>, Double>();

    public void insertResultForSystem(double val, String version, String metric) {
        insertResult(val, version, metric, "###system###");
    }

    public void insertResult(double val, String version, String metric, String target) {
        List<String> al = new ArrayList<String>();
        al.add(version);
        al.add(metric);
        al.add(target);

        coeffs.add(al);
        vals.put(al, val);
    }

    public Map<String, Double> getSystemEvolution(String metric) {
        //ver->val
        return getTargetEvolution(metric, "###system###");
    }

    public Map<String, Double> getMetricsForSystem(String version) {
        //(ver,system),metric->val
        return getMetricsForTarget(version, "###system###");
    }

    public Map<String, Double> getTargetEvolution(String metric, String target) {
        //(target,metric),ver->val
        Map<String, Double> res = new HashMap<String, Double>();
        for (List<String> c : coeffs) {
            if (c.get(1).equals(metric) && c.get(2).equals(target)) {
                res.put(c.get(0), vals.get(c));
            }
        }
        return res;
    }

    public Map<String, Double> getMetricSnapshot(String version, String metric) {
        //(version, metric),target->val
        Map<String, Double> res = new HashMap<String, Double>();
        for (List<String> c : coeffs) {
            if (c.get(0).equals(version) && c.get(1).equals(metric)) {
                res.put(c.get(2), vals.get(c));
            }
        }
        return res;
    }

    public Map<String, Double> getMetricsForTarget(String version, String target) {
        //(version, target),metric->val
        Map<String, Double> res = new HashMap<String, Double>();
        for (List<String> c : coeffs) {
            if (c.get(0).equals(version) && c.get(2).equals(target)) {
                res.put(c.get(1), vals.get(c));
            }
        }
        return res;
    }

    public List<String> getVersionIDs(){
        return getIDs(0);
    }
    public List<String> getMetricIDs(){
        return getIDs(1);
    }
    public List<String> getTargetIDs(){
        return getIDs(2);
    }
    
    private List<String> getIDs(int idx){
        Set<String> ids = new HashSet<String>();
        
        for(List<String> c : coeffs){
            ids.add(c.get(idx));
        }
        
        return new ArrayList<String>(ids);
    }
    
    public static void printJoined(List<String> ids, List<Map<String, Double>> maps) {
        if (ids.size() != maps.size()) {
            throw new RuntimeException("Inconsistent parameters");
        }
        Set<String> keys = new HashSet<String>();

        for (Map<String, Double> m : maps) {
            keys.addAll(m.keySet());
        }

        StringBuilder sb = new StringBuilder(";");
        for (String id : ids) {
            sb.append(id).append(";");
        }

        sb.append("\n");

        for (String key : keys) {
            sb.append(key).append(";");
            for (Map<String, Double> m : maps) {
                Double val = m.get(key);
                if (val != null) {
                    sb.append(val);
                }
                sb.append(";");
            }
            sb.append("\n");
        }

        OutputUtil.log(sb.toString());
    }
}
