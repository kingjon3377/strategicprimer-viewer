import javax.swing {
    BoxLayout,
    Box,
    JPanel
}

import java.awt {
    Component,
    Container,
    Dimension
}

"The possible axes that a [[BoxLayout]] can be laid out on."
shared class BoxAxis of lineAxis | pageAxis {
    "The constant to pass to the [[BoxLayout]]."
    shared Integer axis;
    shared new lineAxis { axis = BoxLayout.lineAxis; }
    shared new pageAxis { axis = BoxLayout.pageAxis; }
}

"An interface to provide helper methods for a [[panel|JPanel]] laid out by a
 [[BoxLayout]]."
see(`function boxPanel`)
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

"Implementation of [[BoxPanel]]: a [[JPanel]] laid out by a [[BoxLayout]]."
class BoxPanelImpl(BoxAxis layoutAxis) extends JPanel() satisfies BoxPanel {
    shared actual BoxAxis axis = layoutAxis;
}

"Create a panel laid out by a [[BoxLayout]]"
see(`function centeredHorizontalBox`)
shared JPanel&BoxPanel boxPanel(BoxAxis layoutAxis) {
    value retval = BoxPanelImpl(layoutAxis);
    retval.layout = BoxLayout(retval, layoutAxis.axis);
    return retval;
}

"Create a panel laid out by a [[BoxLayout]] on the line axis, with glue at each end and a
 small rigid area between each component."
shared JPanel&BoxPanel centeredHorizontalBox(Component* items) {
    JPanel&BoxPanel retval = boxPanel(BoxAxis.lineAxis);
    retval.addGlue();
    if (exists first = items.first) {
        retval.add(first);
    }
    for (component in items.rest) {
        retval.addRigidArea(2);
        retval.add(component);
    }
    retval.addGlue();
    return retval;
}

"Create a panel laid out by a [[BoxLayout]] in the page axis, with glue at each end and between each component."
shared JPanel&BoxPanel verticalBox(Component* items) {
    JPanel&BoxPanel retval = boxPanel(BoxAxis.pageAxis);
    retval.addGlue();
    for (component in items) {
        retval.add(component);
        retval.addGlue();
    }
    return retval;
}
