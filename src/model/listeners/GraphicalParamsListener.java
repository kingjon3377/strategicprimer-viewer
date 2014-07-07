package model.listeners;

import java.util.EventListener;

import model.viewer.VisibleDimensions;

/**
 * An interface for objects that want to keep abreast of visible dimensions and
 * zoom level.
 *
 * @author Jonathan Lovelace
 *
 */
public interface GraphicalParamsListener extends EventListener {
	/**
	 * @param oldDim the previous dimensions
	 * @param newDim the new dimensions
	 */
	void dimensionsChanged(VisibleDimensions oldDim,
			VisibleDimensions newDim);

	/**
	 * @param oldSize the previous tsize/zoom level
	 * @param newSize the new tsize/zoom level
	 */
	void tsizeChanged(int oldSize, int newSize);
}
