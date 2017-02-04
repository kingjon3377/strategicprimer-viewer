package view.util;

import java.awt.Dimension;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.Optional;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * An intermediate subclass of JFrame to take care of some common setup things that
 * can't be done in an interface.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public abstract class SPFrame extends JFrame implements ISPWindow {
	/**
	 * Don't instantiate directly.
	 * @param title the window title
	 * @param file the map file, if any, being shown or edited
	 */
	protected SPFrame(final String title, final Optional<Path> file) {
		super(title);
		if (file.isPresent()) {
			setTitle(file.get() + "| " + title);
			getRootPane().putClientProperty("Window.documentFile", file.get().toFile());
		}
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	/**
	 * Constructor.
	 * @param title the window title
	 * @param file the map file, if any, being shown or edited
	 * @param minSize the minimum size of the window
	 */
	protected SPFrame(final String title, final Optional<Path> file,
					  final Dimension minSize) {
		this(title, file);
		setMinimumSize(minSize);
	}
	/**
	 * Prevent serialization.
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings("unused")
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization.
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings("unused")
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}
}
