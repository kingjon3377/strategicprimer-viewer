package view.util;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * A JFileChooser that takes a FileFilter in its constructor.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class FilteredFileChooser extends JFileChooser {
	/**
	 * A file filter for maps.
	 */
	public static final FileFilter MAP_EXTS =
			new FileNameExtensionFilter("Strategic Primer world map files", "map", "xml");
	/**
	 * Constructor.
	 *
	 * @param current the current directory
	 * @param filter  the filter to apply
	 */
	public FilteredFileChooser(final String current, final FileFilter filter) {
		super(current);
		setFileFilter(filter);
	}
	/**
	 * Constructor, setting the default location to the current directory.
	 */
	public FilteredFileChooser() {
		this(".", MAP_EXTS);
	}
	/**
	 * Prevent serialization.
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({ "unused", "static-method" })
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}
	/**
	 * Prevent serialization
	 * @param in ignored
	 * @throws IOException always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings({ "unused", "static-method" })
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}
}
