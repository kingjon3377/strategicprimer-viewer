package view.util;

import static javax.swing.SwingUtilities.invokeLater;
import static view.util.MenuItemCreator.createHotkey;
import static view.util.MenuItemCreator.createMenuItem;
import static view.util.MenuItemCreator.createShiftHotkey;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.eclipse.jdt.annotation.Nullable;

import controller.map.misc.IOHandler;
import model.map.IMutableMapNG;
import model.map.Player;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import model.viewer.IViewerModel;
import model.viewer.ViewerModel;
import util.Pair;
import view.map.main.FindDialog;
import view.map.main.SelectTileDialog;
import view.map.main.ViewerFrame;
import view.map.main.ZoomListener;
import view.worker.PlayerChooserHandler;

/**
 * A common superclass for application-specific menu bars.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2015-2015 Jonathan Lovelace
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
public class SPMenu extends JMenuBar {
	/**
	 * Create the file menu.
	 *
	 * FIXME: Any menu items not handled by the IOHandler should be handled
	 * there.
	 *
	 * @param handler
	 *            the class to handle I/O related menu items
	 * @param parent
	 *            the menu-bar's parent window, which should be the window
	 *            closed when the user selects "close"
	 * @param model
	 *            the current driver model; only its type is used, to determine
	 *            which menu items to disable.
	 * @return the file menu
	 */
	protected static JMenu createFileMenu(final IOHandler handler,
			final JFrame parent, final IDriverModel model) {
		final JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JMenuItem newItem = MenuItemCreator.createMenuItem("New", KeyEvent.VK_N,
				MenuItemCreator.createHotkey(KeyEvent.VK_N),
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
				createHotkey(KeyEvent.VK_O), loadCaption, handler));
		final JMenuItem loadSecondaryItem = createMenuItem("Load secondary",
				KeyEvent.VK_E, createShiftHotkey(KeyEvent.VK_O),
				"Load an additional secondary map from file", handler);
		fileMenu.add(loadSecondaryItem);
		fileMenu.add(createMenuItem("Save", KeyEvent.VK_S,
				createHotkey(KeyEvent.VK_S),
				saveCaption, handler));
		fileMenu.add(createMenuItem("Save As", KeyEvent.VK_A,
				createShiftHotkey(KeyEvent.VK_S), saveAsCaption,
				handler));
		final JMenuItem saveAllItem = createMenuItem("Save All", KeyEvent.VK_V,
				createHotkey(KeyEvent.VK_L), "Save all maps to their files",
				handler);
		fileMenu.add(saveAllItem);
		if (!(model instanceof IMultiMapModel)) {
			loadSecondaryItem.setEnabled(false);
			saveAllItem.setEnabled(false);
		}
		fileMenu.addSeparator();
		final JMenuItem openViewerItem = createMenuItem("Open in map viewer",
				KeyEvent.VK_M, createHotkey(KeyEvent.VK_M),
				"Open the main map in the map viewer for a broader view",
				new ViewerOpenerInvoker(model, true, handler));
		fileMenu.add(openViewerItem);
		if (model instanceof IViewerModel) {
			openViewerItem.setEnabled(false);
		}
		final JMenuItem openSecondaryViewerItem = createMenuItem(
				"Open secondary map in map viewer",
				KeyEvent.VK_E,
				createHotkey(KeyEvent.VK_E),
				"Open the first secondary map in the map viewer for a broader view",
				new ViewerOpenerInvoker(model, false, handler));
		fileMenu.add(openSecondaryViewerItem);
		if (model instanceof IViewerModel || !(model instanceof IMultiMapModel)) {
			openSecondaryViewerItem.setEnabled(false);
		}
		fileMenu.addSeparator();
		fileMenu.add(MenuItemCreator.createMenuItem("Close", KeyEvent.VK_W,
				MenuItemCreator.createHotkey(KeyEvent.VK_W),
				"Close this window", evt -> {
					if (evt != null
							&& "Close".equals(evt.getActionCommand())) {
						parent.setVisible(false);
						parent.dispose();
					}
				}));
		fileMenu.addSeparator();
		fileMenu.add(createMenuItem("About", KeyEvent.VK_B,
				createHotkey(KeyEvent.VK_B), "Show development credits", handler));
		fileMenu.addSeparator();
		fileMenu.add(MenuItemCreator.createMenuItem("Quit", KeyEvent.VK_Q,
				MenuItemCreator.createHotkey(KeyEvent.VK_Q), "Quit the application",
				event -> {
					if (event != null && "Quit".equals(event.getActionCommand())) {
						DriverQuit.quit(0);
					}
				}));
		return fileMenu;
	}
	/**
	 * Create the "map" menu, including go-to-tile, find, and zooming functions. This
	 * now takes any IDriverModel, because it's expected that apps where none of that
	 * makes sense will show but disable the menu.
	 * @param parent the menu-bar's parent window
	 * @param model the driver model
	 * @return the menu created
	 */
	protected static JMenu createMapMenu(final JFrame parent,
			final IDriverModel model) {
		final JMenu retval = new JMenu("Map");
		retval.setMnemonic(KeyEvent.VK_M);
		final JMenuItem gotoTileItem;
		final int findKey = KeyEvent.VK_F;
		final KeyStroke findStroke = MenuItemCreator.createHotkey(findKey);
		final JMenuItem findItem;
		final JMenuItem nextItem;
		final int nextKey = KeyEvent.VK_N;
		final KeyStroke nextStroke = MenuItemCreator.createHotkey(KeyEvent.VK_G);
		final ActionListener zoomListener;
		if (model instanceof IViewerModel) {
			gotoTileItem = MenuItemCreator.createMenuItem("Go to tile",
					KeyEvent.VK_T, MenuItemCreator.createHotkey(KeyEvent.VK_T),
					"Go to a tile by coordinates", evt -> new SelectTileDialog(parent, (IViewerModel) model)
							.setVisible(true));
			final FindDialog finder = new FindDialog(parent, (IViewerModel) model);
			findItem = MenuItemCreator.createMenuItem("Find a fixture", findKey,
					findStroke, "Find a fixture by name, kind, or ID#",
					evt -> finder.setVisible(true));
			nextItem = MenuItemCreator.createMenuItem("Find next", nextKey,
					nextStroke, "Find the next fixture matching the pattern",
					evt -> finder.search());
			zoomListener = new ZoomListener((IViewerModel) model);
		} else {
			final ActionListener nullAction = evt -> {
					// do nothing
				};
			gotoTileItem = MenuItemCreator.createMenuItem("Go to tile",
					KeyEvent.VK_T, MenuItemCreator.createHotkey(KeyEvent.VK_T),
					"Go to a tile by coordinates", nullAction);
			gotoTileItem.setEnabled(false);
			findItem = MenuItemCreator.createMenuItem("Find a fixture", findKey,
					findStroke, "Find a fixture by name, kind, or ID#",
					nullAction);
			findItem.setEnabled(false);
			nextItem =
					MenuItemCreator.createMenuItem("Find next", nextKey,
							nextStroke,
							"Find the next fixture matching the pattern",
							nullAction);
			zoomListener = nullAction;
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
		// primary hotkey because it's the best to *show* in the menu.
		final KeyStroke plusKey =
				MenuItemCreator.createHotkey(KeyEvent.VK_PLUS);
		final JMenuItem zoomInItem =
				MenuItemCreator.createMenuItem("Zoom in", KeyEvent.VK_I,
						plusKey, "Increase the visible size of each tile",
						zoomListener);
		final InputMap inputMap =
				zoomInItem.getInputMap(WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(MenuItemCreator.createHotkey(KeyEvent.VK_EQUALS),
				inputMap.get(plusKey));
		inputMap.put(MenuItemCreator.createShiftHotkey(KeyEvent.VK_EQUALS),
				inputMap.get(plusKey));
		inputMap.put(MenuItemCreator.createHotkey(KeyEvent.VK_ADD),
				inputMap.get(plusKey));
		retval.add(zoomInItem);
		retval.add(MenuItemCreator.createMenuItem("Zoom out", KeyEvent.VK_O,
				MenuItemCreator.createHotkey(KeyEvent.VK_MINUS),
				"Decrease the visible size of each tile", zoomListener));
		retval.addSeparator();
		retval.add(MenuItemCreator.createMenuItem("Center", KeyEvent.VK_C,
				MenuItemCreator.createHotkey(KeyEvent.VK_C),
				"Center the view on the selected tile", zoomListener));
		retval.addSeparator();
		final PlayerChooserHandler pch = new PlayerChooserHandler(parent, model);
		retval.add(MenuItemCreator.createMenuItem(
				PlayerChooserHandler.MENU_ITEM, KeyEvent.VK_P, null,
				"Mark a player as the current player in the map", pch));
		pch.addPlayerChangeListener((old, newPlayer) -> {
			for (final Player player : model.getMap().players()) {
				if (player.equals(newPlayer)) {
					player.setCurrent(true);
				} else {
					player.setCurrent(false);
				}
			}
		});
		return retval;
	}
	/**
	 * Create the "edit"menu.
	 * @param pch the object to notify when the user selects a different player
	 * @return the "edit" menu
	 */
	protected static JMenu createEditMenu(final PlayerChooserHandler pch) {
		final JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		editMenu.add(MenuItemCreator.createMenuItem(
				PlayerChooserHandler.MENU_ITEM, KeyEvent.VK_P,
				MenuItemCreator.createHotkey(KeyEvent.VK_P),
				"Look at a different player's units and workers", pch));
		editMenu.add(MenuItemCreator.createMenuItem("Reload tree",
				KeyEvent.VK_R, MenuItemCreator.createHotkey(KeyEvent.VK_R),
				"Refresh the view of the workers", e -> pch.reload()));
		return editMenu;
	}
	/**
	 * Add a menu, but set it to disabled.
	 * @param menu a menu
	 * @return it
	 */
	protected JMenu addDisabled(final JMenu menu) {
		add(menu);
		menu.setEnabled(false);
		return menu;
	}
	/**
	 * A class to invoke a ViewerOpener (below).
	 * @author Jonathan Lovelace
	 */
	protected static final class ViewerOpenerInvoker implements ActionListener {
		/**
		 * @param model
		 *            the exploration model
		 * @param first
		 *            whether this is to open the main map, or otherwise a
		 *            subordinate map
		 * @param ioHandler
		 *            the I/O handler to use to actually open the file
		 */
		protected ViewerOpenerInvoker(final IDriverModel model,
				final boolean first, final IOHandler ioHandler) {
			theModel = model;
			frst = first;
			ioh = ioHandler;
		}
		/**
		 * The exploration model.
		 */
		private final IDriverModel theModel;
		/**
		 * Whether we will be opening the main map, rather than a subordinate map.
		 */
		private final boolean frst;
		/**
		 * The I/O handler to actually open the file.
		 */
		private final IOHandler ioh;
		/**
		 * Handle the action.
		 */
		@Override
		public void actionPerformed(@Nullable final ActionEvent evt) {
			if (frst) {
				invokeLater(new ViewerOpener(theModel.getMap(),
						theModel.getMapFile(), ioh));
			} else if (theModel instanceof IMultiMapModel) {
				final Pair<IMutableMapNG, File> mapPair =
						((IMultiMapModel) theModel).getSubordinateMaps()
								.iterator().next();
				invokeLater(new ViewerOpener(mapPair.first(), mapPair.second(),
						ioh));
			}
		}
	}

	/**
	 * A class to open a ViewerFrame.
	 * @author Jonathan Lovelace
	 */
	private static class ViewerOpener implements Runnable {
		/**
		 * The map view to open.
		 */
		private final IMutableMapNG view;
		/**
		 * The file name the map was loaded from.
		 */
		private final File file;
		/**
		 * The I/O handler to let the menu handle 'open', etc.
		 */
		private final IOHandler ioHelper;

		/**
		 * Constructor.
		 *
		 * @param map the map (view) to open
		 * @param source the filename it was loaded from
		 * @param ioHandler the I/O handler to let the menu handle 'open', etc.
		 */
		protected ViewerOpener(final IMutableMapNG map, final File source,
				final IOHandler ioHandler) {
			view = map;
			file = source;
			ioHelper = ioHandler;
		}

		/**
		 * Run the thread.
		 */
		@Override
		public void run() {
			new ViewerFrame(new ViewerModel(view, file), ioHelper)
					.setVisible(true);
		}
		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "ViewerOpener";
		}
	}
}
