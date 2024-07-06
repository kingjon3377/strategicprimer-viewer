package drivers.map_viewer;

import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;

/**
 * An interface for the method to get the tool-tip message for the location the mouse cursor is over.
 */
/* package */ interface ToolTipSource {
	@Nullable
	String getToolTipText(MouseEvent event);
}

