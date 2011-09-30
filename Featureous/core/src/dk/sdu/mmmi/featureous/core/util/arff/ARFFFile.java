/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.util.arff;

import dk.sdu.mmmi.featureous.core.ui.OutputUtil;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.io.LineNumberReader;

import java.util.List;
import java.util.ArrayList;

/**
 * Parses files formatted using the Attribute-Relation File Format (ARFF).
 * Derived classes must fill-in the application-specific logic.
 * <P>
 * See {@link http://www.cs.waikato.ac.nz/~ml/weka/arff.html}
 * 
 * @author eaddy
 */
public class ARFFFile {

    protected String path;
    protected int currentLine;
    protected int currentFieldIndex = 0;
    long fileSize = 0;
    long filePos = 0;
    protected int validInstances = 0;

    public ARFFFile(String path) {
        this.path = path;
    }

    /**
     * Parses
     *
     * @ATTRIBUTE declarations,
     * @DATA declarations, and data instances. Derived classes are responsible
     *       for app-specific validation.
     *
     * @return false if a fatal error occurs
     */
    public boolean read() {
        File file = new File(path);

        fileSize = file.length();


        if (!onReadBegin()) {
            return false;
        }

        Boolean readyToReadDataInstances = false;

        LineNumberReader br = null;

        try {
            FileReader fr = new FileReader(path);
            if (!fr.ready()) {
                return false; // Failed to read file
            }
            br = new LineNumberReader(fr);

            String line;
            while ((line = br.readLine()) != null) {
                currentLine = br.getLineNumber();

                // Ignore empty and comment lines
                if (line.isEmpty() || line.charAt(0) == '%') {
                    continue;
                } // Parse declarations (e.g., @ATTRIBUTE, @DATA)
                else if (line.charAt(0) == '@') {
                    List<String> declFields =
                            parseDelimitedAndQuotedString(line, ' ');
                    if (declFields.isEmpty()) {
                        throw new RuntimeException("decl field empty!");
                    } else if (declFields.get(0).equalsIgnoreCase("@RELATION")) {
                        // Do nothing; fall through
                    } else if (declFields.get(0).equalsIgnoreCase("@ATTRIBUTE")) {
                        if (!onAttribute(declFields)) {
                            return false; // Fatal error
                        }
                        ++currentFieldIndex;
                    } else if (declFields.get(0).equalsIgnoreCase("@DATA")) {
                        if (!onData(declFields)) {
                            return false; // Fatal error
                        }
                        readyToReadDataInstances = true;
                    } else {
                        throw new RuntimeException("Invalid ARFF file!");
                    }
                } // Parse data instances
                else if (readyToReadDataInstances) {
                    List<String> cols = parseDelimitedAndQuotedString(line, ',');

                    if (!onDataInstance(cols, line)) {
                        return false; // Fatal error
                    }
                }

            }
        } catch (IOException e) {
            OutputUtil.log("Failed to open file '" + path + "'.");

            return false; // Failed to read file
        } finally {
            // Release the stream
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return onReadEnd();
    }

    public boolean onReadBegin() {
        validInstances = 0;
        return true;
    }

    public boolean onReadEnd() {
        return true;
    }

    public void onSaveEnd() {
    }

    public Boolean onAttribute(final List<String> fields) {
        return true;
    }

    public Boolean onData(final List<String> fields) {
        return true;
    }

    public Boolean onDataInstance(final List<String> cols, final String raw_line) {
        return true;
    }

    protected Boolean verifyAttributeDataType(List<String> fields,
            String expectedDatatype) {
        if (fields.size() < 3) {
            return false;
        } else if (!fields.get(2).equalsIgnoreCase(expectedDatatype)) {
            return false;
        } else {
            return true;
        }
    }

    protected String[] parseNominalAttribute(String attributeName,
            List<String> fields) {
        if (fields.size() < 3) {
            return null;
        }

        String nominalValuesDelimitedList = fields.get(2);
        if (nominalValuesDelimitedList.length() < 2
                || nominalValuesDelimitedList.charAt(0) != '{'
                || nominalValuesDelimitedList.charAt(nominalValuesDelimitedList.length() - 1) != '}') {
            return null;
        }

        // Remove braces
        nominalValuesDelimitedList = nominalValuesDelimitedList.substring(1,
                nominalValuesDelimitedList.length() - 1);

        String[] nominalValues = nominalValuesDelimitedList.split(",");
        if (nominalValues == null || nominalValues.length == 0) {
            return null;
        }

        return nominalValues;
    }

    /**
     * Splits @ATTRIBUTE and @DATA instance rows into separate fields
     * <P>
     * Nothing in the ARFF spec explains how to embed quotes and
     * commas in field values.  Here are my rules:
     *
     * - Whitespace that is not part of the field value is ignored.
     * - If a field value is quoted, whitespace and commas inside
     *   the field value are not treated as delimiters.
     * - Any character can be escaped to prevent it from being
     *   used to delimit fields, quote field values, or specify
     *   hierarchical concern names:
     *
     *   blah,blah,Eaddy\,\ Marc	parsed as -> "Eaddy, Marc"
     *   blah,blah,"Eaddy, Marc"	parsed as -> "Eaddy, Marc"
     *   blah,blah,what\\is\\up     parsed as -> "what\\is\\up"
     *   	(slashes will not be used to create a hierarchical concern)
     */
    public List<String> parseDelimitedAndQuotedString(String line, char delimiter) {
        List<String> list = new ArrayList<String>();

        StringBuffer cur = new StringBuffer();

        boolean inQuote = false;

        int len = line.length();

        for (int i = 0; i < len; ++i) {
            char c = line.charAt(i);

            // Note: A delimiter may appear inside a field value either
            // by quoting the field value or by escaping the delimiter

            // Handle escaped characters: \,
            if (c == '\\') {
                if (i + 1 >= len) {
                    list.clear();
                    return list;
                }

                // Append entire escape sequence
                cur.append('\\');
                cur.append(line.charAt(++i));
            } else if (c == delimiter && !inQuote) {
                list.add(cur.toString().trim());
                cur.setLength(0);
            } else if (c == '\"' || c == '\'') {
                if (inQuote
                        && i + 1 < len
                        && line.charAt(i + 1) != delimiter) {
                    list.clear();
                    return list;
                }

                inQuote = !inQuote;
            } else {
                cur.append(c);
            }
        }

        if (inQuote) {
            list.clear();
            return list;
        }

        // Add the last one
        list.add(cur.toString().trim());
        return list;
    }

    /**
     * Escape forward slashes (/) and commas (,) so they are
     * not confused to be concern path or concern list delimiters
     * @param s
     * @return
     */
    static public String escape(final String s) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c == '/' || c == ',') {
                buf.append('\\');
            }

            buf.append(c);
        }

        return buf.toString();
    }

    /*
     * Replace all escaped characters with their character equivalent
     */
    static public String unescape(final String s) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c == '\\') {
                // Skip over the escape to get the escaped character
                // Be careful!
                c = s.charAt(++i);
            }

            buf.append(c);
        }

        return buf.toString();
    }
}
