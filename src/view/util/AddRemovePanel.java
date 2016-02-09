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
 * TODO: Rather than having boolean constructor parameter, split into two classes, one
 * allowing removals and the other not.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
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
	 * The layout.
	 */
	private final CardLayout layout;
	/**
	 * The text box.
	 */
	private final JTextField field = new JTextField(10);
	/**
	 * What we're adding or removing.
	 */
	private final String category;

	/**
	 * Constructor.
	 *
	 * @param removalPossible whether we should put in a "remove" button.
	 * @param what            what we're adding or removing
	 */
	public AddRemovePanel(final boolean removalPossible, final String what) {
		layout = new CardLayout();
		category = what;
		setLayout(layout);
		setPanelSizes(this);
		final BoxPanel first = new BoxPanel(true);
		first.add(new ListenedButton("+", evt -> {
			layout.next(this);
			field.requestFocusInWindow();
		}));
		if (removalPossible) {
			first.add(new ListenedButton("-", evt -> {
				for (final AddRemoveListener listener : arListeners) {
					listener.remove(what);
				}
			}));
		}
		setPanelSizes(first);
		add(first);
		final BoxPanel second = new BoxPanel(false);
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
		final BoxPanel okPanel = new BoxPanel(true);
		okPanel.add(new ListenedButton("OK", okListener));
		okPanel.add(new ListenedButton("Cancel", evt -> {
			layout.first(this);
			field.setText("");
		}));
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
	 * @param out ignored
	 * @throws IOException always
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}
	/**
	 * Prevent serialization
	 * @param in ignored
	 * @throws IOException always
	 * @throws ClassNotFoundException never
	 */
	private void readObject(ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}
}
