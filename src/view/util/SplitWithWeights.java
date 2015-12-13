package view.util;

import java.awt.Component;

import javax.swing.JSplitPane;

/**
 * A version of JSplitPane that takes the divider location and resize weight, as
 * well as the other parameters, in its constructor.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2013 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class SplitWithWeights extends JSplitPane {
	/**
	 * Constructor.
	 *
	 * @param orient the orientation of the panel.
	 * @param divLoc the divider location
	 * @param resWeight the resize weight
	 * @param left the left or top component
	 * @param right the right or bottom component
	 */
	public SplitWithWeights(final int orient, final double divLoc,
			final double resWeight, final Component left, final Component right) {
		super(orient, true, left, right);
		setDividerLocation(divLoc);
		setResizeWeight(resWeight);
	}
}
