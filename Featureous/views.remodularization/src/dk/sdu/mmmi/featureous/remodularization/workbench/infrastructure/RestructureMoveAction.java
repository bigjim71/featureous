/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.workbench.infrastructure;

import dk.sdu.mmmi.featureous.remodularization.workbench.Container;
import dk.sdu.mmmi.featureous.remodularization.workbench.PackageWidget;
import dk.sdu.mmmi.featureous.remodularization.workbench.UMLClassWidget;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author ao
 */
public final class RestructureMoveAction extends WidgetAction.LockedAdapter {

    private MoveStrategy strategy;
    private MoveProvider provider;
    private PackageWidget originalParent = null;
    private Widget movingWidget = null;
    private Point dragSceneLocation = null;
    private Point originalSceneLocation = null;
    private Point initialMouseLocation = null;

    public RestructureMoveAction(MoveStrategy strategy, MoveProvider provider) {
        this.strategy = strategy;
        this.provider = provider;
    }

    protected boolean isLocked() {
        return movingWidget != null;
    }

    public State mousePressed(Widget widget, WidgetMouseEvent event) {
        if (isLocked()) {
            return State.createLocked(widget, this);
        }
        if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 1) {
            originalParent = (PackageWidget) widget.getParentWidget();
            Point orgLoc = new Point(widget.getLocation());
            originalParent.removeClass(widget);
            originalParent.recolorBasedOnChildAffinity();
            widget.getScene().addChild(widget);

            movingWidget = widget;
            initialMouseLocation = event.getPoint();
            originalSceneLocation = provider.getOriginalLocation(widget);
            if (originalSceneLocation == null) {
                originalSceneLocation = new Point();
            }
            dragSceneLocation = widget.convertLocalToScene(event.getPoint());
            provider.movementStarted(widget);

            Point p2 = originalParent.getLocation();
            provider.setNewLocation(widget, strategy.locationSuggested(widget, originalSceneLocation,
                    new Point(orgLoc.x + p2.x, orgLoc.y + p2.y)));
            return State.createLocked(widget, this);
        }
        return State.REJECTED;
    }

    public State mouseReleased(Widget widget, WidgetMouseEvent event) {
        boolean state;
        if (initialMouseLocation != null && initialMouseLocation.equals(event.getPoint())) {
            state = true;
        } else {
            state = move(widget, event.getPoint());
        }
        if (state) {
            movingWidget = null;
            dragSceneLocation = null;
            originalSceneLocation = null;
            initialMouseLocation = null;
            provider.movementFinished(widget);

            Point orgLoc = new Point(widget.getLocation());
            Widget dropTarget = findDropTarget(widget);
            if (dropTarget == null) {
                dropTarget = originalParent;
            }

            widget.getScene().removeChild(widget);
            PackageWidget acw = (PackageWidget) dropTarget;
            acw.doOpen();
            acw.addClass((UMLClassWidget) widget);
            acw.recolorBasedOnChildAffinity();

            provider.setNewLocation(widget,
                    new Point(orgLoc.x - dropTarget.getLocation().x, orgLoc.y - dropTarget.getLocation().y));
            ((StringGraphScene)widget.getScene()).notifyRestructured();
        }
        return state ? State.CONSUMED : State.REJECTED;
    }

    public State mouseDragged(Widget widget, WidgetMouseEvent event) {
        return move(widget, event.getPoint()) ? State.createLocked(widget, this) : State.REJECTED;
    }

    private boolean move(Widget widget, Point newLocation) {
        if (movingWidget != widget) {
            return false;
        }
        initialMouseLocation = null;
        newLocation = widget.convertLocalToScene(newLocation);
        Point location = new Point(originalSceneLocation.x + newLocation.x - dragSceneLocation.x, originalSceneLocation.y + newLocation.y - dragSceneLocation.y);
        provider.setNewLocation(widget, strategy.locationSuggested(widget, originalSceneLocation, location));
        return true;
    }

    private Widget findDropTarget(Widget widget) {
        Scene scene = widget.getScene();
        for (Widget layer : scene.getChildren()) {
            for (Widget child : layer.getChildren()) {
                if (child instanceof Container) {
                    Rectangle r = new Rectangle(child.getLocation(), child.getBounds().getSize());
                    if (r.contains(widget.getLocation())) {
                        return child;
                    }
                }
            }
        }
        return null;
    }
}