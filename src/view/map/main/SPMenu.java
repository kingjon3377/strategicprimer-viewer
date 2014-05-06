package view.map.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

import model.listeners.PlayerChangeListener;
import model.map.IPlayerCollection;
import model.map.Player;
import model.viewer.IViewerModel;

import org.eclipse.jdt.annotation.Nullable;

import view.util.DriverQuit;
import view.util.MenuItemCreator;
import view.window.WindowMenu;
import view.worker.PlayerChooserHandler;
import controller.map.misc.IOHandler;

/**
 * A class encapsulating the menus.
 *
 * @author Jonathan Lovelace
 *
 */
public class SPMenu extends JMenuBar {
	/**
	 * Constructor.
	 *
	 * @param handler the I/O handler to handle I/O related items
	 * @param parent the frame we'll be attached to
	 * @param model the map model
	 */
	public SPMenu(final IOHandler handler, final JFrame parent,
			final IViewerModel model) {
		super();
		add(createFileMenu(handler, parent));
		add(createMapMenu(parent, model));
		add(new WindowMenu());
	}

	/**
	 * Create the "map" menu, including go-to-tile, find, and zooming function.
	 *
	 * @param parent the menu-bar's parent window
	 * @param model the map model
	 * @return the menu created
	 */
	private static JMenu createMapMenu(final JFrame parent,
			final IViewerModel model) {
		final JMenu retval = new JMenu("Map");
		retval.setMnemonic(KeyEvent.VK_M);
		retval.add(MenuItemCreator.createMenuItem("Go to tile", KeyEvent.VK_T,
				MenuItemCreator.createHotkey(KeyEvent.VK_T),
				"Go to a tile by coordinates", new ActionListener() {
					@Override
					public void actionPerformed(@Nullable final ActionEvent evt) {
						new SelectTileDialog(parent, model).setVisible(true);
					}
				}));
		final FindDialog finder = new FindDialog(parent, model);
		retval.add(MenuItemCreator.createMenuItem("Find a fixture",
				KeyEvent.VK_SLASH,
				KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 0),
				"Find a fixture by name, kind, or ID#", new ActionListener() {
					@Override
					public void actionPerformed(@Nullable final ActionEvent evt) {
						finder.setVisible(true);
					}
				}));
		retval.add(MenuItemCreator.createMenuItem("Find next", KeyEvent.VK_N,
				KeyStroke.getKeyStroke(KeyEvent.VK_N, 0),
				"Find the next fixture matching the pattern",
				new ActionListener() {
					@Override
					public void actionPerformed(@Nullable final ActionEvent evt) {
						finder.search();
					}
				}));
		retval.addSeparator();
		final ActionListener zoomListener = new ZoomListener(model);
		retval.add(MenuItemCreator.createMenuItem("Zoom in", KeyEvent.VK_I,
				MenuItemCreator.createHotkey(KeyEvent.VK_PLUS),
				"Increase the visible size of each tile", zoomListener));
		retval.add(MenuItemCreator.createMenuItem("Zoom out", KeyEvent.VK_O,
				MenuItemCreator.createHotkey(KeyEvent.VK_MINUS),
				"Decrease the visible size of each tile", zoomListener));
		retval.addSeparator();
		final PlayerChooserHandler pch = new PlayerChooserHandler(parent, model);
		retval.add(MenuItemCreator.createMenuItem(
				PlayerChooserHandler.MENU_ITEM, KeyEvent.VK_P, null,
				"Mark a player as the current player in the map", pch));
		pch.addPlayerChangeListener(new PlayerChangeListener() {
			@Override
			public void playerChanged(@Nullable final Player old,
					final Player newPlayer) {
				final IPlayerCollection pColl = model.getMap().getPlayers();
				for (final Player player : pColl) {
					if (player.equals(newPlayer)) {
						player.setCurrent(true);
					} else {
						player.setCurrent(false);
					}
				}
			}
		});
		return retval;
	}

	/**
	 * Create the map menu.
	 *
	 * @param handler the class to handle I/O related menu items
	 * @param parent the menu-bar's parent window---the window to close on
	 *        "Close".
	 * @return the map menu.
	 */
	private static JMenu createFileMenu(final IOHandler handler,
			final JFrame parent) {
		final JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(MenuItemCreator.createMenuItem("New", KeyEvent.VK_N,
				MenuItemCreator.createHotkey(KeyEvent.VK_N),
				"Create a new, empty map the same size as the current one",
				handler));
		fileMenu.add(MenuItemCreator.createMenuItem("Load", KeyEvent.VK_L,
				MenuItemCreator.createHotkey(KeyEvent.VK_O),
				"Load a map from file", handler));
		fileMenu.add(MenuItemCreator.createMenuItem("Save", KeyEvent.VK_S,
				MenuItemCreator.createHotkey(KeyEvent.VK_S),
				"Save the map to the file it was loaded from", handler));
		fileMenu.add(MenuItemCreator.createMenuItem("Save As", KeyEvent.VK_A,
				MenuItemCreator.createShiftHotkey(KeyEvent.VK_S),
				"Save the map to file", handler));
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
		fileMenu.add(MenuItemCreator.createMenuItem("Quit", KeyEvent.VK_Q,
				MenuItemCreator.createHotkey(KeyEvent.VK_Q), "Quit the viewer",
				new ActionListener() {
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
		return fileMenu;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "SPMenu";
	}
}
