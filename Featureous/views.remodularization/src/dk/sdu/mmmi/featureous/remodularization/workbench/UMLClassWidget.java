/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.remodularization.workbench;

import dk.sdu.mmmi.featureous.core.affinity.Affinity;
import dk.sdu.mmmi.featureous.remodularization.workbench.infrastructure.LevelOfDetailsComponentWidget;
import dk.sdu.mmmi.featureous.remodularization.workbench.infrastructure.RestructureMoveAction;
import dk.sdu.mmmi.featureous.remodularization.workbench.infrastructure.RestructuringProxyAnchor;
import dk.sdu.mmmi.featureous.remodularization.workbench.infrastructure.SceneSupport;
import dk.sdu.mmmi.featureous.remodularization.workbench.infrastructure.ShadowBorder;
import dk.sdu.mmmi.srcUtils.nb.NavigationUtils;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.IOException;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.*;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.LineBorder;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.model.StateModel;

public class UMLClassWidget extends Widget implements StateModel.Listener {

    private static final Image IMAGE_OPEN = Utilities.loadImage("dk/sdu/mmmi/featureous/remodularization/workbench/icons/open.png"); // NOI18N
    private static final Image IMAGE_CLOSED = Utilities.loadImage("dk/sdu/mmmi/featureous/remodularization/workbench/icons/closed.png"); // NOI18N
    private static final Image IMAGE_CLASS = Utilities.loadImage("dk/sdu/mmmi/featureous/remodularization/workbench/icons/class.png"); // NOI18N
    private static final Image IMAGE_INTERFACE = Utilities.loadImage("dk/sdu/mmmi/featureous/remodularization/workbench/icons/interface.png"); // NOI18N
    private static final Image IMAGE_MEMBER = Utilities.loadImage("dk/sdu/mmmi/featureous/remodularization/workbench/icons/variablePublic.png"); // NOI18N
    private static final Image IMAGE_OPERATION = Utilities.loadImage("dk/sdu/mmmi/featureous/remodularization/workbench/icons/methodPublic.png"); // NOI18N
    private WidgetAction moveAction = ActionFactory.createMoveAction();
    private WidgetAction popupMenuAction = ActionFactory.createPopupMenuAction(new MyPopupMenuProvider());
    private final String fullName;
    private RestructuringProxyAnchor anchor;
    private StateModel containerModel;
    private ImageWidget minimizeWidget;
    private StateModel membersMinimizedModel = new StateModel(2);
    private LabelWidget className;
    private Widget members;
    private Widget operations;
    private static final Border BORDER_4 = BorderFactory.createEmptyBorder(4);
    private final Widget classHeader;
    private final LabelWidget affinityLabel;
    private Affinity affinity;
    private JComponent editor;
    private int umlWidth = -1;
    private int umlHeight = -1;
    private final Widget uml;

    public UMLClassWidget(Scene scene, String fullName, boolean isInterface) {
        super(scene);
        this.fullName = fullName;
        setLayout(LayoutFactory.createOverlayLayout());
        setBorder(BorderFactory.createSwingBorder(scene, new ShadowBorder(3, 3)));
        setOpaque(true);
        setCheckClipping(true);


        classHeader = new Widget(scene);
        classHeader.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 1));
        classHeader.setBorder(BORDER_4);
        classHeader.setToolTipText(fullName);

        ImageWidget classImage = new ImageWidget(scene);
        if (isInterface) {
            classImage.setImage(IMAGE_INTERFACE);
        } else {
            classImage.setImage(IMAGE_CLASS);
        }

        affinityLabel = new LabelWidget(scene, "  ");
        affinityLabel.setOpaque(true);
        affinityLabel.setBackground(Color.white);
        classHeader.addChild(affinityLabel);

        classHeader.addChild(classImage);

        className = new LabelWidget(scene);
        className.setFont(scene.getDefaultFont().deriveFont(Font.BOLD));
        className.setLabel(getShortName(fullName));
        classHeader.addChild(className);
        membersMinimizedModel.setBooleanState(true);
        minimizeWidget = new ImageWidget(scene, IMAGE_CLOSED);
        minimizeWidget.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        minimizeWidget.getActions().addAction(new ToggleMinimizedAction());
        classHeader.addChild(minimizeWidget);
        uml = new LevelOfDetailsWidget(scene, 0.0, 0.1, 3.0, 4);
        uml.setLayout(LayoutFactory.createVerticalFlowLayout());
        uml.addChild(classHeader);

        uml.addChild(new SeparatorWidget(scene, SeparatorWidget.Orientation.HORIZONTAL));

        members = new Widget(scene);
        members.setLayout(LayoutFactory.createVerticalFlowLayout());
        members.setOpaque(false);
        members.setBorder(BORDER_4);
        uml.addChild(members);

        uml.addChild(new SeparatorWidget(scene, SeparatorWidget.Orientation.HORIZONTAL));

        operations = new Widget(scene);
        operations.setLayout(LayoutFactory.createVerticalFlowLayout());
        operations.setOpaque(false);
        operations.setBorder(BORDER_4);
        uml.addChild(operations);
        addChild(uml);

        createActions(SceneSupport.ShiftKeySwitchToolAction.REFACTOR_CLASS).addAction(
                new RestructureMoveAction(ActionFactory.createFreeMoveStrategy(), ActionFactory.createDefaultMoveProvider()));

        createActions(SceneSupport.ShiftKeySwitchToolAction.ALIGN).addAction(moveAction);
        getActions().addAction(popupMenuAction);

        membersMinimizedModel.addListener(this);

        stateChanged();
    }

    public Affinity getAffinity() {
        return affinity;
    }

    public void setAffinity(Affinity aff) {
        affinity = aff;
        affinityLabel.setBackground(affinity.color);
    }

    public void stateChanged() {
        if (containerModel != null) {
            this.setVisible(containerModel.getBooleanState());
        }

        boolean minimized = membersMinimizedModel.getBooleanState();
        Rectangle rectangle = minimized ? new Rectangle() : null;
        if (this.getScene().isVisible()) {
            if (umlHeight == -1 && uml.getBounds() != null) {
                umlWidth = uml.getBounds().width;
                umlHeight = uml.getBounds().height;
            }
            getScene().getSceneAnimator().animatePreferredBounds(members, minimized ? rectangle : null);
            getScene().getSceneAnimator().animatePreferredBounds(operations, minimized ? rectangle : null);
        }
        if (editor == null && !minimized && umlHeight != -1) {
            try {
                editor = NavigationUtils.getEditorForClass(getParentWidget().toString(), fullName);
                if (editor != null) {
                    editor.setBorder(new LineBorder(Color.black));
//                JScrollPane editorPane = new JScrollPane(editor);
                    editor.setMinimumSize(new Dimension(umlWidth, umlHeight));
                    editor.setMaximumSize(new Dimension(umlWidth, umlHeight));
                    editor.setPreferredSize(new Dimension(umlWidth, umlHeight));
                    Widget lod = new LevelOfDetailsComponentWidget(getScene(), editor, 4, 4, 15.0, 15.1);
                    addChild(lod);
                }
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        minimizeWidget.setImage(minimized ? IMAGE_CLOSED : IMAGE_OPEN);
    }

    public void setParent(AbstractContainerWidget newParent, StateModel newModel) {
        if (containerModel != null) {
            containerModel.removeListener(this);
        }
        this.containerModel = newModel;

        if (anchor == null) {
            Anchor newAnchor = AnchorFactory.createRectangularAnchor(this);
            RestructuringProxyAnchor proxyAnchor = new RestructuringProxyAnchor(newModel,
                    AnchorFactory.createRectangularAnchor(newParent), newAnchor);
            this.anchor = proxyAnchor;
        } else {
            anchor.replaceContainer(newParent);
            anchor.setModel(newModel);
        }

        setVisible(containerModel.getBooleanState());

        this.containerModel.addListener(this);
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public void addMember(Widget memberWidget) {
        members.addChild(memberWidget);
    }

    public void addOperation(Widget operationWidget) {
        operations.addChild(operationWidget);
    }

    public Widget createMember(final String member, Color color) {
        Scene scene = getScene();
        Widget widget = new Widget(scene) {

            @Override
            public String toString() {
                //HACK
                return member;
            }
        };
        widget.setLayout(LayoutFactory.createHorizontalFlowLayout());

        Widget affinity = new LabelWidget(scene, "  ");
        affinity.setOpaque(true);
        affinity.setBackground(color);
        widget.addChild(affinity);

        ImageWidget imageWidget = new ImageWidget(scene);
        imageWidget.setImage(UMLClassWidget.IMAGE_MEMBER);
        widget.addChild(imageWidget);

        LabelWidget labelWidget = new LabelWidget(scene);
        int lastDot = member.lastIndexOf(".");
        labelWidget.setLabel(member.substring(lastDot + 1));
        widget.addChild(labelWidget);

        labelWidget.setToolTipText(member);

        return widget;
    }

    public Widget createOperation(final String operation, Color color) {
        Scene scene = getScene();
        Widget widget = new Widget(scene) {

            @Override
            public String toString() {
                //HACK
                return operation;
            }
        };
        widget.setLayout(LayoutFactory.createHorizontalFlowLayout());

        Widget affinity = new LabelWidget(scene, "  ");
        affinity.setOpaque(true);
        affinity.setBackground(color);
        widget.addChild(affinity);

        ImageWidget imageWidget = new ImageWidget(scene);
        imageWidget.setImage(UMLClassWidget.IMAGE_OPERATION);
        widget.addChild(imageWidget);

        LabelWidget labelWidget = new LabelWidget(scene);
        int firstPar = operation.indexOf("(");
        int lastDot = operation.substring(0, firstPar).lastIndexOf(".");
        labelWidget.setLabel(operation.substring(lastDot + 1, firstPar) + "()");
        widget.addChild(labelWidget);

        labelWidget.setToolTipText(operation);

        return widget;
    }

    public List<Widget> getMembers() {
        return members.getChildren();
    }

    public List<Widget> getOperations() {
        return operations.getChildren();
    }

    private static String getShortName(String fullName) {
        int lastDot = fullName.lastIndexOf(".");
        if (lastDot != -1) {
            return fullName.substring(lastDot + 1);
        } else {
            return fullName;
        }
    }

    @Override
    public String toString() {
        return fullName;
    }

    public String getFullName() {
        return fullName;
    }

    private final class ToggleMinimizedAction extends WidgetAction.Adapter {

        public State mousePressed(Widget widget, WidgetMouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON1) {
                membersMinimizedModel.toggleBooleanState();
                return State.CONSUMED;
            }
            return State.REJECTED;
        }
    }

    public class MyPopupMenuProvider implements PopupMenuProvider {

        public JPopupMenu getPopupMenu(final Widget widget, Point localLocation) {
            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem gotoClass = new JMenuItem("Open " + (widget).toString());
            gotoClass.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        NavigationUtils.openClass(getParentWidget().toString(), fullName);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
            popupMenu.add(gotoClass);
            return popupMenu;
        }
    }
}
