package view.map.main;

import java.awt.Component;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import model.map.TileFixture;
import model.map.fixtures.Ground;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.Meadow;
import model.viewer.FixtureMatcher;
import model.viewer.ZOrderFilter;
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;
import util.ReorderableListModel;

import static model.viewer.FixtureMatcher.simpleMatcher;

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
	 * The selection model.
	 */
	private final ListSelectionModel lsm;
	/**
	 * The data model.
	 */
	private final DefaultListModel<FixtureMatcher> model;

	/**
	 * Constructor.
	 */
	public FixtureFilterList() {
		model = new ReorderableListModel<>();
		setModel(model);
		lsm = NullCleaner.assertNotNull(getSelectionModel());
		lsm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		lsm.addListSelectionListener(e -> {
			for (int i = 0; i < model.getSize(); i++) {
				model.getElementAt(i).setDisplayed(lsm.isSelectedIndex(i));
			}
		});
		setCellRenderer(this);
		model.addElement(
				simpleMatcher(Ground.class, Ground::isExposed, "Ground (exposed)"));
		model.addElement(simpleMatcher(Ground.class, fix -> !fix.isExposed(), "Ground"));
		model.addElement(simpleMatcher(Grove.class, Grove::isOrchard, "Orchards"));
		model.addElement(simpleMatcher(Grove.class, fix -> !fix.isOrchard(), "Groves"));
		model.addElement(simpleMatcher(Meadow.class, Meadow::isField, "Fields"));
		model.addElement(simpleMatcher(Meadow.class, fix -> !fix.isField(), "Meadows"));
		setTransferHandler(new FixtureFilterTransferHandler());
		setDropMode(DropMode.INSERT);
		setDragEnabled(true);
	}

	/**
	 * @param fix a fixture
	 * @return whether it should be searched
	 */
	@Override
	public boolean shouldDisplay(final TileFixture fix) {
		for (int i = 0; i < model.getSize(); i++) {
			final FixtureMatcher matcher = model.getElementAt(i);
			if (matcher.matches(fix)) {
				return matcher.isDisplayed();
			}
		}
		final Class<? extends TileFixture> cls = fix.getClass();
		if (cls == null) {
			return false;
		} else {
			model.addElement(new FixtureMatcher(cls::isInstance, fix.plural()));
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
												  final JList<? extends FixtureMatcher>
															  list,
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

	/**
	 * @return a quasi-diagnostic String
	 */
	@Override
	public String toString() {
		return "FixtureFilterList containing " + getComponentCount() + " items";
	}
}
