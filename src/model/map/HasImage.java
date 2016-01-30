package model.map;

/**
 * An interface for model elements that have images that can be used to represent them.
 * This interface should really be in model.viewer, but that would, I think, introduce
 * circular dependencies between packages.
 *
 * TODO: Split mutator into separate interface
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2014 Jonathan Lovelace
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
public interface HasImage {
	/**
	 * FIXME: This is model-view mixing, but should fix code-complexity problems in the
	 * tile-draw helper.
	 *
	 * This is the image to use if the individual fixture doesn't specify a different
	 * image. It should be a "constant function."
	 *
	 * @return the name of an image to represent this kind of fixture.
	 */
	String getDefaultImage();

	/**
	 * @param image the new image for this *individual* fixture. If null or the empty
	 *              string, the default image will be used.
	 */
	void setImage(String image);

	/**
	 * @return the name of an image to represent this individual fixture.
	 */
	String getImage();
}
