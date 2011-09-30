/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.metrics;

import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ao
 */
public abstract class AbstractMetric {

    public static enum Scope {

        PACKAGE, CLASS, METHOD, FEATURE, SYSTEM;

        public static Scope getPkgOrClass(boolean pkg) {
            if (pkg) {
                return PACKAGE;
            } else {
                return CLASS;
            }
        }
    }
    private final Scope scope;
    public final String name;
    private Set<Result> results;

    public AbstractMetric(String name, Scope scope) {
        this.name = name;
        this.scope = scope;
        results = new HashSet<Result>();
    }
    
    public float calculateAndReturnRes(Set<TraceModel> tms, StaticDependencyModel sdm){
        if(!results.isEmpty()){
            throw new RuntimeException("Do not reuse metric objects.");
        }
        calculateAll(tms, sdm);
        return getResult();
    }
    
    public Set<Result> calculateAndReturnAll(Set<TraceModel> tms, StaticDependencyModel sdm){
        if(!results.isEmpty()){
            throw new RuntimeException("Do not reuse metric objects.");
        }
        calculateAll(tms, sdm);
        return getResults();
    }
    
    public float calculateAndReturnFor(Set<TraceModel> tms, StaticDependencyModel sdm, String target){
        if(!results.isEmpty()){
            throw new RuntimeException("Do not reuse metric objects.");
        }
        calculateAll(tms, sdm);
        return getResultFor(target).value;
    }

    public abstract void calculateAll(Set<TraceModel> tms, StaticDependencyModel sdm);
    public abstract float getResult();

    protected void setResultForSubject(float result, String subject) {
        results.add(new Result(result, subject));
    }

    protected float getSumVal() {
        float sum = 0;
        for (Result res : results) {
            sum += res.value;
        }
        return sum;
    }

    protected float getMeanVal() {
        return getSumVal() / results.size();
    }

    protected float max() {
        float max = Float.MIN_VALUE;
        for (Result r : results) {
            if (r.value != null && max < r.value) {
                max = r.value;
            }
        }
        return max;
    }

    protected float getVar() {
        if (results.size() == 0) {
            return 0;
        }
        float avg = getMeanVal();
        float sum = 0.0f;
        for (Result r : results) {
            sum += (r.value - avg) * (r.value - avg);
        }
        return sum / (results.size() - 1);
    }

    /**
     * Return sample standard deviation of array, NaN if no such value.
     */
    protected float getStdDev() {
        return (float) Math.sqrt(getVar());
    }

    public String getName() {
        return name;
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return name.equals(o.toString());
    }

    public Set<Result> getResults() {
        return results;
    }
    
    public Result getResultFor(String id) {
        for(Result r : results){
            if(r.name.equals(id)){
                return r;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name.toString();
    }
}
