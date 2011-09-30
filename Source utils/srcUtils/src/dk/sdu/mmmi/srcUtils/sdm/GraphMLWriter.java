/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.sdm;

import dk.sdu.mmmi.srcUtils.sdm.model.JDependency;
import dk.sdu.mmmi.srcUtils.sdm.model.JPackage;
import dk.sdu.mmmi.srcUtils.sdm.model.JType;
import dk.sdu.mmmi.srcUtils.sdm.model.StaticDependencyModel;
import org.jdom.*;
import org.jdom.output.*;

/**
 * Util to convert nodes and edges into grapml document
 * @author rbolze, ao
 */
public class GraphMLWriter {

    public static void convert(String fileName, StaticDependencyModel sdm) {
        // graphml document header
        Element graphml = new Element("graphml");
        Document document = new Document(graphml);
        Namespace xsi = Namespace.getNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");
        Namespace schemLocation = Namespace.getNamespace("schemLocation","http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd");
        Namespace y = Namespace.getNamespace("y","http://www.yworks.com/xml/graphml");

        // add Namespace
        graphml.addNamespaceDeclaration(xsi);
        graphml.addNamespaceDeclaration(schemLocation);
        graphml.addNamespaceDeclaration(y);

        // keys for graphic representation
        Element key_d0 = new Element("key");
        key_d0.setAttribute("id","d0");
        key_d0.setAttribute("for","node");
        key_d0.setAttribute("yfiles.type","nodegraphics");
        graphml.addContent(key_d0);
        Element key_d1 = new Element("key");
        key_d1.setAttribute("id","d1");
        key_d1.setAttribute("for","node");
        key_d1.setAttribute("attr.name","name");
        key_d1.setAttribute("attr.type","string");
        graphml.addContent(key_d1);

        Element graph = new Element("graph");
        graph.setAttribute("id","G");
        graph.setAttribute("edgedefault","directed");
        graphml.addContent(graph);

        for(JPackage p : sdm.getPackages()){
            addNode(p.getQualName().hashCode(), p.getQualName(), graph, graphml);
            for(JType t : p.getTopLevelTypes()){
                addNode(t.getQualName().hashCode(), t.getQualName(), graph, graphml);
            }
        }

        for(JPackage p : sdm.getPackages()){
            for(JType t : p.getTopLevelTypes()){
                int sId = t.getQualName().hashCode();
                for(JDependency d : t.getDependencies()){
                    int tId = d.getReferencedType().getQualName().hashCode();
                    addEdge(d.toString().hashCode(), sId, tId, graph);
                }
            }
        }

        printAll(document);
        save(fileName,document);
    }

    /**
     * add a edge to the graphML document
     * @param id the id of the edge
     * @param idSource the id of the node source
     * @param idTarget the id of the node target
     * @param source the URL of the source
     * @param target the URL of the target
     * @param graph the graph element of the graphML document
     * @param graphml the graphml element of the graphML document
     */
    public static void addEdge(int id, int idSource, int idTarget, Element graph){
        Element edge = new Element("edge");
        edge.setAttribute("id","e"+id);
        edge.setAttribute("source","n"+idSource);
        edge.setAttribute("target","n"+idTarget);
        graph.addContent(edge);
    }

    /**
     * add a node to the graphML document
     * @param id the id of the node
     * @param url the URL of the node
     * @param graph the graph element of the graphML document
     * @param graphml the graphml element of the graphML document
     */
    public static void addNode(int id, String name, Element graph, Element graphml){
        Element node = new Element("node");
        node.setAttribute("id","n"+id);
        Element data0 = new Element("data");
        data0.setAttribute("key","d0");
        node.addContent(data0);
        Namespace yns = graphml.getNamespace("y");
        Element shapeNode = new Element("ShapeNode",yns);
        data0.addContent(shapeNode);
        Element nodeLabel = new Element("NodeLabel",yns);
        nodeLabel.setAttribute("visible","true");
        nodeLabel.setAttribute("autoSizePolicy","content");
        nodeLabel.setText("n"+id);
        //System.out.println(url);
        shapeNode.addContent(nodeLabel);
        Element data1 = new Element("data");
        data1.setAttribute("key","d1");
        data1.setText(name);
        node.addContent(data1);

        graph.addContent(node);
    }
    /**
     * print the content of the document
     * @param doc xml document
     */
    public static void printAll(Document doc) {
        try{
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            outputter.output(doc, System.out);
        } catch (java.io.IOException e){
            e.printStackTrace();
        }
    }

    /**
     * write the xml document into file
     * @param file the file name
     * @param doc xml document
     */
    public static void save(String file, Document doc) {
        System.out.println("### document saved in : "+file);
        try {
            XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
            sortie.output(doc, new java.io.FileOutputStream(file));
        } catch (java.io.IOException e){}
    }
}
