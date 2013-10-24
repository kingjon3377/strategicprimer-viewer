package view.exploration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;

import model.exploration.IExplorationModel;
import model.map.IMap;
import model.map.MapView;
import model.map.SPMap;
import model.viewer.ViewerModel;

import org.eclipse.jdt.annotation.Nullable;

import util.Pair;
import view.map.main.ViewerFrame;
import view.util.DriverQuit;
import view.util.MenuItemCreator;
import controller.map.misc.IOHandler;
import controller.map.misc.MultiIOHandler;

/**
 * Menus for the exploration GUI.
 *
 * @author Jonathan Lovelace
 *
 */
public class ExplorationMenu extends JMenuBar {
	/**
	 * Constructor.
	 *
	 * @param handler the I/O handler to handle I/O related items
	 * @param model the exploration model
	 * @param parent the window this is to be attached to, which should close on
	 *        "Close".
	 */
	public ExplorationMenu(final MultiIOHandler handler,
			final IExplorationModel model, final JFrame parent) {
		final JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(MenuItemCreator.createMenuItem("Load", KeyEvent.VK_L,
				MenuItemCreator.createHotkey(KeyEvent.VK_O),
				"Load the main map from file", handler));
		fileMenu.add(MenuItemCreator.createMenuItem("Load secondary",
				KeyEvent.VK_E,
				MenuItemCreator.createShiftHotkey(KeyEvent.VK_O),
				"Load an additional secondary map from file", handler));
		fileMenu.add(MenuItemCreator.createMenuItem("Save", KeyEvent.VK_S,
				MenuItemCreator.createHotkey(KeyEvent.VK_S),
				"Save the main map to the file it was loaded from", handler));
		fileMenu.add(MenuItemCreator.createMenuItem("Save As", KeyEvent.VK_A,
				MenuItemCreator.createShiftHotkey(KeyEvent.VK_S),
				"Save the main map to file", handler));
		fileMenu.add(MenuItemCreator.createMenuItem("Save All", KeyEvent.VK_V,
				MenuItemCreator.createHotkey(KeyEvent.VK_L),
				"Save all maps to their files", handler));
		fileMenu.addSeparator();
		fileMenu.add(MenuItemCreator.createMenuItem("Open in map viewer",
				KeyEvent.VK_M, MenuItemCreator.createHotkey(KeyEvent.VK_M),
				"Open the main map in the map viewer for a broader view",
				new ActionListener() {
					@Override
					public void actionPerformed(
							@Nullable final ActionEvent event) {
						SwingUtilities.invokeLater(new ViewerOpener(model
								.getMap(), model.getMapFilename(), -1, -1,
								handler));
					}
				}));
		fileMenu.add(MenuItemCreator.createMenuItem(
				"Open secondary map in map viewer",
				KeyEvent.VK_E,
				MenuItemCreator.createHotkey(KeyEvent.VK_E),
				"Open the first secondary map in the map viewer for a broader view",
				new ActionListener() {
					@Override
					public void actionPerformed(
							@Nullable final ActionEvent event) {
						final Pair<IMap, String> mapPair = model
								.getSubordinateMaps().iterator().next();
						SwingUtilities.invokeLater(new ViewerOpener(mapPair
								.first(), mapPair.second(), model.getMap()
								.getPlayers().getCurrentPlayer().getPlayerId(),
								model.getMap().getCurrentTurn(), handler));
					}
				}));
		fileMenu.addSeparator();
		fileMenu.add(MenuItemCreator.createMenuItem("Close", KeyEvent.VK_W,
				MenuItemCreator.createHotkey(KeyEvent.VK_W),
				"Close this window", new ActionListener() {
					/**
					 * Close the window when pressed.
					 *
					 * @param evt the event to handle
					 */
					@Override
					public void actionPerformed(@Nullable final ActionEvent evt) {
						if (evt != null
								&& "Close".equals(evt.getActionCommand())) {
							parent.setVisible(false);
							parent.dispose();
						}
					}
				}));
		fileMenu.addSeparator();
		fileMenu.add(MenuItemCreator.createMenuItem("Quit", KeyEvent.VK_Q,
				MenuItemCreator.createHotkey(KeyEvent.VK_Q),
				"Quit the application", new ActionListener() {
					/**
					 * Handle the menu "button" press.
					 *
					 * @param event the event to handle
					 */
					@Override
					public void actionPerformed(
							@Nullable final ActionEvent event) {
						if (event != null
								&& "Quit".equals(event.getActionCommand())) {
							DriverQuit.quit(0);
						}
					}
				}));
		add(fileMenu);
	}

	/**
	 * A class to open a ViewerFrame.
	 * @author Jonathan Lovelace
	 */
	private static class ViewerOpener implements Runnable {
		/**
		 * Constructor.
		 *
		 * @param map the map (view) to open
		 * @param file the filename it was loaded from
		 * @param player the current player's number---ignored if map is a
		 *        MapView.
		 * @param turn the current turn---ignored if map is a MapView.
		 * @param ioHandler the I/O handler to let the menu handle 'open', etc.
		 */
		protected ViewerOpener(final IMap map, final String file, final int player,
				final int turn, final IOHandler ioHandler) {
			if (map instanceof MapView) {
				view = (MapView) map;
			} else {
				view = new MapView((SPMap) map, player, turn);
			}
			filename = file;
			ioHelper = ioHandler;
		}

		/**
		 * The map view to open.
		 */
		private final MapView view;
		/**
		 * The file name the map was loaded from.
		 */
		private final String filename;
		/**
		 * The I/O handler to let the menu handle 'open', etc.
		 */
		private final IOHandler ioHelper;

		/**
		 * Run the thread.
		 */
		@Override
		public void run() {
			new ViewerFrame(new ViewerModel(view, filename), ioHelper)
					.setVisible(true);
		}
	}
}
