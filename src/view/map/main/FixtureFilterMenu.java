package view.map.main;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import model.map.TileFixture;
import model.viewer.FixtureMatcher;
import model.viewer.TileTypeFixture;
import model.viewer.ZOrderFilter;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A menu to let the player turn of display of kinds of fixtures.
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
public final class FixtureFilterMenu extends JMenu implements ZOrderFilter {
	/**
	 * Map from fixture classes to menu-items representing them.
	 */
	private final Map<FixtureMatcher, JCheckBoxMenuItem> mapping =
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
		all.addActionListener(evt -> selectAll());
		add(all);
		final JMenuItem none = new JMenuItem("None");
		none.addActionListener(evt -> deselectAll());
		add(none);
		addSeparator();
	}
	/**
	 * @param predicate a predicate to match some tile fixtures
	 * @param desc a description of such fixtures
	 * @return the menu item to control whether matching fixtures are visible
	 */
	private JCheckBoxMenuItem createMenuItem(final Predicate<TileFixture> predicate,
											 final String desc) {
		final FixtureMatcher matcher = new FixtureMatcher(predicate, desc);
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem(desc, true);
		item.getModel().addItemListener(e -> matcher.setDisplayed(item.isSelected()));
		mapping.put(matcher, item);
		return item;
	}
	/**
	 * Add an item to the menu where it belongs in the alphabetical sequence.
	 * @param item the item to add
	 */
	private void addSorted(final JCheckBoxMenuItem item) {
		final String text = item.getText();
		itemNames.add(text);
		Collections.sort(itemNames);
		final int index = itemNames.indexOf(text);
		add(item, index + 3); // "All", "None", and the separator
	}
	/**
	 * @param fix a kind of fixture. We mark it Nullable because nulls got passed in
	 *            anyway.
	 * @return whether the view should display that kind of fixture
	 */
	@Override
	public boolean shouldDisplay(@Nullable final TileFixture fix) {
		if (fix == null) {
			return false;
		}
		if (fix instanceof TileTypeFixture) {
			return false;
		} else {
			for (final FixtureMatcher matcher : mapping.keySet()) {
				if (matcher.matches(fix)) {
					return matcher.isDisplayed();
				}
			}
			final Class<? extends TileFixture> cls = fix.getClass();
			final JCheckBoxMenuItem item = createMenuItem(cls::isInstance, fix.plural());
			if ("null".equals(fix.shortDesc())) {
				item.setSelected(false);
			} else {
				addSorted(item);
			}
			return item.isSelected();
		}
	}
	/**
	 * Select all items.
	 */
	private void selectAll() {
		for (final JCheckBoxMenuItem item : mapping.values()) {
			item.setSelected(true);
		}
	}
	/**
	 * Deselect all items.
	 */
	private void deselectAll() {
		for (final JCheckBoxMenuItem item : mapping.values()) {
			item.setSelected(false);
		}
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
	/**
	 * @return a diagnostic String
	 */
	@Override
	public String toString() {
		return "FixtureFilterMenu containing " + mapping.size() + " items";
	}
}
