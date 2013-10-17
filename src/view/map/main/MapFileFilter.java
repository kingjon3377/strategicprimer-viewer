package view.map.main;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.eclipse.jdt.annotation.Nullable;

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
	 * @param file a file to consider
	 *
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
	 *
	 * @return The extension of that file
	 */
	public static String getExtension(final File file) {
		final String name = file.getName();
		if (name == null) {
			throw new IllegalArgumentException("File with null name");
		}
		final int dotPos = name.lastIndexOf('.');

		final String retval = name.substring(dotPos + 1).toLowerCase();
		assert retval != null;
		return (dotPos > 0 && dotPos < name.length() - 1) ? retval : "";
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
