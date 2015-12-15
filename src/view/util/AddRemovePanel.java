package view.util;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.eclipse.jdt.annotation.Nullable;

import model.listeners.AddRemoveListener;
import util.NullCleaner;

/**
 * A panel to be the GUI to add or remove items from a list.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
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
public final class AddRemovePanel extends JPanel implements ActionListener {
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
	 * @param what what we're adding or removing
	 */
	public AddRemovePanel(final boolean removalPossible, final String what) {
		layout = new CardLayout();
		category = what;
		setLayout(layout);
		setPanelSizes(this);
		final BoxPanel first = new BoxPanel(true);
		first.add(new ListenedButton("+", this));
		if (removalPossible) {
			first.add(new ListenedButton("-", this));
		}
		setPanelSizes(first);
		add(first);
		final BoxPanel second = new BoxPanel(false);
		second.add(field);
		field.addActionListener(this);
		field.setActionCommand("OK");
		final BoxPanel okPanel = new BoxPanel(true);
		okPanel.add(new ListenedButton("OK", this));
		okPanel.add(new ListenedButton("Cancel", this));
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
	 * @param evt the event to handle
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent evt) {
		if (evt == null) {
			return;
		} else if ("+".equals(evt.getActionCommand())) {
			layout.next(this);
			field.requestFocusInWindow();
		} else if ("-".equals(evt.getActionCommand())) {
			for (final AddRemoveListener list : arListeners) {
				list.remove(category);
			}
		} else if ("OK".equals(evt.getActionCommand())) {
			final String text = field.getText();
			for (final AddRemoveListener list : arListeners) {
				list.add(category, NullCleaner.valueOrDefault(text, "null"));
			}
			layout.first(this);
			field.setText("");
		} else if ("Cancel".equals(evt.getActionCommand())) {
			layout.first(this);
			field.setText("");
		}
	}

	/**
	 * @param list a listener to add
	 */
	public void addAddRemoveListener(final AddRemoveListener list) {
		arListeners.add(list);
	}

	/**
	 * @param list a list to remove
	 */
	public void removeAddRemoveListener(final AddRemoveListener list) {
		arListeners.remove(list);
	}
}
