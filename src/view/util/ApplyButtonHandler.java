package view.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A class to handle Apply and Revert button presses.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2010-2014 Jonathan Lovelace
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
public final class ApplyButtonHandler implements ActionListener {
	/**
	 * The Applyable that does the heavy lifting.
	 */
	private final Applyable obj;

	/**
	 * Constructor.
	 *
	 * @param app the Applyable that does the heavy lifting.
	 */
	public ApplyButtonHandler(final Applyable app) {
		obj = app;
	}

	/**
	 * Handle button presses.
	 *
	 * @param evt the event to handle
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent evt) {
		if (evt != null) {
			if ("Apply".equals(evt.getActionCommand())) {
				obj.apply();
			} else if ("Revert".equals(evt.getActionCommand())) {
				obj.revert();
			}
		}
	}

	/**
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "ApplyButtonHandler";
	}
}
