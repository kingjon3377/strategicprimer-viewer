package model.listeners;

/**
 * An interface for objects that handle movement and can tell listeners how much
 * a move cost.
 *
 * @author Jonathan Lovelace
 *
 */
public interface MovementCostSource {
	/**
	 * Add a listener.
	 *
	 * @param list the listener to add
	 */
	void addMovementCostListener(MovementCostListener list);

	/**
	 * @param list the listener to remove
	 */
	void removeMovementCostListener(MovementCostListener list);
}
