package view.map.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import model.map.TileFixture;
import model.viewer.TileTypeFixture;
import model.viewer.ZOrderFilter;
import util.NullCleaner;

/**
 * A menu to let the player turn of display of kinds of fixtures.
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
public final class FixtureFilterMenu extends JMenu implements ZOrderFilter,
		ActionListener {
	/**
	 * Map from fixture classes to menu-items representing them.
	 */
	private final Map<Class<? extends TileFixture>, JCheckBoxMenuItem> mapping =
			new HashMap<>();
	/**
	 * The list of menu item names.
	 */
	private final List<String> itemNames = new ArrayList<>();
	/**
	 * Constructor.
	 */
	public FixtureFilterMenu() {
		super("Display ...");
		setMnemonic(KeyEvent.VK_D);
		final JMenuItem all = new JMenuItem("All");
		all.addActionListener(this);
		add(all);
		final JMenuItem none = new JMenuItem("None");
		none.addActionListener(this);
		add(none);
		addSeparator();
	}

	/**
	 * @param fix a kind of fixture. We mark it Nullable because nulls got passed
	 *        in anyway.
	 * @return whether the view should display that kind of fixture
	 */
	@Override
	public boolean shouldDisplay(@Nullable final TileFixture fix) {
		if (fix == null) {
			return false; // NOPMD
		}
		@NonNull
		TileFixture localFix = fix;
		final JCheckBoxMenuItem item; // NOPMD
		if (fix instanceof TileTypeFixture) {
			return false;
		} else if (mapping.containsKey(fix.getClass())) {
			item = mapping.get(fix.getClass());
		} else if ("null".equals(fix.shortDesc())) {
			item = new JCheckBoxMenuItem(fix.plural(), false);
			mapping.put(localFix.getClass(), item);
		} else {
			item = new JCheckBoxMenuItem(fix.plural(), true);
			mapping.put(NullCleaner.assertNotNull(fix).getClass(), item);
			final String text = fix.plural();
			itemNames.add(text);
			Collections.sort(itemNames);
			final int index = itemNames.indexOf(text);
			add(item, index + 3); // "All", "None", and the separator
		}
		return item.isSelected();
	}
	/**
	 * @param evt the event to handle
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent evt) {
		if (evt == null) {
			return; // NOPMD
		} else if ("All".equals(evt.getActionCommand())) {
			for (final JCheckBoxMenuItem item : mapping.values()) {
				item.setSelected(true);
			}
		} else if ("None".equals(evt.getActionCommand())) {
			for (final JCheckBoxMenuItem item : mapping.values()) {
				item.setSelected(false);
			}
		}
	}
}
