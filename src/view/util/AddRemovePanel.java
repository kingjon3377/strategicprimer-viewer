package view.util;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JPanel;
import javax.swing.JTextField;
import model.listeners.AddRemoveListener;

/**
 * A panel to be the GUI to add or remove items from a list.
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
public final class AddRemovePanel extends JPanel implements AddRemoveSource {
	/**
	 * The maximum height of the widget.
	 */
	private static final int MAX_HEIGHT = 50;

	/**
	 * A list of listeners to notify of addition or removal.
	 */
	private final Collection<AddRemoveListener> arListeners = new ArrayList<>();

	/**
	 * Factory method.
	 * @param what what the panel allows the user to add or remove
	 * @return the constructed panel.
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	public static AddRemovePanel addRemovePanel(final String what) {
		final AddRemovePanel retval = new AddRemovePanel();
		final CardLayout layout = new CardLayout();
		final String category = what;
		retval.setLayout(layout);
		setPanelSizes(retval);
		final JPanel first = new BoxPanel(true);
		final JTextField field = new JTextField(10);
		first.add(new ListenedButton("+", evt -> {
			layout.next(retval);
			field.requestFocusInWindow();
		}));
		first.add(new ListenedButton("-", evt -> {
			for (final AddRemoveListener listener : retval.arListeners) {
				listener.remove(what);
			}
		}));
		setPanelSizes(first);
		retval.add(first);
		final JPanel second = new BoxPanel(false);
		second.add(field);
		final ActionListener okListener = evt -> {
			final String text = field.getText();
			for (final AddRemoveListener listener : retval.arListeners) {
				listener.add(category, text);
			}
			layout.first(retval);
			field.setText("");
		};
		field.addActionListener(okListener);
		field.setActionCommand("OK");
		final JPanel okPanel = new BoxPanel(true);
		okPanel.add(new ListenedButton("OK", okListener));
		okPanel.add(new ListenedButton("Cancel", evt -> {
			layout.first(retval);
			field.setText("");
		}));
		second.add(okPanel);
		setPanelSizes(second);
		retval.add(second);
		return retval;
	}
	/**
	 * Constructor.
	 */
	private AddRemovePanel() {
		// Use factory method.
	}

	/**
	 * Set the sizes we want on a panel.
	 *
	 * @param panel the panel in question
	 */
	private static void setPanelSizes(final JPanel panel) {
		panel.setMinimumSize(new Dimension(60, 40));
		panel.setPreferredSize(new Dimension(80, MAX_HEIGHT));
		panel.setMaximumSize(new Dimension(90, MAX_HEIGHT));
	}

	/**
	 * @param list a listener to add
	 */
	@Override
	public void addAddRemoveListener(final AddRemoveListener list) {
		arListeners.add(list);
	}

	/**
	 * @param list a list to remove
	 */
	@Override
	public void removeAddRemoveListener(final AddRemoveListener list) {
		arListeners.remove(list);
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
