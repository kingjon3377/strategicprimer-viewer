package model.listeners;

/**
 * An interface for objects that want to be notified of when a moving unit
 * incurs movement costs.
 *
 * @author Jonathan Lovelace
 *
 */
public interface MovementCostListener {
	/**
	 * Account for a movement.
	 * @param cost how much it cost
	 */
	void deduct(final int cost);
}
