package view.util;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * A JFileChooser that takes a FileFilter in its constructor.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2013 Jonathan Lovelace
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class FilteredFileChooser extends JFileChooser {
	/**
	 * Constructor.
	 *
	 * @param current the current directory
	 * @param filter the filter to apply
	 */
	public FilteredFileChooser(final String current, final FileFilter filter) {
		super(current);
		setFileFilter(filter);
	}
}
