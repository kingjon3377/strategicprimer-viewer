package drivers.worker_mgmt;

/**
 * An interface for objects that handle the user's selection of a unit.
 */
public interface UnitSelectionSource {
	/**
	 * Add a listener.
	 */
	void addUnitSelectionListener(UnitSelectionListener listener);

	/**
	 * Remove a listener.
	 */
	void removeUnitSelectionListener(UnitSelectionListener listener);
}
