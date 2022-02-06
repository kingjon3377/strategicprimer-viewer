package drivers.map_viewer;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.nio.file.Path;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.io.IOException;

import javax.swing.SwingUtilities;

import drivers.common.IDriverModel;
import drivers.common.SPOptions;
import drivers.common.ViewerDriver;
import drivers.common.cli.ICLIHelper;
import common.map.MapDimensions;
import common.map.Point;
import common.map.IMutableMapNG;
import drivers.gui.common.about.AboutDialog;
import drivers.IOHandler;
import drivers.gui.common.WindowCloseListener;
import drivers.gui.common.MenuBroker;
import drivers.gui.common.SPFileChooser;

import lovelace.util.FileChooser;

import javax.imageio.ImageIO;

import java.io.File;

/**
 * A driver to start the map viewer.
 */
public class ViewerGUI implements ViewerDriver {
	/**
	 * A logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(ViewerGUI.class.getName());

	private final ICLIHelper cli;

	public ViewerGUI(final IViewerModel model, final SPOptions options, final ICLIHelper cli) {
		LOGGER.finer("In ViewerGUI constructor");
		this.model = model;
		this.options = options;
		this.cli = cli;
	}

	private final IViewerModel model;

	@Override
	public IViewerModel getModel() {
		return model;
	}

	private final SPOptions options;

	@Override
	public SPOptions getOptions() {
		return options;
	}

	@Override
	public void center() {
		Point selection = model.getSelection();
		LOGGER.finer("Asked to center on " + selection);
		MapDimensions dimensions = model.getMapDimensions();
		VisibleDimensions visible = model.getVisibleDimensions();
		LOGGER.finer(String.format(
			"Visible area is currently from (%d, %d) to (%d to %s), %s rows x %s cols.",
			visible.getMinimumRow(), visible.getMinimumColumn(), visible.getMaximumRow(),
			visible.getMaximumColumn(), visible.getHeight(), visible.getWidth()));
		int topRow;
		if (selection.getRow() - (visible.getHeight() / 2) <= 0) {
			LOGGER.finer("Close enough to the top to go flush to it");
			topRow = 0;
		} else if (selection.getRow() + (visible.getHeight() / 2) >= dimensions.getRows()) {
			LOGGER.finer("Close enough to the bottom to go flush to it");
			topRow = dimensions.getRows() - visible.getHeight(); // TODO: do we need an off-by-one adjustment?
		} else {
			topRow = selection.getRow() - (visible.getHeight() / 2);
			LOGGER.finer("Setting top row to " + topRow);
		}
		int leftColumn;
		if (selection.getColumn() - (visible.getWidth() / 2) <= 0) {
			LOGGER.finer("Close enough to left edge to go flush to it");
			leftColumn = 0;
		} else if (selection.getColumn() + (visible.getWidth() / 2) >= dimensions.getColumns()) {
			LOGGER.finer("Close enough to right edge to go flush to it");
			leftColumn = dimensions.getColumns() - visible.getWidth();
		} else {
			leftColumn = selection.getColumn() - (visible.getWidth() / 2);
			LOGGER.finer("Setting left column to " + leftColumn);
		}
		// Original Java version had topRow + dimensions.rows and
		// leftColumn + dimensions.columns as max row and column; this seems
		// plainly wrong.
		model.setVisibleDimensions(new VisibleDimensions(topRow,
			topRow + visible.getHeight(), leftColumn, leftColumn + visible.getWidth()));
	}

	@Override
	public void zoomIn() {
		model.zoomIn();
	}

	@Override
	public void zoomOut() {
		model.zoomOut();
	}

	@Override
	public void resetZoom() {
		model.resetZoom();
	}

	@Nullable
	private FindDialog finder = null;

	private FindDialog getFindDialog(final ViewerFrame parent) {
		FindDialog temp = finder;
		if (temp != null) {
			return temp;
		} else {
			FindDialog local = new FindDialog(parent, model);
			finder = local;
			return local;
		}
	}

	private ViewerGUI factory(final IDriverModel model) {
		return new ViewerGUI(new ViewerModel(model), options.copy(), cli);
	}

	private void createWindow(final MenuBroker menuHandler) {
		ViewerFrame frame = new ViewerFrame(model, menuHandler, this, this::factory);
		String backgroundFile = options.getArgument("--background");
		if (!backgroundFile.isEmpty() && !"false".equals(backgroundFile)) {
			try {
				frame.setBackgroundImage(ImageIO.read(new File(backgroundFile)));
			} catch (FileNotFoundException|NoSuchFileException except) {
				LOGGER.severe("Background image file not found");
				LOGGER.log(Level.FINE, "Stack trace for background missing", except);
			} catch (final IOException except) {
				LOGGER.log(Level.SEVERE, "I/O error reading background image", except);
			}
		}
		frame.addWindowListener(new WindowCloseListener(menuHandler));
		SelectTileDialog selectTileDialogInstance = new SelectTileDialog(frame, model);
		menuHandler.registerWindowShower(selectTileDialogInstance, "go to tile");
		selectTileDialogInstance.dispose();
		menuHandler.registerWindowShower(() -> getFindDialog(frame), "find a fixture");
		menuHandler.register(ignored -> getFindDialog(frame).search(), "find next");
		try {
			menuHandler.registerWindowShower(new AboutDialog(frame, frame.getWindowName()),
				"about");
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O error loading About dialog contents", except);
		}
		// TODO: We'd like to have the starting position stored in the map
		// file, in system preferences, or some such, or simply default to HQ.
		if (options.hasOption("--starting-row") && options.hasOption("--starting-column")) {
			int startingRow;
			int startingCol;
			try {
				startingRow = Integer.parseInt(options.getArgument("--starting-row"));
				startingCol = Integer.parseInt(options.getArgument("--starting-column"));
				Point starting = new Point(startingRow, startingCol);
				if (model.getMapDimensions().contains(starting)) {
					model.setSelection(starting);
				} else {
					LOGGER.warning(
						"Starting coordinates must be within the map's dimensions");
				}
			} catch (final NumberFormatException except) {
				LOGGER.warning(
					"Arguments to --starting-row and --starting-column must be numeric");
			}
		} else if (options.hasOption("--starting-row")) {
			LOGGER.warning("--starting-row requires --starting-column");
		} else if (options.hasOption("--starting-column")) {
			LOGGER.warning("--starting-column requires --starting-row");
		}
		LOGGER.finer("About to show viewer GUI window");
		frame.showWindow();
	}

	/* package */ static final ViewerGUI createDriver(final ICLIHelper cli, final SPOptions options,
	                                                  final IDriverModel model) {
		if (model instanceof IViewerModel) {
			LOGGER.finer("Creating a viewer-GUI instance for a model of the proper type");
				return new ViewerGUI((IViewerModel) model, options, cli);
		} else {
			LOGGER.finer("Creating a viewer-GUI instance after converting its type");
			return createDriver(cli, options, new ViewerModel(model));
		}
	}

	@Override
	public void startDriver() {
		LOGGER.finer("In ViewerGUI.startDriver()");
		MenuBroker menuHandler = new MenuBroker();
		menuHandler.register(new IOHandler(this, ViewerGUI::createDriver, cli), "load", "save",
			"save as", "new", "load secondary", "save all", "open in map viewer",
			"open secondary map in map viewer", "close", "quit");
		menuHandler.register(ignored -> zoomIn(), "zoom in");
		menuHandler.register(ignored -> zoomOut(), "zoom out");
		menuHandler.register(ignored -> resetZoom(), "reset zoom");
		menuHandler.register(ignored -> center(), "center");
		SwingUtilities.invokeLater(() -> createWindow(menuHandler));
	}

	/**
	 * Ask the user to choose a file or files.
	 */
	@Override
	public Iterable<Path> askUserForFiles() {
		try {
			return SPFileChooser.open((Path) null).getFiles();
		} catch (final FileChooser.ChoiceInterruptedException except) {
			LOGGER.log(Level.WARNING, "Choice interrupted or user failed to choose", except);
			return Collections.emptyList();
		}
	}

	@Override
	public void open(final IMutableMapNG map) {
		if (model.isMapModified()) {
			SwingUtilities.invokeLater(() -> new ViewerGUI(new ViewerModel(map),
				options.copy(), cli).startDriver());
		} else {
			model.setMap(map);
		}
	}
}
