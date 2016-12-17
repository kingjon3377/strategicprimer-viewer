package view.util;

import com.apple.eawt.Application;
import com.bric.window.WindowList;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.InputMap;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import model.viewer.IViewerModel;
import model.workermgmt.IWorkerModel;
import util.OnMac;

/**
 * A common superclass for application-specific menu bars.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class SPMenu extends JMenuBar implements MenuItemCreator {
	/**
	 * Create the file menu.
	 *
	 * @param handler the class to handle I/O related menu items
	 * @param model   the current driver model; only its type is used, to determine which
	 *                menu items to disable.
	 * @return the file menu
	 */
	protected JMenu createFileMenu(final ActionListener handler,
								   final IDriverModel model) {
		final JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		final JMenuItem newItem = createMenuItem("New", KeyEvent.VK_N,
				createHotKey(KeyEvent.VK_N),
				"Create a new, empty map the same size as the current one",
				handler);
		fileMenu.add(newItem);
		if (!(model instanceof IViewerModel)) {
			newItem.setEnabled(false);
		}
		final String loadCaption;
		final String saveCaption;
		final String saveAsCaption;
		if (model instanceof IMultiMapModel) {
			loadCaption = "Load the main map from file";
			saveCaption = "Save the main map to the file it was loaded from";
			saveAsCaption = "Save the main map to file";
		} else {
			loadCaption = "Load a map from file";
			saveCaption = "Save the map to the file it was loaded from";
			saveAsCaption = "Save the map to file";
		}
		fileMenu.add(createMenuItem("Load", KeyEvent.VK_L,
				createHotKey(KeyEvent.VK_O), loadCaption, handler));
		final JMenuItem loadSecondaryItem = createMenuItem("Load secondary",
				KeyEvent.VK_E, createShiftHotKey(KeyEvent.VK_O),
				"Load an additional secondary map from file", handler);
		fileMenu.add(loadSecondaryItem);
		fileMenu.add(createMenuItem("Save", KeyEvent.VK_S,
				createHotKey(KeyEvent.VK_S),
				saveCaption, handler));
		fileMenu.add(createMenuItem("Save As", KeyEvent.VK_A,
				createShiftHotKey(KeyEvent.VK_S), saveAsCaption,
				handler));
		final JMenuItem saveAllItem = createMenuItem("Save All", KeyEvent.VK_V,
				createHotKey(KeyEvent.VK_L), "Save all maps to their files",
				handler);
		fileMenu.add(saveAllItem);
		if (!(model instanceof IMultiMapModel)) {
			loadSecondaryItem.setEnabled(false);
			saveAllItem.setEnabled(false);
		}
		fileMenu.addSeparator();
		final JMenuItem openViewerItem = createMenuItem("Open in map viewer",
				KeyEvent.VK_M, createHotKey(KeyEvent.VK_M),
				"Open the main map in the map viewer for a broader view",
				handler);
		fileMenu.add(openViewerItem);
		if (model instanceof IViewerModel) {
			openViewerItem.setEnabled(false);
		}
		final JMenuItem openSecondaryViewerItem = createMenuItem(
				"Open secondary map in map viewer", KeyEvent.VK_E,
				createHotKey(KeyEvent.VK_E),
				"Open the first secondary map in the map viewer for a broader view",
				handler);
		fileMenu.add(openSecondaryViewerItem);
		if ((model instanceof IViewerModel) || !(model instanceof IMultiMapModel)) {
			openSecondaryViewerItem.setEnabled(false);
		}
		fileMenu.addSeparator();
		fileMenu.add(createMenuItem("Close", KeyEvent.VK_W, createHotKey(KeyEvent.VK_W),
				"Close this window", handler));
		fileMenu.addSeparator();
		if (OnMac.SYSTEM_IS_MAC) {
			Application.getApplication().setAboutHandler(e -> {
				final Window[] windows = WindowList.getWindows(true, false);
				final Object source;
				if (windows.length == 0) {
					source = e;
				} else {
					source = windows[windows.length - 1];
				}
				handler.actionPerformed(
						new ActionEvent(source, ActionEvent.ACTION_FIRST, "About"));
			});
		} else {
			fileMenu.add(createMenuItem("About", KeyEvent.VK_B,
					createHotKey(KeyEvent.VK_B), "Show development credits", handler));
			fileMenu.addSeparator();
			fileMenu.add(
					createMenuItem("Quit", KeyEvent.VK_Q, createHotKey(KeyEvent.VK_Q),
							"Quit the application", handler));
		}
		return fileMenu;
	}

	/**
	 * Create the "map" menu, including go-to-tile, find, and zooming functions. This now
	 * takes any IDriverModel, because it's expected that apps where none of that makes
	 * sense will show but disable the menu.
	 *
	 * @param model   the driver model
	 * @param handler the menu-item-handler
	 * @return the menu created
	 */
	protected JMenu createMapMenu(final ActionListener handler,
								  final IDriverModel model) {
		final JMenu retval = new JMenu("Map");
		retval.setMnemonic(KeyEvent.VK_M);
		final int findKey = KeyEvent.VK_F;
		final KeyStroke findStroke = createHotKey(findKey);
		final KeyStroke nextStroke = createHotKey(KeyEvent.VK_G);
		final JMenuItem gotoTileItem = createMenuItem("Go to tile",
				KeyEvent.VK_T, createHotKey(KeyEvent.VK_T),
				"Go to a tile by coordinates",
				handler);
		final JMenuItem findItem = createMenuItem("Find a fixture", findKey,
				findStroke, "Find a fixture by name, kind, or ID#",
				handler);
		final int nextKey = KeyEvent.VK_N;
		final JMenuItem nextItem = createMenuItem("Find next", nextKey,
				nextStroke, "Find the next fixture matching the pattern",
				handler);
		if (!(model instanceof IViewerModel)) {
			gotoTileItem.setEnabled(false);
			findItem.setEnabled(false);
		}
		retval.add(gotoTileItem);
		final InputMap findInput = findItem.getInputMap(WHEN_IN_FOCUSED_WINDOW);
		findInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 0),
				findInput.get(findStroke));
		retval.add(findItem);
		final InputMap nextInput = nextItem.getInputMap(WHEN_IN_FOCUSED_WINDOW);
		nextInput.put(KeyStroke.getKeyStroke(nextKey, 0), nextInput.get(nextStroke));
		retval.add(nextItem);
		retval.addSeparator();
		// VK_PLUS only works on non-US keyboards, but we leave it as the
		// primary hot-key because it's the best to *show* in the menu.
		final KeyStroke plusKey =
				createHotKey(KeyEvent.VK_PLUS);
		final JMenuItem zoomInItem =
				createMenuItem("Zoom in", KeyEvent.VK_I,
						plusKey, "Increase the visible size of each tile",
						handler);
		final InputMap inputMap =
				zoomInItem.getInputMap(WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(createHotKey(KeyEvent.VK_EQUALS),
				inputMap.get(plusKey));
		inputMap.put(createShiftHotKey(KeyEvent.VK_EQUALS),
				inputMap.get(plusKey));
		inputMap.put(createHotKey(KeyEvent.VK_ADD),
				inputMap.get(plusKey));
		retval.add(zoomInItem);
		retval.add(createMenuItem("Zoom out", KeyEvent.VK_O,
				createHotKey(KeyEvent.VK_MINUS),
				"Decrease the visible size of each tile", handler));
		retval.addSeparator();
		retval.add(createMenuItem("Center", KeyEvent.VK_C,
				createHotKey(KeyEvent.VK_C),
				"Center the view on the selected tile", handler));
		return retval;
	}

	/**
	 * Create the "view" menu.
	 *
	 * @param handler the listener to handle item selections
	 * @param model the driver model underlying this app
	 * @return the "edit" menu
	 */
	protected JMenu createViewMenu(final ActionListener handler,
								   final IDriverModel model) {
		final JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic(KeyEvent.VK_E);

		// We *create* these items here (early) so that we can enable or disable them
		// without an extra branch.
		final Collection<JMenuItem> treeItems = new ArrayList<>();
		treeItems.add(createMenuItem("Reload tree",
				KeyEvent.VK_R, createHotKey(KeyEvent.VK_R),
				"Refresh the view of the workers", handler));
		treeItems.add(createMenuItem("Expand All", KeyEvent.VK_X, null,
				"Expand all nodes in the unit tree", handler));
		treeItems.add(
				createMenuItem("Expand Unit Kinds", KeyEvent.VK_K, null,
						"Expand all unit kinds to show the units", handler));
		treeItems.add(createMenuItem("Collapse All", KeyEvent.VK_C, null,
				"Collapse all nodes in the unit tree", handler));

		final JMenuItem currentPlayerItem;
		if (model instanceof IWorkerModel) {
			currentPlayerItem = createMenuItem(
					"Change current player", KeyEvent.VK_P,
					createHotKey(KeyEvent.VK_P),
					"Look at a different player's units and workers", handler);
		} else {
			currentPlayerItem = createMenuItem(
					"Change current player", KeyEvent.VK_P, null,
					"Mark a player as the current player in the map", handler);
			treeItems.forEach(item -> item.setEnabled(false));
		}
		viewMenu.add(currentPlayerItem);
		treeItems.forEach(viewMenu::add);
		return viewMenu;
	}

	/**
	 * Add a menu, but set it to disabled.
	 *
	 * @param menu a menu
	 * @return it
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	protected final JMenu addDisabled(final JMenu menu) {
		add(menu);
		menu.setEnabled(false);
		return menu;
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
}
