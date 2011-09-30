/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.util.arff;

import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.core.ui.OutputUtil;
import dk.sdu.mmmi.featuretracer.lib.featureLocation.model.FeatureTraceModel;
import dk.sdu.mmmi.featuretracer.lib.featureLocation.model.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses AARF files that contain concern link data.
 * <P>
 * We expect three &#064;ATTRIBUTE declarations, followed by a &#064;DATA
 * declaration, followed by the instance data.
 * <P>
 * Here's a minimal example:
 * <P>
 * &#064;ATTRIBUTE entity-name string<BR>
 * &#064;ATTRIBUTE entity-type {method,field}<BR>
 * &#064;ATTRIBUTE concern-list string<BR>
 * &#064;DATA<BR>
 * Logging,Java.util.Logging.log(),method<BR>
 * <P>
 * Other than these requirements, we try to be as permissive as possible and
 * ignore stuff we don't understand.
 * 
 * @author eaddy
 */
public class FtmARFFFile extends ARFFFile {

    private int entityNameCol = -1;
    private static final String ENTITY_NAME_ATTR_NAME = "entity-name";
    private int entityTypeCol = -1;
    private static final String ENTITY_TYPE_ATTR_NAME = "entity-type";
    private int concernListCol = -1;
    private static final String CONCERN_LIST_ATTR_NAME = "concern-list";
    private String[] entityTypes = null;
    private Map<String, TraceModel> ftms = new HashMap<String, TraceModel>();

    public FtmARFFFile(final String path) {
        super(path);
    }

    public Map<String, TraceModel> getFtms() {
        return ftms;
    }

    @Override
    public Boolean onAttribute(final List<String> fields) {
        if (fields.get(1).equalsIgnoreCase(CONCERN_LIST_ATTR_NAME)) {
            if (concernListCol != -1) {
                return true; // Already assigned, ignore
            } else if (!verifyAttributeDataType(fields, "string")) {
                return false;
            } else {
                concernListCol = currentFieldIndex;
            }
        } else if (fields.get(1).equalsIgnoreCase(ENTITY_NAME_ATTR_NAME)) {
            if (entityNameCol != -1) {
                return true; // Already assigned, ignore
            }
            if (!verifyAttributeDataType(fields, "string")) {
                return false;
            } else {
                entityNameCol = currentFieldIndex;
            }
        } else if (fields.get(1).equalsIgnoreCase(ENTITY_TYPE_ATTR_NAME)) {
            if (entityTypeCol != -1) {
                return true; // Already assigned, ignore
            }
            entityTypes = parseNominalAttribute(ENTITY_TYPE_ATTR_NAME, fields);

            if (entityTypes == null || entityTypes.length == 0) {
                return false;
            }

            for (String entityType : entityTypes) {
                if (!isValidEntityType(entityType)) {
                    return false;
                }
            }

            entityTypeCol = currentFieldIndex;
        }

        return true;
    }

    @Override
    public Boolean onDataInstance(final List<String> cols, final String raw_line) {
        if (concernListCol < 0 || entityTypeCol < 0 || entityNameCol < 0) {
            return false; // Halt further processing
        }

        assert currentFieldIndex >= 3;

        // Make sure there are enough columns

        int maxCol = Math.max(Math.max(concernListCol, entityTypeCol),
                entityNameCol);

        if (maxCol >= cols.size()) {
            return true; // Continue processing
        }

        // Parse List of concerns associated with the entity

        String concernList = cols.get(concernListCol);
        if (concernList.isEmpty()) {
            return true; // Ignore empty concerns
        } else if (IsNullOrEmpty(concernList, raw_line, CONCERN_LIST_ATTR_NAME)) {
            return true; // Continue processing
        }
        // Parse Entity Name (e.g., String.toString(int\, boolean))
        // May contain escaped characters.

        String entityName = cols.get(entityNameCol);
        if (IsNullOrEmpty(entityName, raw_line, ENTITY_NAME_ATTR_NAME)) {
            return true; // Continue processing
        }
        // Parse Entity Type (e.g., "method")

        String entityType = cols.get(entityTypeCol);
        if (IsNullOrEmpty(entityType, raw_line, ENTITY_TYPE_ATTR_NAME)) {
            return true; // Continue processing
        }

        if (!isValidEntityType(entityType)) {
            return true; // Continue processing
        }
        // we will take only methods!
        if (!entityType.equals("method")) {
            return true;
        }

        // Parse <package>.<type>.<field>
        // and   <package>.<type>."<static initializer>"
        // and   <package>.<type>.<method>(<args>)

        int lastDot = -1;

        int firstParen = entityName.indexOf('(');
        if (firstParen != -1) {
            // Make sure we don't accidentally find a dot within
            // the argument list
            lastDot = entityName.lastIndexOf('.', firstParen - 1);
        } else {
            return true;
        }

        assert lastDot != -1; // Members always have at least one dot

        // Extract <package>.<type>
        String pkgDotType = entityName.substring(0, lastDot);
        if (pkgDotType == null) {
            return true;
        }

        String methodName = entityName.substring(lastDot + 1, firstParen);

        for (String concernPath : parseDelimitedAndQuotedString(concernList, ',')) {
            TraceModel ftm = ftms.get(concernPath);
            if (ftm == null) {
                ftm = new TraceModel(new FeatureTraceModel(concernPath), super.path);
            }
            if (pkgDotType.contains("$")) {
                // For now we do not support this
                OutputUtil.log("$");
                return true;
            }
            if (ftm.getClass(pkgDotType) == null) {
                ftm.addClass(new ClassModel(new Type(pkgDotType,
                        pkgDotType.substring(0, pkgDotType.lastIndexOf(".")),
                        null)));
            }

            ClassModel cm = ftm.getClass(pkgDotType);
            if (cm == null) {
                throw new RuntimeException();
            }
            cm.addMethod(entityName);
            ftms.put(concernPath, ftm);
        }

        return true;
    }

    public void save() {
    }

    private Boolean IsNullOrEmpty(final String value,
            final String line,
            final String name) {
        if (value == null || value.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isValidEntityType(final String entityType) {
        if (entityType.equalsIgnoreCase("method")
                || entityType.equalsIgnoreCase("field")
                || entityType.equalsIgnoreCase("type")
                || entityType.equalsIgnoreCase("initializer")) {
            return true;
        } else {
            return false;
        }
    }
}
