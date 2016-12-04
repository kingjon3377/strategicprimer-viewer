package model.map;

/**
 * An interface for model elements that have images that can be used to represent them
 * and that can be changed.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface HasMutableImage extends HasImage {
	/**
	 * @param img the new image for this *individual* fixture. If null or the empty
	 *            string, the default image will be used.
	 */
	void setImage(String img);
}
