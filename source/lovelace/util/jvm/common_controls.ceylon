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
    InputMap,
    JMenuItem,
    JLabel,
    AbstractAction,
    DefaultListModel
}
import java.awt {
    Component,
    Container,
    Color,
    Dimension,
    BorderLayout,
    Toolkit
}
import lovelace.util.common {
    todo,
    Reorderable
}
import java.lang {
    JDouble=Double,
    JInteger=Integer,
    JString=String,
    Types
}
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
        String message) => JOptionPane.showMessageDialog(parent, message, title, JOptionPane.errorMessage);

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
    shared ParallelGroup parallelGroupOf(Component|Group* components) =>
        initializeGroup(createParallelGroup(), *components);
    "Factory for a sequential group."
    shared SequentialGroup sequentialGroupOf(Component|Group* components) =>
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
shared class BoxAxis of lineAxis | pageAxis {
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
    }
    "Add a rigid (fixed-size) area between components."
    shared default void addRigidArea(Integer dimension) {
        Dimension dimensionObject;
        switch (axis)
        case (BoxAxis.lineAxis) { dimensionObject = Dimension(dimension, 0); }
        case (BoxAxis.pageAxis) { dimensionObject = Dimension(0, dimension); }
        assert (is Container container = this);
        container.add(Box.createRigidArea(dimensionObject));
    }
}
class BoxPanelImpl(BoxAxis layoutAxis) extends JPanel() satisfies BoxPanel {
    shared actual BoxAxis axis = layoutAxis;
}
"Create a panel laid out by a [[BoxLayout]]"
shared JPanel&BoxPanel boxPanel(BoxAxis layoutAxis) {
	value retval = BoxPanelImpl(layoutAxis);
	retval.layout = BoxLayout(retval, layoutAxis.axis);
	return retval;
}
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
            add(temp, Types.nativeString(BorderLayout.center));
        }
        centerLocal = center;
    }
    variable Component? lineStartLocal = null;
    shared Component? lineStart => lineStartLocal;
    assign lineStart {
        if (exists temp = lineStart) {
            add(temp, Types.nativeString(BorderLayout.lineStart));
        }
        lineStartLocal = lineStart;
    }
    variable Component? lineEndLocal = null;
    shared Component? lineEnd => lineEndLocal;
    assign lineEnd {
        if (exists temp = lineEnd) {
            add(temp, Types.nativeString(BorderLayout.lineEnd));
        }
        lineEndLocal = lineEnd;
    }
    variable Component? pageStartLocal = null;
    shared Component? pageStart => pageStartLocal;
    assign pageStart {
        if (exists temp = pageStart) {
            add(temp, Types.nativeString(BorderLayout.pageStart));
        }
        pageStartLocal = pageStart;
    }
    variable Component? pageEndLocal = null;
    shared Component? pageEnd => pageEndLocal;
    assign pageEnd {
        if (exists temp = pageEnd) {
            add(temp, Types.nativeString(BorderLayout.pageEnd));
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
shared JSplitPane horizontalSplit(Float dividerLocation, Float resizeWeight,
        Component left, Component right) {
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
        Action|ActionListener|Anything(ActionEvent) handler,
        "See [[JComponent#getInputMap(Integer)]]."
        Integer condition,
        "The keys to use as hot-keys"
        KeyStroke* keys) {
    InputMap inputMap = component.getInputMap(condition);
    for (key in keys) {
        inputMap.put(key, action);
    }
    Action temp;
    if (is Action handler) {
        temp = handler;
    } else {
        temp = ActionWrapper(handler);
    }
    component.actionMap.put(action, temp);
}
"An enumeration of possible modifiers to hot-keys."
shared class HotKeyModifier {
    "The mask to OR with the default mask when creating the hot-key."
    shared Integer mask;
    shared new shift { mask = InputEvent.shiftDownMask; }
    shared new ctrl { mask = InputEvent.ctrlDownMask; }
    shared new meta { mask = InputEvent.metaDownMask; }
}
"Create a key-stroke representing a hot-key accelerator."
shared KeyStroke createAccelerator(Integer key, HotKeyModifier* modifiers) {
    variable Integer mask = Toolkit.defaultToolkit.menuShortcutKeyMask;
    for (modifier in modifiers) {
        mask = mask.or(modifier.mask);
    }
    return KeyStroke.getKeyStroke(key, mask);
}
"Create a menu item."
shared JMenuItem createMenuItem(
        "The text of the item"
        String item,
        "The mnemonic key"
        Integer mnemonic,
        "The description to show to accessibility software."
        String description,
        "The listener to handle when the item is selected."
        Anything(ActionEvent)|Anything() listener,
        "The keyboard accelerators (hot-keys). The first one is shown in the menu, but all
         are listened for."
        KeyStroke* accelerators) {
    JMenuItem menuItem = JMenuItem(item, mnemonic);
    if (exists accelerator = accelerators.first) {
        menuItem.accelerator = accelerator;
    }
    menuItem.accessibleContext.accessibleDescription = description;
    if (is Anything(ActionEvent) listener) {
	    menuItem.addActionListener(listener);
	} else {
		menuItem.addActionListener((evt) => listener());
	}
    InputMap inputMap = menuItem.getInputMap(JComponent.whenInFocusedWindow);
    for (accelerator in accelerators) {
        inputMap.put(accelerator, menuItem.action);
    }
    return menuItem;
}
"Combines JLabel with [[JString.format()]]"
todo("Find an equivalent text-formatting API in the Ceylon SDK, if there is one.")
shared class FormattedLabel extends JLabel {
	static Object mapper(Object arg) {
		switch (arg)
		case (is Integer) { return JInteger(arg); }
		case (is String) { return Types.nativeString(arg); }
		case (is Float) { return JDouble(arg); }
		else { return arg; }
	}
	static String formatter(String format, Object* args) {
		return JString.format(format, *args.map(mapper));
	}
	String format;
	shared new (String format, Object* args)
			extends JLabel(formatter(format, *args)) {
		this.format = format;
	}

    shared void setArgs(Object* newArgs) {
        text = formatter(format, *newArgs);
    }
}
"A wrapper around an ActionListener (or equivalent lambda) that extends AbstractAction,
 for the exceedingly common case of a JDK method requiring an Action when we don't need
 more functionality than a single method accepting an ActionEvent."
shared class ActionWrapper(Anything(ActionEvent)|ActionListener wrappedListener)
        extends AbstractAction() {
    Anything(ActionEvent) wrapped;
    if (is ActionListener wrappedListener) {
        wrapped = wrappedListener.actionPerformed;
    } else {
        wrapped = wrappedListener;
    }
    shared actual void actionPerformed(ActionEvent event) => wrapped(event);
}
"Adds an implementation of the [[Reorderable]] interface to the [[DefaultListModel]]
 class."
shared class ReorderableListModel<T>() extends DefaultListModel<T>()
        satisfies Reorderable {
    shared actual void reorder(Integer fromIndex, Integer toIndex) {
        if (fromIndex != toIndex) {
            if (fromIndex > toIndex) {
                add(toIndex, remove(fromIndex));
            } else {
                add(toIndex - 1, remove(fromIndex));
            }
        }
    }
}
