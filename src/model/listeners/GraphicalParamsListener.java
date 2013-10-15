package model.listeners;

import model.viewer.VisibleDimensions;

/**
 * An interface for objects that want to keep abreast of visible dimensions and zoom level.
 * @author Jonathan Lovelace
 *
 */
public interface GraphicalParamsListener {
	/**
	 * @param oldDim the previous dimensions
	 * @param newDim the new dimensions
	 */
	void dimensionsChanged(final VisibleDimensions oldDim, final VisibleDimensions newDim);
	/**
	 * @param oldSize the previous tsize/zoom level
	 * @param newSize the new tsize/zoom level
	 */
	void tsizeChanged(final int oldSize, final int newSize);
}
