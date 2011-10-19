package view.map.main;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Filter out extraneous files when we're opening a map.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class MapFileFilter extends FileFilter {
	/**
	 * Accept .xml and .map.
	 * 
	 * @param file
	 *            a file to consider
	 * 
	 * @return true if its extension is .xml or .map
	 */
	@Override
	public boolean accept(final File file) {
		if (file.isDirectory()) {
			return true; // NOPMD
		} else {
			final String extension = getExtension(file);
			return "xml".equals(extension) || "map".equals(extension);
		}
	}

	/**
	 * @param file
	 *            A file
	 * 
	 * @return The extension of that file
	 */
	public static String getExtension(final File file) {
		final String name = file.getName();
		final int dotPos = name.lastIndexOf('.');

		return (dotPos > 0 && dotPos < name.length() - 1) ? name.substring(
				dotPos + 1).toLowerCase() : "";
	}

	/**
	 * 
	 * @return A description of the filter.
	 */
	@Override
	public String getDescription() {
		return "Strategic Primer world map files";
	}

	/**
	 * 
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "MapFileFilter";
	}
}
