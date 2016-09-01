package model.listeners;

import java.util.EventListener;
import model.viewer.VisibleDimensions;

/**
 * An interface for objects that want to keep abreast of visible dimensions and zoom
 * level.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface GraphicalParamsListener extends EventListener {
	/**
	 * @param oldDim the previous dimensions
	 * @param newDim the new dimensions
	 */
	@SuppressWarnings("UnusedParameters")
	void dimensionsChanged(VisibleDimensions oldDim, VisibleDimensions newDim);

	/**
	 * @param oldSize the previous tile-size/zoom level
	 * @param newSize the new tile-size/zoom level
	 */
	@SuppressWarnings("UnusedParameters")
	void tileSizeChanged(int oldSize, int newSize);
}
