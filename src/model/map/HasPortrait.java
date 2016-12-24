package model.map;

/**
 * An interface for model objects that may have a "portrait" to display in the "fixture
 * details" panel.
 *
 * TODO: Split mutator into separate interface
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface HasPortrait {
	/**
	 * The portrait image filename.
	 * @return the name of the image file containing the portrait.
	 */
	String getPortrait();

	/**
	 * Set a portrait image filename.
	 * @param portrait the name of the image file containing the portrait
	 */
	void setPortrait(String portrait);
}
