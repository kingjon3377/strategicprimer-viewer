package view.map.main;

import controller.map.misc.IDFactoryFiller;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.*;
import model.listeners.SelectionChangeListener;
import model.listeners.SelectionChangeSource;
import model.listeners.SelectionChangeSupport;
import model.listeners.VersionChangeListener;
import model.map.Point;
import model.map.PointFactory;
import model.map.TileType;
import model.misc.IDriverModel;
import model.viewer.IViewerModel;
import org.eclipse.jdt.annotation.Nullable;
import view.worker.NewUnitDialog;

/**
 * A popup menu to let the user change a tile's terrain type, or add a unit.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class TerrainChangingMenu extends JPopupMenu
		implements VersionChangeListener, SelectionChangeSource,
						   SelectionChangeListener {
	/**
	 * The driver model.
	 */
	private final IDriverModel model;
	/**
	 * The helper to handle selection-change listeners for us.
	 */
	private final SelectionChangeSupport scs = new SelectionChangeSupport();
	/**
	 * The menu item to allow the user to create a new unit.
	 */
	private final JMenuItem newUnitItem = new JMenuItem("Add New Unit");
	/**
	 * The point at which we might change terrain.
	 */
	private Point point = PointFactory.point(-1, -1);

	/**
	 * Constructor.
	 *
	 * @param version     the map version
	 * @param driverModel the driver model
	 */
	public TerrainChangingMenu(final int version, final IViewerModel driverModel) {
		model = driverModel;
		final NewUnitDialog nuDialog =
				new NewUnitDialog(driverModel.getMap().getCurrentPlayer(),
										 IDFactoryFiller
																				   .createFactory(
																						   driverModel
																								   .getMap()));
		nuDialog.addNewUnitListener(unit -> {
			driverModel.getMap().addFixture(point, unit);
			driverModel.setSelection(point);
			scs.fireChanges(null, point);
		});
		newUnitItem.addActionListener(e -> nuDialog.setVisible(true));
		nuDialog.dispose();
		updateForVersion(version);
	}

	/**
	 * Update the menu for a new version.
	 *
	 * @param version the version
	 */
	private void updateForVersion(final int version) {
		removeAll();
		for (final TileType type : TileType.valuesForVersion(version)) {
			//noinspection ObjectAllocationInLoop
			final JMenuItem item = new JMenuItem(type.toString());
			add(item);
			item.addActionListener(evt -> {
				model.getMap().setBaseTerrain(point, type);
				scs.fireChanges(null, point);
			});
		}
		addSeparator();
		add(newUnitItem);
	}

	/**
	 * @param old        the previously selected version
	 * @param newVersion the newly selected version
	 */
	@Override
	public void changeVersion(final int old, final int newVersion) {
		updateForVersion(newVersion);
	}

	/**
	 * @param list a listener to add
	 */
	@Override
	public void addSelectionChangeListener(final SelectionChangeListener list) {
		scs.addSelectionChangeListener(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public void removeSelectionChangeListener(final SelectionChangeListener list) {
		scs.removeSelectionChangeListener(list);
	}

	/**
	 * @param old      ignored
	 * @param newPoint ignored
	 */
	@Override
	public void selectedPointChanged(@Nullable final Point old, final Point newPoint) {
		point = newPoint;
		if (TileType.NotVisible == model.getMap().getBaseTerrain(newPoint)) {
			newUnitItem.setEnabled(false);
		} else {
			newUnitItem.setEnabled(true);
		}
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
		return "TerrainChangingMenu for " + point;
	}
}
