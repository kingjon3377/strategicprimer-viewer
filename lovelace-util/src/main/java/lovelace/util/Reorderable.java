package lovelace.util;

/**
 * An interface for list-like things that can be reordered.
 */
public interface Reorderable {
	/**
	 * Move a row of a list or table from one position to another.
	 *
	 * @param fromIndex the index to remove from
	 * @param toIndex   the index (<em>before</em> removing the item!) to move to
	 */
	void reorder(int fromIndex, int toIndex);
}
