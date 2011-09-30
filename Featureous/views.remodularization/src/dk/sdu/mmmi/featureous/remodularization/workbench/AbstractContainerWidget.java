/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.workbench;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.model.StateModel;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author ao
 */
public abstract class AbstractContainerWidget extends Widget implements Container {

    private WidgetAction popupMenuAction = ActionFactory.createPopupMenuAction(new ContainerPopupProvider());
    private WidgetAction moveAction = ActionFactory.createMoveAction();
    private LabelWidget name;
    private StateModel model = new StateModel(2);
    private final ImageWidget classImage;

    protected abstract Image getIcon();

    public AbstractContainerWidget(Scene scene, String fullName, boolean editable) {
        super(scene);
        setBorder(new LineBorder(Color.BLACK, 1, true));
        setOpaque(true);
        setBackground(new Color(250, 235, 215));
        setCheckClipping(true);

        classImage = new ImageWidget(scene);
        classImage.setImage(getIcon());
        addChild(classImage);
        classImage.setPreferredLocation(new Point(0, 0));

        name = new LabelWidget(scene);
        name.setLabel(fullName);
        name.setFont(scene.getDefaultFont().deriveFont(Font.BOLD));
        addChild(name);
        name.setPreferredLocation(new Point(20, classImage.getImage().getHeight(null)-3));

        getActions().addAction(moveAction);
        if(editable){
            getActions().addAction(popupMenuAction);
        }
        
        {
            WidgetAction collapse = ActionFactory.createSelectAction(new SelectProvider() {

                public boolean isAimingAllowed(Widget widget, Point point, boolean bln) {
                    return false;
                }

                public boolean isSelectionAllowed(Widget widget, Point point, boolean bln) {
                    return true;
                }

                public void select(Widget widget, Point point, boolean bln) {
                    // modifies the state of the state model
                    model.increase();
                }
            });
            getActions().addAction(collapse);
        }
    }

    public void doOpen() {
        model.setBooleanState(false);
        model.increase();
    }

    private int maxInRow = 3;
    private int width = 150;
    private int height = 40;
    
    public void addClass(UMLClassWidget classWidget) {
        addChild(classWidget);
        classWidget.setParent(this, model);
        classWidget.setPreferredLocation(new Point(((getChildren().size()-3)%maxInRow)*width, 30 + ((getChildren().size()-3)/maxInRow)*height ));
    }

    public String getName() {
        return name.getLabel();
    }

    public void setName(String name) {
        this.name.setLabel(name);
    }

    @Override
    public String toString() {
        return getName();
    }

    public void removeClass(Widget widget) {
        removeChild(widget);
    }

    private class LabelTextFieldEditor implements TextFieldInplaceEditor {

        public boolean isEnabled(Widget widget) {
            return true;
        }

        public String getText(Widget widget) {
            return ((LabelWidget) widget).getLabel();
        }

        public void setText(Widget widget, String text) {
            ((LabelWidget) widget).setLabel(text);
        }
    }

    private class ContainerPopupProvider implements PopupMenuProvider {

        public JPopupMenu getPopupMenu(Widget widget, Point point) {
            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem rename = new JMenuItem("Rename " + (widget).toString());
            popupMenu.add(rename);
            rename.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    InplaceEditorProvider.EditorController inplaceEditorController =
                            ActionFactory.getInplaceEditorController(
                            ActionFactory.createInplaceEditorAction(new LabelTextFieldEditor()));
                    inplaceEditorController.openEditor(name);
                }
            });
            return popupMenu;
        }
    }
}
