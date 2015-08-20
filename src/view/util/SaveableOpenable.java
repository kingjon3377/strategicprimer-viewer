package view.util;

import java.io.IOException;

/**
 * An interface for UIs that can save and open their contents to and from file.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2010-2014 Jonathan Lovelace
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public interface SaveableOpenable {
	/**
	 * Load from file.
	 *
	 * @param file
	 *            the filename to load from
	 *
	 * @throws IOException
	 *             if the file doesn't exist or on other I/O error while loading
	 */
	void open(String file) throws IOException;

	/**
	 * Save to file.
	 *
	 * @param file the filename to save to
	 *
	 * @throws IOException on I/O error while saving
	 */
	void save(String file) throws IOException;
}
