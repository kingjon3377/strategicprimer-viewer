import javax.swing {
    JSplitPane
}

import java.awt {
    Component
}

"A version of [[JSplitPane]] that take the divider location and resize weight, as
 well as other parameters, in the same operation, and doesn't require the caller
 to remember whether 'true' means a horizontal or vertical split."
see(`function horizontalSplit`)
shared JSplitPane verticalSplit(Component top, Component bottom,
        Float dividerLocation = 0.5, Float resizeWeight = dividerLocation) {
    JSplitPane retval = JSplitPane(JSplitPane.verticalSplit, true, top, bottom);
    retval.setDividerLocation(dividerLocation);
    retval.resizeWeight = resizeWeight;
    return retval;
}
