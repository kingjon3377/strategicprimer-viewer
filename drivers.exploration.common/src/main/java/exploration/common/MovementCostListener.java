package exploration.common;

/**
 * An interface for objects that want to be notified of when a moving unit
 * incurs movement costs.
 */
public interface MovementCostListener {
	/**
	 * Account for a movement.
	 * 
	 * @param cost How many movement points the movement or action cost
	 */
	void deduct(int cost);
}
