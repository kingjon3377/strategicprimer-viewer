package view.map.main;

import static view.util.MenuItemCreator.createHotkey;
import static view.util.MenuItemCreator.createMenuItem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import model.listeners.PlayerChangeListener;
import model.map.IPlayerCollection;
import model.map.Player;
import model.viewer.IViewerModel;

import org.eclipse.jdt.annotation.Nullable;

import view.util.DriverQuit;
import view.util.MenuItemCreator;
import view.worker.PlayerChooserHandler;

import com.bric.window.WindowMenu;

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
		add(createFileMenu(handler, parent));
		add(createMapMenu(parent, model));
		add(new WindowMenu(parent));
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
		final int findKey = KeyEvent.VK_F;
		final KeyStroke findStroke = MenuItemCreator.createHotkey(findKey);
		final JMenuItem findItem =
				MenuItemCreator.createMenuItem("Find a fixture", findKey,
						findStroke, "Find a fixture by name, kind, or ID#",
						new ActionListener() {
							@Override
							public void actionPerformed(
									@Nullable final ActionEvent evt) {
								finder.setVisible(true);
							}
						});
		final InputMap findInput = findItem.getInputMap(WHEN_IN_FOCUSED_WINDOW);
		findInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 0),
				findInput.get(findStroke));
		retval.add(findItem);
		final int nextKey = KeyEvent.VK_N;
		final KeyStroke nextStroke = MenuItemCreator.createHotkey(KeyEvent.VK_G);
		final JMenuItem nextItem =
				MenuItemCreator.createMenuItem("Find next", nextKey,
						nextStroke,
						"Find the next fixture matching the pattern",
						new ActionListener() {
							@Override
							public void actionPerformed(
									@Nullable final ActionEvent evt) {
								finder.search();
							}
						});
		final InputMap nextInput = nextItem.getInputMap(WHEN_IN_FOCUSED_WINDOW);
		nextInput.put(KeyStroke.getKeyStroke(nextKey, 0), nextInput.get(nextStroke));
		retval.add(nextItem);
		retval.addSeparator();
		final ActionListener zoomListener = new ZoomListener(model);
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
		fileMenu.addSeparator();
		fileMenu.add(createMenuItem("About", KeyEvent.VK_B,
				createHotkey(KeyEvent.VK_B), "Show development credits", handler));
		fileMenu.addSeparator();
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
