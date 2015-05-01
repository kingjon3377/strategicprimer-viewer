package view.exploration;

import static javax.swing.SwingUtilities.invokeLater;
import static view.util.MenuItemCreator.createHotkey;
import static view.util.MenuItemCreator.createMenuItem;
import static view.util.MenuItemCreator.createShiftHotkey;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import model.exploration.IExplorationModel;
import model.map.IMutableMapNG;
import model.viewer.ViewerModel;

import org.eclipse.jdt.annotation.Nullable;

import util.Pair;
import view.map.main.ViewerFrame;
import view.util.DriverQuit;

import com.bric.window.WindowMenu;

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
	 * @param ioh the I/O handler to handle I/O related items
	 * @param model the exploration model
	 * @param parent the window this is to be attached to, which should close on
	 *        "Close".
	 */
	public ExplorationMenu(final MultiIOHandler ioh,
			final IExplorationModel model, final JFrame parent) {
		final JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(createMenuItem("Load", KeyEvent.VK_L,
				createHotkey(KeyEvent.VK_O), "Load the main map from file", ioh));
		fileMenu.add(createMenuItem("Load secondary", KeyEvent.VK_E,
				createShiftHotkey(KeyEvent.VK_O),
				"Load an additional secondary map from file", ioh));
		fileMenu.add(createMenuItem("Save", KeyEvent.VK_S,
				createHotkey(KeyEvent.VK_S),
				"Save the main map to the file it was loaded from", ioh));
		fileMenu.add(createMenuItem("Save As", KeyEvent.VK_A,
				createShiftHotkey(KeyEvent.VK_S), "Save the main map to file",
				ioh));
		fileMenu.add(createMenuItem("Save All", KeyEvent.VK_V,
				createHotkey(KeyEvent.VK_L), "Save all maps to their files",
				ioh));
		fileMenu.addSeparator();
		fileMenu.add(createMenuItem("Open in map viewer", KeyEvent.VK_M,
				createHotkey(KeyEvent.VK_M),
				"Open the main map in the map viewer for a broader view",
				new ActionListener() {
					@Override
					public void actionPerformed(@Nullable final ActionEvent evt) {
						invokeLater(new ViewerOpener(model.getMap(), model
								.getMapFile(), ioh));
					}
				}));
		fileMenu.add(createMenuItem(
				"Open secondary map in map viewer",
				KeyEvent.VK_E,
				createHotkey(KeyEvent.VK_E),
				"Open the first secondary map in the map viewer for a broader view",
				new ActionListener() {
					@Override
					public void actionPerformed(@Nullable final ActionEvent evt) {
						final Pair<IMutableMapNG, File> mapPair = model
								.getSubordinateMaps().iterator().next();
						invokeLater(new ViewerOpener(mapPair.first(), mapPair
								.second(), ioh));
					}
				}));
		fileMenu.addSeparator();
		fileMenu.add(createMenuItem("Close", KeyEvent.VK_W,
				createHotkey(KeyEvent.VK_W), "Close this window",
				new ActionListener() {
					@Override
					public void actionPerformed(@Nullable final ActionEvent evt) {
						parent.setVisible(false);
						parent.dispose();
					}
				}));
		fileMenu.addSeparator();
		fileMenu.add(createMenuItem("About", KeyEvent.VK_B,
				createHotkey(KeyEvent.VK_B), "Show development credits", ioh));
		fileMenu.addSeparator();
		fileMenu.add(createMenuItem("Quit", KeyEvent.VK_Q,
				createHotkey(KeyEvent.VK_Q), "Quit the application",
				new ActionListener() {
					@Override
					public void actionPerformed(@Nullable final ActionEvent evt) {
						DriverQuit.quit(0);
					}
				}));
		add(fileMenu);
		add(new WindowMenu(parent));
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
