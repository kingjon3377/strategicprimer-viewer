package view.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.JPanel;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A panel laid out by a BorderLayout, with helper methods to assign components to its
 * different sectors in a more functional style.
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
@SuppressWarnings("ReturnOfThis")
public class BorderedPanel extends JPanel {
	/**
	 * Constructor.
	 */
	public BorderedPanel() {
		super(new BorderLayout());
	}

	/**
	 * Constructor.
	 *
	 * @param pageStart the "page start" (north in U.S.) component. Ignored if null.
	 * @param pageEnd   the "page end" (south in U.S.) component. Ignored if null.
	 * @param lineEnd   the "line end" (east in U.S.) component. Ignored if null.
	 * @param lineStart the "line start" (west in U.S.) component. Ignored if null.
	 * @param center    the central component. Ignored if null.
	 */
	public BorderedPanel(@Nullable final Component center,
	                     @Nullable final Component pageStart,
	                     @Nullable final Component pageEnd,
	                     @Nullable final Component lineEnd,
	                     @Nullable final Component lineStart) {
		this();
		if (center != null) {
			setCenter(center);
		}
		if (pageStart != null) {
			setPageStart(pageStart);
		}
		if (pageEnd != null) {
			setPageEnd(pageEnd);
		}
		if (lineEnd != null) {
			setLineEnd(lineEnd);
		}
		if (lineStart != null) {
			setLineStart(lineStart);
		}
	}

	/**
	 * @param component a component to place to the north
	 * @return this
	 */
	public final BorderedPanel setPageStart(final Component component) {
		add(component, BorderLayout.PAGE_START);
		return this;
	}

	/**
	 * @param component a component to place to the south
	 * @return this
	 */
	public final BorderedPanel setPageEnd(final Component component) {
		add(component, BorderLayout.PAGE_END);
		return this;
	}

	/**
	 * @param component a component to place in the center
	 * @return this
	 */
	public final BorderedPanel setCenter(final Component component) {
		add(component, BorderLayout.CENTER);
		return this;
	}

	/**
	 * @param component a component to place at line-start.
	 * @return this
	 */
	public final BorderedPanel setLineStart(final Component component) {
		add(component, BorderLayout.LINE_START);
		return this;
	}

	/**
	 * @param component a component to place at line-end.
	 * @return this
	 */
	public final BorderedPanel setLineEnd(final Component component) {
		add(component, BorderLayout.LINE_END);
		return this;
	}
	/**
	 * Prevent serialization.
	 * @param out ignored
	 * @throws IOException always
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}
	/**
	 * Prevent serialization
	 * @param in ignored
	 * @throws IOException always
	 * @throws ClassNotFoundException never
	 */
	private void readObject(ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}
}
