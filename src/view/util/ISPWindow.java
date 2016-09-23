package view.util;

/**
 * An interface for top-level windows in assistive programs.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@FunctionalInterface
public interface ISPWindow {
	/**
	 * This method should *not* return a string including the loaded file, since it is
	 * used only in the About dialog to "personalize" it for the particular app.
	 *
	 * @return The name of this window.
	 */
	String getWindowName();
}
