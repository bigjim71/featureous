/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.workbench.infrastructure;

import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

import java.util.HashSet;
import java.util.Set;

/**
 * @author David Kaspar
 */
public class StringGraphScene extends GraphScene<String, String> {

    private LayerWidget mainLayer;
    private LayerWidget connectionLayer;
    private Widget nextWidget;
    
    public StringGraphScene() {
        mainLayer = new LayerWidget(this);
        connectionLayer = new LayerWidget(this);
        addChild(mainLayer);
        addChild(connectionLayer);

    }

    public void setNextConstructedNode(Widget widget){
        this.nextWidget = widget;
    }
    
    protected Widget attachNodeWidget(String widget) {
        if(nextWidget==null){
            throw new RuntimeException("First set the next widget!");
        }
        Widget created = nextWidget;
        nextWidget = null;
        mainLayer.addChild(created);
        
        return created;
    }

    protected Widget attachEdgeWidget(String edge) {
        ConnectionWidget connectionWidget = new ConnectionWidget(this);
        connectionLayer.addChild(connectionWidget);
        return connectionWidget;
    }

    protected void attachEdgeSourceAnchor(String edge, String oldSourceNode, String sourceNode) {
        ((ConnectionWidget) findWidget(edge)).setSourceAnchor(AnchorFactory.createRectangularAnchor(findWidget(sourceNode)));
    }

    protected void attachEdgeTargetAnchor(String edge, String oldTargetNode, String targetNode) {
        ((ConnectionWidget) findWidget(edge)).setTargetAnchor(AnchorFactory.createRectangularAnchor(findWidget(targetNode)));
    }
    
    public LayerWidget getMainLayer() {
        return mainLayer;
    }

    public LayerWidget getConnectionLayer() {
        return connectionLayer;
    }
    
    public void notifyRestructured(){
        for(RestructuringListener res : resListeners){
            res.sceneRestructured();
        }
    }

    public void addRestructuringListener(RestructuringListener listener) {
        resListeners.add(listener);
    }
    
    private Set<RestructuringListener> resListeners = new HashSet<RestructuringListener>();
}
