package model.listeners;

/**
 * An interface for objects that tell listeners when the visible dimensions or
 * the tile size/zoom level changed.
 *
 * @author Jonathan Lovelace
 *
 */
public interface GraphicalParamsSource {
	/**
	 * @param list a listener to add
	 */
	void addGraphicalParamsListener(GraphicalParamsListener list);

	/**
	 * @param list a listener to remove
	 */
	void removeGraphicalParamsListener(GraphicalParamsListener list);
}
