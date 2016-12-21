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
import util.OnMac;

/**
 * A panel to be the GUI to add items from a list.
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
public final class ItemAdditionPanel extends JPanel implements AddRemoveSource {
	/**
	 * The maximum height of the widget.
	 */
	private static final int MAX_HEIGHT = 50;

	/**
	 * A list of listeners to notify of addition or removal.
	 */
	private final Collection<AddRemoveListener> arListeners = new ArrayList<>();

	/**
	 * The text box.
	 */
	private final JTextField field = new JTextField(10);
	/**
	 * Constructor.
	 *
	 * @param what what we're adding or removing
	 */
	public ItemAdditionPanel(final String what) {
		final CardLayout layout = new CardLayout();
		final String category = what;
		setLayout(layout);
		setPanelSizes(this);
		final JPanel first = new BoxPanel(true);
		first.add(new ListenedButton("+", evt -> {
			layout.next(this);
			field.requestFocusInWindow();
		}));
		setPanelSizes(first);
		add(first);
		final JPanel second = new BoxPanel(false);
		second.add(field);
		final ActionListener okListener = evt -> {
			final String text = field.getText();
			for (final AddRemoveListener listener : arListeners) {
				listener.add(category, text);
			}
			layout.first(this);
			field.setText("");
		};
		field.addActionListener(okListener);
		field.setActionCommand("OK");
		final JPanel okPanel = new BoxPanel(true);
		final ListenedButton okButton = new ListenedButton("OK", okListener);
		okPanel.add(okButton);
		final ListenedButton cancelButton = new ListenedButton("Cancel", evt -> {
			layout.first(this);
			field.setText("");
		});
		OnMac.makeButtonsSegmented(okButton, cancelButton);
		okPanel.add(cancelButton);
		second.add(okPanel);
		setPanelSizes(second);
		add(second);
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
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({"unused", "static-method"})
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
	@SuppressWarnings({"unused", "static-method"})
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * @return a diagnostic String
	 */
	@Override
	public String toString() {
		return "ItemAdditionPanel: Field currently contains " + field.getText();
	}
}
