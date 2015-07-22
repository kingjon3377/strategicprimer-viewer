package view.util;

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
import javax.swing.JMenuItem;

import org.eclipse.jdt.annotation.Nullable;

import controller.map.misc.IOHandler;
import controller.map.misc.MultiIOHandler;
import model.map.IMutableMapNG;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import model.viewer.IViewerModel;
import model.viewer.ViewerModel;
import util.Pair;
import view.map.main.ViewerFrame;

/**
 * A common superclass for application-specific menu bars.
 * @author Jonathan Lovelace
 *
 */
public class SPMenu extends JMenuBar {
	/**
	 * Create the file menu.
	 * @param handler the class to handle I/O related menu items
	 * @param parent the menu-bar's parent window, which should be the window closed when the user selects "close"
	 * @param model the current driver model; only its type is used, to determine which menu items to disable.
	 * @return the file menu
	 */
	protected static JMenu createFileMenu(final IOHandler handler, final JFrame parent, final IDriverModel model) {
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
		final JMenuItem loadSecondaryItem = createMenuItem("Load secondary", KeyEvent.VK_E,
				createShiftHotkey(KeyEvent.VK_O),
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
		if (!(model instanceof IMultiMapModel) || !(handler instanceof MultiIOHandler)) {
			loadSecondaryItem.setEnabled(false);
			saveAllItem.setEnabled(false);
		}
		fileMenu.addSeparator();
		final JMenuItem openViewerItem = createMenuItem("Open in map viewer", KeyEvent.VK_M,
				createHotkey(KeyEvent.VK_M),
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
				MenuItemCreator.createHotkey(KeyEvent.VK_Q), "Quit the application",
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
	 * A class to invoke a ViewerOpener (below).
	 * @uathor Jonathan Lovelace
	 */
	protected static final class ViewerOpenerInvoker implements ActionListener {
		/**
		 * @param model the exploration model
		 * @param first whether this is to open the main map, or otherwise a subordinate map
		 * @param ioHandler the I/O handler to use to actually open the file
		 */
		protected ViewerOpenerInvoker(final IDriverModel model, final boolean first, final IOHandler ioHandler) {
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
		 * Handle the action
		 */
		@Override
		public final void actionPerformed(@Nullable final ActionEvent evt) {
			if (frst) {
				invokeLater(new ViewerOpener(theModel.getMap(), theModel.getMapFile(), ioh));
			} else if (theModel instanceof IMultiMapModel) {
				final Pair<IMutableMapNG, File> mapPair = ((IMultiMapModel) theModel).getSubordinateMaps().iterator().next();
				invokeLater(new ViewerOpener(mapPair.first(), mapPair.second(), ioh));
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
