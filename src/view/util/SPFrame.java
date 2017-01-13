package view.util;

import java.awt.Dimension;
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
}
