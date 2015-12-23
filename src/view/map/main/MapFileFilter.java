package view.map.main;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import org.eclipse.jdt.annotation.Nullable;

import static util.NullCleaner.assertNotNull;

/**
 * Filter out extraneous files when we're opening a map.
 *
 * FIXME: This should be singleton
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
public final class MapFileFilter extends FileFilter {
	/**
	 * Accept .xml and .map.
	 *
	 * @param file a file to consider
	 * @return true if its extension is .xml or .map
	 */
	@Override
	public boolean accept(@Nullable final File file) {
		if (file == null) {
			throw new IllegalArgumentException("null filename");
		} else if (file.isDirectory()) {
			return true; // NOPMD
		} else {
			final String extension = getExtension(file);
			return "xml".equals(extension) || "map".equals(extension);
		}
	}

	/**
	 * @param file A file
	 * @return The extension of that file
	 */
	public static String getExtension(final File file) {
		final String name = file.getName();
		final int dotPos = name.lastIndexOf('.');

		if ((dotPos > 0) && (dotPos < (name.length() - 1))) {
			return assertNotNull(name.substring(dotPos + 1).toLowerCase()); // NOPMD
		} else {
			return "";
		}
	}

	/**
	 * @return A description of the filter.
	 */
	@Override
	public String getDescription() {
		return "Strategic Primer world map files";
	}

	/**
	 * @return a String representation of the object.
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "MapFileFilter";
	}
}
