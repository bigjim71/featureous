/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.sourcehighlighter.api;

/**
 *
 * @author ao
 */
public class LineInfoTag {

    private final long line;
    private final String msg;


    public LineInfoTag(long line, String msg) {
        this.line = line;
        this.msg = msg;
    }

    public long getLine() {
        return line;
    }

    public String getMsg() {
        return msg;
    }
}
