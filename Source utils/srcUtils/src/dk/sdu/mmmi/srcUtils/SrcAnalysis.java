/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils;

import java.util.List;
import recoder.java.CompilationUnit;

/**
 *
 * @author ao
 */
public interface SrcAnalysis {
    void setScope(List<CompilationUnit> cus);
}
