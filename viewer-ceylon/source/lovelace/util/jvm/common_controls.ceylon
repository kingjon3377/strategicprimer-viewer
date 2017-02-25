import java.awt.event {
    ActionListener,ActionEvent,
    KeyEvent,
    InputEvent
}
import javax.swing {
    JButton,
    JOptionPane,
    GroupLayout,
    JComboBox,
    JEditorPane,
    ComboBoxModel,
    BoxLayout,
    Box,
    JPanel,
    JSplitPane,
    KeyStroke,
    Action,
    JComponent,
    InputMap
}
import java.awt {
    Component,
    Container,
    Color,
    Dimension,
    BorderLayout
}
import lovelace.util.common {
    todo
}
import java.lang {
    IllegalStateException
}
import ceylon.interop.java { javaString }
"A factory method to construct a button and add listeners to it in one step."
shared JButton listenedButton("The text to put on the button" String text,
        Anything(ActionEvent)|ActionListener* listeners) {
    JButton retval = JButton(text);
    for (listener in listeners) {
        if (is ActionListener listener) {
            retval.addActionListener(listener);
        } else {
            retval.addActionListener(listener);
        }
    }
    return retval;
}
"Show an error dialog to the user."
shared void showErrorDialog(
        "The parent component for the dialog. [[JOptionPane]] doesn't seem to care if it
         is null."
        Component? parent,
        "What to title the dialog."
        String title,
        "The error message to show the user."
        String message) {
    JOptionPane.showMessageDialog(parent, message, title, JOptionPane.errorMessage);
}
"An extension to [[GroupLayout]] to provide additional methods to make initialization
 less verbose and more functional in style."
shared class FunctionalGroupLayout(Container host) extends GroupLayout(host) {
    "Add components and/or groups to a group."
    T initializeGroup<T>(T group, Component|Group* components) given T satisfies Group {
        for (component in components) {
            switch (component)
            case (is Component) { group.addComponent(component); }
            case (is Group) { group.addGroup(component); }
        }
        return group;
    }
    "Factory for a parallel group."
    todo("Rename to parallelGroupOf()")
    shared ParallelGroup createParallelGroupOf(Component|Group* components) =>
        initializeGroup(createParallelGroup(), *components);
    "Factory for a sequential group."
    todo("Rename to sequentialGroupOf()")
    shared SequentialGroup createSequentialGroupOf(Component|Group* components) =>
        initializeGroup(createSequentialGroup(), *components);
}
"An extension to [[JComboBox]] to improve it by making the Tab key do what one expects."
shared class ImprovedComboBox<T> extends JComboBox<T> {
    shared new () extends JComboBox<T>() { }
    shared new withModel(ComboBoxModel<T> boxModel) extends JComboBox<T>(boxModel) { }
    editable = true;
    "Handle a key-press. If Tab is pressed when the pop-up list is visible, treat it like
     Enter."
    by("http://stackoverflow.com/a/24336768")
    shared actual void processKeyEvent(KeyEvent event) {
        if (event.id != KeyEvent.keyPressed || event.keyCode != KeyEvent.vkTab) {
            super.processKeyEvent(event);
            return;
        }
        if (popupVisible) {
            assert (is Component source = event.source);
            super.processKeyEvent(KeyEvent(source, event.id, event.when, 0,
                KeyEvent.vkEnter, KeyEvent.charUndefined));
        }
        if (event.modifiers == 0) {
            transferFocus();
        } else if (event.modifiers == InputEvent.shiftMask) {
            transferFocusBackward();
        }
    }
}
"Possible colors for use by text in a [[StreamingLabel]]"
shared class LabelTextColor {
    shared actual String string;
    shared new yellow { string = "yellow"; }
    shared new white { string = "white"; }
    shared new red { string = "red"; }
    shared new green { string = "green"; }
}
"A label that can easily be written (appended) to."
shared class StreamingLabel extends JEditorPane {
    shared new () extends JEditorPane("text/html",
        """<html><body bgcolor="#000000"><p>&nbsp;</p></body></html>""") {}
    editable = false;
    setBackground(Color.black);
    opaque = true;
    StringBuilder buffer = StringBuilder();
    "Add text to the label."
    shared void append(String string) {
        buffer.append(string);
        text = "<html><body bgcolor=\"#000000\">``buffer``</body></html>";
        repaint();
    }
}
"The possible axes that a [[BoxLayout]] can be laid out on."
shared class BoxAxis {
    "The constant to pass to the [[BoxLayout]]."
    shared Integer axis;
    shared new lineAxis { axis = BoxLayout.lineAxis; }
    shared new pageAxis { axis = BoxLayout.pageAxis; }
}
"An interface to provide helper methods for a panel laid out by a [[BoxLayout]]."
shared sealed interface BoxPanel {
    "Which direction the panel is laid out, for use in the helper methods."
    shared formal BoxAxis axis;
    """Add "glue" (elasticity) between components."""
    shared default void addGlue() {
        assert (is Container container = this);
        switch (axis)
        case (BoxAxis.lineAxis) { container.add(Box.createHorizontalGlue()); }
        case (BoxAxis.pageAxis) { container.add(Box.createVerticalGlue()); }
        else { throw IllegalStateException("Impossible axis case"); }
    }
    "Add a rigid (fixed-size) area between components."
    shared default void addRigidArea(Integer dimension) {
        Dimension dimensionObject;
        switch (axis)
        case (BoxAxis.lineAxis) { dimensionObject = Dimension(dimension, 0); }
        case (BoxAxis.pageAxis) { dimensionObject = Dimension(0, dimension); }
        else { throw IllegalStateException("Impossible axis case"); }
        assert (is Container container = this);
        container.add(Box.createRigidArea(dimensionObject));
    }
}
class BoxPanelImpl(BoxAxis layoutAxis) extends JPanel() satisfies BoxPanel {
    shared actual BoxAxis axis = layoutAxis;
}
"Create a panel laid out by a [[BoxLayout]]"
shared JPanel&BoxPanel boxPanel(BoxAxis layoutAxis) => BoxPanelImpl(layoutAxis);
"Create a panel laid out by a [[BoxLayout]] on the line axis, with glue at each end and a
 small rigid area between each component."
shared JPanel&BoxPanel centeredHorizontalBox(Component* items) {
    JPanel&BoxPanel retval = BoxPanelImpl(BoxAxis.lineAxis);
    retval.addGlue();
    if (exists first = items.first) {
        retval.add(first);
    }
    for (component in items) {
        retval.addRigidArea(2);
        retval.add(component);
    }
    retval.addGlue();
    return retval;
}
"A panel laid out by a BorderLayout, with helper methods/attributes to assign components
 to its different sectors."
shared class BorderedPanel extends JPanel {
    variable Component? centerLocal = null;
    shared Component? center => centerLocal;
    assign center {
        if (exists temp = center) {
            add(temp, javaString(BorderLayout.center));
        }
        centerLocal = center;
    }
    variable Component? lineStartLocal = null;
    shared Component? lineStart => lineStartLocal;
    assign lineStart {
        if (exists temp = lineStart) {
            add(temp, javaString(BorderLayout.lineStart));
        }
        lineStartLocal = lineStart;
    }
    variable Component? lineEndLocal = null;
    shared Component? lineEnd => lineEndLocal;
    assign lineEnd {
        if (exists temp = lineEnd) {
            add(temp, javaString(BorderLayout.lineEnd));
        }
        lineEndLocal = lineEnd;
    }
    variable Component? pageStartLocal = null;
    shared Component? pageStart => pageStartLocal;
    assign pageStart {
        if (exists temp = pageStart) {
            add(temp, javaString(BorderLayout.pageStart));
        }
        pageStartLocal = pageStart;
    }
    variable Component? pageEndLocal = null;
    shared Component? pageEnd => pageEndLocal;
    assign pageEnd {
        if (exists temp = pageEnd) {
            add(temp, javaString(BorderLayout.pageEnd));
        }
        pageEndLocal = pageEnd;
    }
    shared new (Component? center = null, Component? pageStart = null,
            Component? pageEnd = null, Component? lineEnd = null,
            Component? lineStart = null) extends JPanel(BorderLayout()) {
        this.center = center;
        this.pageStart = pageStart;
        this.pageEnd = pageEnd;
        this.lineStart = lineStart;
        this.lineEnd = lineEnd;
    }
    shared new verticalPanel(Component? pageStart, Component? center, Component? pageEnd)
        extends BorderedPanel(center, pageStart, pageEnd) { }
    shared new horizontalPanel(Component? lineStart, Component? center,
            Component? lineEnd) extends BorderedPanel(center, null, null, lineEnd,
                lineStart) { }
}
"Versions of [[JSplitPane]] that take the divider location and resize weight, as well as
 other parameters, in the same operation, and don't require the caller to remember
 whether 'true' means a horizontal or vertical split.."
shared JSplitPane verticalSplit(Float dividerLocation, Float resizeWeight, Component top,
        Component bottom) {
    JSplitPane retval = JSplitPane(JSplitPane.verticalSplit, true, top, bottom);
    retval.setDividerLocation(dividerLocation);
    retval.resizeWeight = resizeWeight;
    return retval;
}
see(`function verticalSplit`)
shared JSplitPane horizontalSplit(Float dividerLocation, Float resizeWeight, Component left,
        Component right) {
    JSplitPane retval = JSplitPane(JSplitPane.horizontalSplit, true, left, right);
    retval.setDividerLocation(dividerLocation);
    retval.resizeWeight = resizeWeight;
    return retval;
}
"Set up a hot-key for an action that doesn't call a *menu* item."
shared void createHotKey(
        "The component defining the action's context"
        JComponent component,
        "The String to use to identify the action"
        String action,
        "The listener that should handle the action"
        todo("Now we have union types, take an [[ActionListener]] or equivalent lambda
              as well and in that case wrap it in an [[ActionWrapper]].")
        Action handler,
        "See [[JComponent#getInputMap(Integer)]]."
        Integer condition,
        "The keys to use as hot-keys"
        KeyStroke* keys) {
    InputMap inputMap = component.getInputMap(condition);
    for (key in keys) {
        inputMap.put(key, action);
    }
    component.actionMap.put(action, handler);
}