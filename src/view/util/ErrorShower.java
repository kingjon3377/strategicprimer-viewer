package view.util;

import java.awt.Component;
import javax.swing.JOptionPane;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A utility class to show error messages.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2013 Jonathan Lovelace
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
@SuppressWarnings("UtilityClassCanBeEnum")
public final class ErrorShower {
	/**
	 * Do not instantiate.
	 */
	private ErrorShower() {
		// Do not use.
	}

	/**
	 * Show an error dialog.
	 *
	 * @param parent  the parent component for the dialog. May be null, since JOptionPane
	 *                doesn't seem to care.
	 * @param message the error message.
	 */
	public static void showErrorDialog(@Nullable final Component parent,
	                                   final String message) {
		JOptionPane.showMessageDialog(parent, message,
				"Strategic Primer Map Viewer error", JOptionPane.ERROR_MESSAGE);
	}
}
