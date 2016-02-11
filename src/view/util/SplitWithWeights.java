package view.util;

import java.awt.Component;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JSplitPane;

/**
 * A version of JSplitPane that takes the divider location and resize weight, as well as
 * the other parameters, in its constructor.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2013 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class SplitWithWeights extends JSplitPane {
	/**
	 * Factory method.
	 * @param divLoc    the divider location
	 * @param resWeight the resize weight
	 * @param left      the left or top component
	 * @param right     the right or bottom component
	 */
	public static JSplitPane vertical(final double divLoc, final double resWeight,
	                           final Component left, final Component right) {
		return new SplitWithWeights(VERTICAL_SPLIT, divLoc, resWeight, left, right);
	}
	/**
	 * Factory method.
	 * @param divLoc    the divider location
	 * @param resWeight the resize weight
	 * @param left      the left or top component
	 * @param right     the right or bottom component
	 */
	public static JSplitPane horizontal(final double divLoc, final double resWeight,
	                             final Component left, final Component right) {
		return new SplitWithWeights(HORIZONTAL_SPLIT, divLoc, resWeight, left, right);
	}
	/**
	 * Constructor.
	 *
	 * @param orient    the orientation of the panel.
	 * @param divLoc    the divider location
	 * @param resWeight the resize weight
	 * @param left      the left or top component
	 * @param right     the right or bottom component
	 */
	private SplitWithWeights(final int orient, final double divLoc,
	                        final double resWeight, final Component left,
	                        final Component right) {
		super(orient, true, left, right);
		setDividerLocation(divLoc);
		setResizeWeight(resWeight);
	}
	/**
	 * Prevent serialization.
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({ "unused", "static-method" })
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}
	/**
	 * Prevent serialization
	 * @param in ignored
	 * @throws IOException always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings({ "unused", "static-method" })
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}
}
