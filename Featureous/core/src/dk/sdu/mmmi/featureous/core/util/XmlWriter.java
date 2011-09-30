/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.core.util;

import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.TraceModel;

public class XmlWriter {

    public static String traceToXml(TraceModel traceModel) {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        xml += "<trace name=\"" + traceModel.getName() + "\">\n";
        for (ClassModel classModel : traceModel.getClassSet()) {
            xml += classToXml(classModel);
        }
        xml += "</trace>";
        return xml;
    }

    private static String classToXml(ClassModel classModel) {
        String xml = "  <class name=\"" + classModel.getName() + "\">\n";
        for (String instanceId : classModel.getInstancesUsed()) {
            xml += "    <instance id=\"" + instanceId + "\"/>\n";
        }
        for (String methodName : classModel.getAllMethods()) {
            xml += "    <method name=\"" + methodName + "\"/>\n";
        }
        xml += "  </class>\n";
        return xml;
    }
}
