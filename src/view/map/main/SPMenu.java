package view.map.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

import model.viewer.IViewerModel;
import view.util.DriverQuit;
import view.util.MenuItemCreator;
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
	}
	/**
	 * Create the "map" menu, including go-to-tile, find, and zooming function.
	 * @param parent the menu-bar's parent window
	 * @param model the map model
	 * @return the menu created
	 */
	private JMenu createMapMenu(final JFrame parent, final IViewerModel model) {
		final JMenu retval = new JMenu("Map");
		retval.setMnemonic(KeyEvent.VK_M);
		retval.add(creator.createMenuItem("Go to tile", KeyEvent.VK_T,
				creator.createHotkey(KeyEvent.VK_T),
				"Go to a tile by coordinates", new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent event) {
						if ("Go to tile".equals(event.getActionCommand())) {
							new SelectTileDialog(parent, model)
									.setVisible(true);
						}
					}
				}));
		final FindDialog finder = new FindDialog(parent, model);
		retval.add(creator.createMenuItem("Find a fixture", KeyEvent.VK_SLASH,
				KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 0),
				"Find a fixture by name, kind, or ID#", new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent event) {
						if ("Find a fixture".equals(event.getActionCommand())) {
							finder.setVisible(true);
						}
					}
				}));
		retval.add(creator.createMenuItem("Find next", KeyEvent.VK_N,
				KeyStroke.getKeyStroke(KeyEvent.VK_N, 0),
				"Find the next fixture matching the pattern",
				new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent event) {
						if ("Find next".equals(event.getActionCommand())) {
							finder.search();
						}
					}
				}));
		retval.addSeparator();
		final ActionListener zoomListener = new ZoomListener(model);
		retval.add(creator.createMenuItem("Zoom in", KeyEvent.VK_I,
				creator.createHotkey(KeyEvent.VK_PLUS),
				"Increase the visible size of each tile", zoomListener));
		retval.add(creator.createMenuItem("Zoom out", KeyEvent.VK_O,
				creator.createHotkey(KeyEvent.VK_MINUS),
				"Decrease the visible size of each tile", zoomListener));
		return retval;
	}

	/**
	 * The helper to create menu items for us.
	 */
	private final transient MenuItemCreator creator = new MenuItemCreator();

	/**
	 * Create the map menu.
	 *
	 * @param handler the class to handle I/O related menu items
	 * @param parent the menu-bar's parent window---the window to close on "Close".
	 * @return the map menu.
	 */
	private JMenu createFileMenu(final IOHandler handler, final JFrame parent) {
		final JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(creator.createMenuItem("New", KeyEvent.VK_N,
				creator.createHotkey(KeyEvent.VK_N),
				"Create a new map the same size as the current one",
				handler));
		fileMenu.add(creator.createMenuItem("Load", KeyEvent.VK_L,
				creator.createHotkey(KeyEvent.VK_O),
				"Load a map from file", handler));
		fileMenu.add(creator.createMenuItem("Save", KeyEvent.VK_S,
				creator.createHotkey(KeyEvent.VK_S),
				"Save the map to the file it was loaded from", handler));
		fileMenu.add(creator.createMenuItem("Save As", KeyEvent.VK_A,
				creator.createShiftHotkey(KeyEvent.VK_S),
				"Save the map to file", handler));
		fileMenu.addSeparator();
		fileMenu.add(creator.createMenuItem("Close", KeyEvent.VK_W,
				creator.createHotkey(KeyEvent.VK_W),
				"Close this window", new ActionListener() {
					/**
					 * Close the window when pressed.
					 *
					 * @param evt the event to handle
					 */
					@Override
					public void actionPerformed(final ActionEvent evt) {
						if ("Close".equals(evt.getActionCommand())) {
							parent.setVisible(false);
							parent.dispose();
						}
					}
				}));
		fileMenu.add(creator.createMenuItem("Quit", KeyEvent.VK_Q,
				creator.createHotkey(KeyEvent.VK_Q),
				"Quit the viewer", new ActionListener() {
					/**
					 * Handle the menu "button" press.
					 *
					 * @param event the event to handle
					 */
					@Override
					public void actionPerformed(final ActionEvent event) {
						if ("Quit".equals(event.getActionCommand())) {
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
