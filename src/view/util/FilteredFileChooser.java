package view.util;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
/**
 * A JFileChooser that takes a FileFilter in its constructor.
 * @author Jonathan Lovelace
 *
 */
public class FilteredFileChooser extends JFileChooser {
	/**
	 * Constructor.
	 * @param current the current directory
	 * @param filter the filter to apply
	 */
	public FilteredFileChooser(final String current, final FileFilter filter) {
		super(current);
		setFileFilter(filter);
	}
}
