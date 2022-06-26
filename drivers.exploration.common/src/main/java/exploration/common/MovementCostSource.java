package exploration.common;

/**
 * An interface for objects that handle movement and can tell listeners how
 * much a move cost.
 */
public interface MovementCostSource {
	/**
	 * Notify the given listener of any future movement costs.
	 */
	void addMovementCostListener(MovementCostListener listener);

	/**
	 * Stop notifying the given listener of movement costs.
	 */
	void removeMovementCostListener(MovementCostListener listener);
}
