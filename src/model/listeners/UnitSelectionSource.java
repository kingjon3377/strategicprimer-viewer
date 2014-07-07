package model.listeners;
/**
 * An interface for objects that handle the user's selection of a unit.
 * @author Jonathan Lovelace
 *
 */
public interface UnitSelectionSource {
	/**
	 * @param list a listener to add
	 */
	void addUnitSelectionListener(UnitSelectionListener list);
	/**
	 * @param list a listener to remove
	 */
	void removeUnitSelectionListener(UnitSelectionListener list);
}
