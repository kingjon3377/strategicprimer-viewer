package drivers.gui.common;

import com.pump.window.WindowList;
import com.pump.window.WindowMenu;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import drivers.common.ISPDriver;
import drivers.common.ModelDriver;
import drivers.common.WorkerGUI;
import drivers.common.UtilityGUI;
import drivers.common.MultiMapGUIDriver;
import drivers.common.ViewerDriver;
import drivers.common.GUIDriver;

import javax.swing.KeyStroke;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JFrame;

import lovelace.util.Platform;

import static lovelace.util.MenuUtils.HotKeyModifier;
import static lovelace.util.MenuUtils.createAccelerator;
import static lovelace.util.MenuUtils.createMenuItem;

import lovelace.util.ComponentParentStream;

import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.EnumSet;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A class to hold the logic for building our menus.
 */
public final class SPMenu extends JMenuBar {
	@Serial
	private static final long serialVersionUID = 1L;

	private static void simpleQuit() {
		System.exit(0);
	}

	private static Runnable localDefaultQuit = SPMenu::simpleQuit;

	public static Runnable getDefaultQuit() {
		return localDefaultQuit;
	}

	// FIXME: setDefaultQuit()

	/**
	 * If the given driver isn't one of the specfied driver types, disable the
	 * given menu-item; regardless, return the menu item.
	 */
	@SafeVarargs
	private static JMenuItem enabledForDriver(final JMenuItem item, final ISPDriver driver,
	                                          final Class<? extends ISPDriver>... types) {
		boolean any = false;
		for (final Class<?> type : types) {
			if (type.isInstance(driver)) {
				any = true;
				break;
			}
		}
		if (!any) {
			item.setEnabled(false);
		}
		return item;
	}

	/**
	 * If the given driver is of the specified driver type, disable the
	 * given menu item; regardless, return the menu item.
	 */
	@SafeVarargs
	private static JMenuItem disabledForDriver(final JMenuItem item, final ISPDriver driver,
	                                           final Class<? extends ISPDriver>... types) {
		boolean any = false;
		for (final Class<?> type : types) {
			if (type.isInstance(driver)) {
				any = true;
				break;
			}
		}
		if (any) {
			item.setEnabled(false);
		}
		return item;
	}

	/**
	 * Create the File menu.
	 */
	public static JMenu createFileMenu(final ActionListener handler, final ISPDriver driver) {
		final JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(enabledForDriver(createMenuItem("New", KeyEvent.VK_N,
				"Create a new, empty map the same size as the current one", handler,
				createAccelerator(KeyEvent.VK_N)), driver, ViewerDriver.class));

		final String loadCaption;
		final String saveCaption;
		final String saveAsCaption;
		if (driver instanceof MultiMapGUIDriver) {
			loadCaption = "Load the main map from file";
			saveCaption = "Save the main map to the file it was loaded from";
			saveAsCaption = "Save the main map to file";
		} else {
			loadCaption = "Load a map from file";
			saveCaption = "Save the map to the file it was loaded from";
			saveAsCaption = "Save the map to file";
		}

		fileMenu.add(enabledForDriver(createMenuItem("Load", KeyEvent.VK_L, loadCaption, handler,
				createAccelerator(KeyEvent.VK_O)), driver, GUIDriver.class, UtilityGUI.class));
		fileMenu.add(enabledForDriver(createMenuItem("Load secondary", KeyEvent.VK_E,
						"Load an additional secondary map from file", handler,
						createAccelerator(KeyEvent.VK_O, HotKeyModifier.Shift)), driver,
				MultiMapGUIDriver.class));

		fileMenu.add(enabledForDriver(createMenuItem("Save", KeyEvent.VK_S, saveCaption, handler,
				createAccelerator(KeyEvent.VK_S)), driver, ModelDriver.class));
		fileMenu.add(enabledForDriver(createMenuItem("Save As", KeyEvent.VK_A, saveAsCaption,
						handler, createAccelerator(KeyEvent.VK_S, HotKeyModifier.Shift)), driver,
				ModelDriver.class));
		fileMenu.add(enabledForDriver(createMenuItem("Save All", KeyEvent.VK_V,
						"Save all maps to their files", handler, createAccelerator(KeyEvent.VK_L)),
				driver, MultiMapGUIDriver.class));
		fileMenu.addSeparator();

		final KeyStroke openViewerHotkey;
		if (Platform.SYSTEM_IS_MAC) {
			openViewerHotkey = KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.ALT_DOWN_MASK);
		} else {
			openViewerHotkey = createAccelerator(KeyEvent.VK_M);
		}
		fileMenu.add(disabledForDriver(enabledForDriver(createMenuItem("Open in map viewer",
						KeyEvent.VK_M, "Open the main map in the map viewer for a broader view",
						handler, openViewerHotkey), driver, ModelDriver.class), driver,
				ViewerDriver.class));

		fileMenu.add(enabledForDriver(createMenuItem("Open secondary map in map viewer",
						KeyEvent.VK_E,
						"Open the first secondary map in the map vieer for a broader view",
						handler, createAccelerator(KeyEvent.VK_E)), driver,
				MultiMapGUIDriver.class));
		fileMenu.addSeparator();

		fileMenu.add(createMenuItem("Close", KeyEvent.VK_W, "Close this window",
				handler, createAccelerator(KeyEvent.VK_W)));

		if (Platform.SYSTEM_IS_MAC) {
			Desktop.getDesktop().setAboutHandler(
					(event) -> handler.actionPerformed(new ActionEvent(Stream.<Object>of(
									WindowList.getWindows(WindowList.WindowSorting.Layer,
											EnumSet.noneOf(WindowList.WindowFiltering.class)))
							.filter(Objects::nonNull).reduce((first, second) -> second).orElse(event),
							ActionEvent.ACTION_FIRST, "About")));
			Desktop.getDesktop().setQuitHandler((event, quitResponse) -> {
				localDefaultQuit = quitResponse::performQuit;
				handler.actionPerformed(new ActionEvent(
						Stream.<Object>of(WindowList.getWindows(WindowList.WindowSorting.Layer,
										EnumSet.noneOf(WindowList.WindowFiltering.class)))
								.filter(Objects::nonNull).reduce((first, second) -> second)
								.orElse(event), ActionEvent.ACTION_FIRST, "Quit"));
			});
		} else {
			fileMenu.add(createMenuItem("About", KeyEvent.VK_B, "Show development credits",
					handler, createAccelerator(KeyEvent.VK_B)));
			fileMenu.addSeparator();
			fileMenu.add(createMenuItem("Quit", KeyEvent.VK_Q, "Quit the application",
					handler, createAccelerator(KeyEvent.VK_Q)));
		}
		return fileMenu;
	}

	/**
	 * Create the "Map" menu, including go-to-tile, find, and zooming functions.
	 */
	public static JMenu createMapMenu(final ActionListener handler, final ISPDriver driver) {
		final JMenu retval = new JMenu("Map");
		retval.setMnemonic(KeyEvent.VK_M);

		retval.add(enabledForDriver(createMenuItem("Go to tile", KeyEvent.VK_T,
						"Go to a tile by coordinates", handler, createAccelerator(KeyEvent.VK_T)),
				driver, ViewerDriver.class));

		final int findKey = KeyEvent.VK_F;
		retval.add(enabledForDriver(createMenuItem("Find a fixture", findKey,
				"Find a fixture by name, kind or ID #", handler, createAccelerator(findKey),
				KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 0)), driver, ViewerDriver.class));

		final int nextKey = KeyEvent.VK_N;
		retval.add(enabledForDriver(createMenuItem("Find next", nextKey,
						"Find the next fixture matching the pattern", handler,
						createAccelerator(KeyEvent.VK_G), KeyStroke.getKeyStroke(nextKey, 0)),
				driver, ViewerDriver.class));
		retval.addSeparator();

		// VK_PLUS only works on non-US keyboards, but we leave it as the primary hot-key
		// because it's the best to *show* in the menu.
		final KeyStroke plusKey = createAccelerator(KeyEvent.VK_PLUS);
		retval.add(enabledForDriver(createMenuItem("Zoom in", KeyEvent.VK_I,
				"Increase the visible size of each tile", handler, plusKey,
				createAccelerator(KeyEvent.VK_EQUALS),
				createAccelerator(KeyEvent.VK_EQUALS, HotKeyModifier.Shift),
				createAccelerator(KeyEvent.VK_ADD)), driver, ViewerDriver.class));

		retval.add(enabledForDriver(createMenuItem("Zoom out", KeyEvent.VK_O,
				"Decrease the visible size of each tile", handler,
				createAccelerator(KeyEvent.VK_MINUS)), driver, ViewerDriver.class));

		retval.add(enabledForDriver(createMenuItem("Reset zoom", KeyEvent.VK_R,
						"Reset the zoom level", handler, createAccelerator(KeyEvent.VK_0)),
				driver, ViewerDriver.class));
		retval.addSeparator();

		final KeyStroke centerHotkey;
		if (Platform.SYSTEM_IS_MAC) {
			centerHotkey = createAccelerator(KeyEvent.VK_L);
		} else {
			centerHotkey = createAccelerator(KeyEvent.VK_C);
		}
		retval.add(enabledForDriver(createMenuItem("Center", KeyEvent.VK_C,
						"Center the view on the selected tile", handler, centerHotkey),
				driver, ViewerDriver.class));

		return retval;
	}

	/**
	 * Create the "View" menu.
	 */
	public static JMenu createViewMenu(final ActionListener handler, final ISPDriver driver) {
		final JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic(KeyEvent.VK_E);

		final String currentPlayerDesc;
		if (driver instanceof WorkerGUI) {
			currentPlayerDesc = "Look at a different player's units and workers";
		} else {
			currentPlayerDesc = "Mark a player as the current player in the map";
		}
		viewMenu.add(enabledForDriver(createMenuItem("Change current player",
						KeyEvent.VK_P, currentPlayerDesc, handler,
						createAccelerator(KeyEvent.VK_P)), driver, ModelDriver.class,
				WorkerGUI.class));

		viewMenu.add(enabledForDriver(createMenuItem("Reload tree", KeyEvent.VK_R,
						"Refresh the view of the workers", handler, createAccelerator(KeyEvent.VK_R)),
				driver, WorkerGUI.class));

		viewMenu.add(enabledForDriver(createMenuItem("Expand All", KeyEvent.VK_X,
				"Expand all nodes in the unit tree", handler), driver, WorkerGUI.class));
		viewMenu.add(enabledForDriver(createMenuItem("Expand Unit Kinds", KeyEvent.VK_K,
				"Expand all unit kinds to show the units", handler), driver, WorkerGUI.class));
		viewMenu.add(enabledForDriver(createMenuItem("Collapse All", KeyEvent.VK_C,
				"Collapse all nodes in the unit tree", handler), driver, WorkerGUI.class));
		return viewMenu;
	}

	/**
	 * Disable a menu and return it.
	 */
	public static JMenu disabledMenu(final JMenu menu) {
		menu.setEnabled(false);
		return menu;
	}

	public SPMenu(final JMenu... menus) {
		for (final JMenu menu : menus) {
			add(menu);
		}
	}

	// The boolean parameter is to make the overload unambiguous; JMenu is a subtype of Component ...
	private SPMenu(final boolean ignored, final Component component, final JMenu... menus) {
		this(menus);
		add(new WindowMenu(new ComponentParentStream(component).stream().filter(JFrame.class::isInstance)
				.map(JFrame.class::cast).findAny().orElseThrow()));
	}

	public static SPMenu forWindowContaining(final Component component, final JMenu... menus) {
		return new SPMenu(false, component, menus);
	}

	private SPMenu(final JFrame frame, final JMenu... menus) {
		this(menus);
		add(new WindowMenu(frame));
	}

	public static SPMenu forWindow(final JFrame frame, final JMenu... menus) {
		return new SPMenu(frame, menus);
	}
}
