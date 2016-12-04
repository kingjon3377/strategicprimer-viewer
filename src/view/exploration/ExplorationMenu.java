package view.exploration;

import com.bric.window.WindowMenu;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.*;
import model.misc.IDriverModel;
import view.util.SPMenu;

/**
 * Menus for the exploration GUI.
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
public final class ExplorationMenu extends SPMenu {
	/**
	 * Constructor.
	 *
	 * @param ioh    the I/O handler to handle I/O related items
	 * @param model  the exploration model
	 * @param parent the window this is to be attached to, which should close on "Close".
	 */
	public ExplorationMenu(final ActionListener ioh, final IDriverModel model,
						   final JFrame parent) {
		add(createFileMenu(ioh, model));
		addDisabled(createMapMenu(ioh, model));
		add(createViewMenu(ioh, model));
		add(new WindowMenu(parent));
	}

	/**
	 * Prevent serialization.
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({"unused", "static-method"})
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings({"unused", "static-method"})
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}
}
