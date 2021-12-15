package drivers.common;

/**
 * An interface for objects that can indicate the selected location and tile changed.
 */
public interface SelectionChangeSource {
	/**
	 * Notify the given listener of future selection changes.
	 */
	void addSelectionChangeListener(SelectionChangeListener listener);

	/**
	 * Stop notifying the given listener of selection changes.
	 */
	void removeSelectionChangeListener(SelectionChangeListener listener);
}
