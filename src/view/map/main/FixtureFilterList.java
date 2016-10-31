package view.map.main;

import java.awt.Component;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import model.map.TileFixture;
import model.map.fixtures.Ground;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.Meadow;
import model.viewer.FixtureFilterListModel;
import model.viewer.FixtureMatcher;
import model.viewer.ZOrderFilter;
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;

/**
 * A list to let the user select which fixtures ought to be searched.
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
public final class FixtureFilterList extends JList<FixtureMatcher>
		implements ZOrderFilter, ListCellRenderer<FixtureMatcher> {
	/**
	 * The renderer that does most of the work.
	 */
	private final ListCellRenderer<Object> lcr = new DefaultListCellRenderer();

	/**
	 * A mapping from classes of fixtures to their plurals.
	 */
	private final Map<Class<? extends TileFixture>, String> plurals;
	/**
	 * The selection model.
	 */
	private final ListSelectionModel lsm;
	/**
	 * The data model.
	 */
	private final FixtureFilterListModel model;

	/**
	 * Constructor.
	 */
	public FixtureFilterList() {
		plurals = new HashMap<>();
		model = new FixtureFilterListModel();
		setModel(model);
		lsm = NullCleaner.assertNotNull(getSelectionModel());
		lsm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		lsm.addListSelectionListener(e -> {
			for (int i = 0; i < model.getSize(); i++) {
				model.getElementAt(i).setDisplayed(lsm.isSelectedIndex(i));
			}
		});
		setCellRenderer(this);
		model.add(new FixtureMatcher(fix -> fix instanceof Ground &&
													((Ground) fix).isExposed(),
											"Ground (exposed)"));
		model.add(new FixtureMatcher(
				fix -> fix instanceof Ground && !((Ground) fix).isExposed(), "Ground"));
		model.add(new FixtureMatcher(fix -> fix instanceof Grove &&
													((Grove) fix).isOrchard(),
											"Orchards"));
		model.add(new FixtureMatcher(fix -> fix instanceof Grove &&
													!((Grove) fix).isOrchard(),
											"Groves"));
		model.add(new FixtureMatcher(fix -> fix instanceof Meadow &&
													((Meadow) fix).isField(), "Fields"));
		model.add(new FixtureMatcher(fix -> fix instanceof Meadow &&
													!((Meadow) fix).isField(),
											"Meadows"));
	}

	/**
	 * @param fix a fixture
	 * @return whether it should be searched
	 */
	@Override
	public boolean shouldDisplay(final TileFixture fix) {
		for (final FixtureMatcher matcher : model) {
			if (matcher.matches(fix)) {
				return matcher.isDisplayed();
			}
		}
		final Class<? extends TileFixture> cls = fix.getClass();
		if (cls == null) {
			return false;
		} else {
			model.add(new FixtureMatcher(cls::isInstance, fix.plural()));
			final int size = model.getSize();
			lsm.addSelectionInterval(size - 1, size - 1);
			return true;
		}
	}

	/**
	 * @param list         this
	 * @param value        the value being rendered
	 * @param index        its index
	 * @param isSelected   whether or not it's selected
	 * @param cellHasFocus whether or not it has the focus
	 * @return the rendered widget
	 */
	@Override
	public Component getListCellRendererComponent(@Nullable
												final JList<? extends FixtureMatcher> list,
												final FixtureMatcher value,
												final int index,
												final boolean isSelected,
												final boolean cellHasFocus) {
		if (list == null) {
			throw new IllegalArgumentException("Asked to render null list");
		}
		final Component retval = lcr.getListCellRendererComponent(list, value,
				index, isSelected, cellHasFocus);
		if (retval instanceof JLabel) {
			((JLabel) retval).setText(value.getDescription());
		} else if (retval == null) {
			throw new IllegalStateException("Default produced null");
		}
		return retval;
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
	 * @return a quasi-diagnostic String
	 */
	@Override
	public String toString() {
		return "FixtureFilterList containing " + getComponentCount() + " items";
	}
}
