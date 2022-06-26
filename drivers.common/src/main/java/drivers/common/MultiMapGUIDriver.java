package drivers.common;

/**
 * An interface for graphical apps that (can) operate on both a "main" and at least one "subordinate" map.
 */
public interface MultiMapGUIDriver extends GUIDriver {
	@Override
	IMultiMapModel getModel();
}
