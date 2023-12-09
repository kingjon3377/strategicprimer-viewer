package drivers.map_viewer;

import drivers.common.DriverFailedException;
import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.io.IOException;

import javax.swing.SwingUtilities;

import drivers.common.IDriverModel;
import drivers.common.SPOptions;
import drivers.common.ViewerDriver;
import drivers.common.cli.ICLIHelper;
import legacy.map.MapDimensions;
import legacy.map.Point;
import legacy.map.IMutableMapNG;
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
    private final ICLIHelper cli;

    public ViewerGUI(final IViewerModel model, final SPOptions options, final ICLIHelper cli) {
        LovelaceLogger.trace("In ViewerGUI constructor");
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
        final Point selection = model.getSelection();
        LovelaceLogger.trace("Asked to center on %s", selection);
        final MapDimensions dimensions = model.getMapDimensions();
        final VisibleDimensions visible = model.getVisibleDimensions();
        LovelaceLogger.trace(
                "Visible area is currently from (%d, %d) to (%d to %d), %d rows x %d cols.",
                visible.getMinimumRow(), visible.getMinimumColumn(), visible.getMaximumRow(),
                visible.getMaximumColumn(), visible.getHeight(), visible.getWidth());
        final int topRow;
        if (selection.row() - (visible.getHeight() / 2) <= 0) {
            LovelaceLogger.trace("Close enough to the top to go flush to it");
            topRow = 0;
        } else if (selection.row() + (visible.getHeight() / 2) >= dimensions.rows()) {
            LovelaceLogger.trace("Close enough to the bottom to go flush to it");
            topRow = dimensions.rows() - visible.getHeight(); // TODO: do we need an off-by-one adjustment?
        } else {
            topRow = selection.row() - (visible.getHeight() / 2);
            LovelaceLogger.trace("Setting top row to %d", topRow);
        }
        final int leftColumn;
        if (selection.column() - (visible.getWidth() / 2) <= 0) {
            LovelaceLogger.trace("Close enough to left edge to go flush to it");
            leftColumn = 0;
        } else if (selection.column() + (visible.getWidth() / 2) >= dimensions.columns()) {
            LovelaceLogger.trace("Close enough to right edge to go flush to it");
            leftColumn = dimensions.columns() - visible.getWidth();
        } else {
            leftColumn = selection.column() - (visible.getWidth() / 2);
            LovelaceLogger.trace("Setting left column to %d", leftColumn);
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

    private @Nullable FindDialog finder = null;

    private FindDialog getFindDialog(final ViewerFrame parent) {
        final FindDialog temp = finder;
        if (temp == null) {
            final FindDialog local = new FindDialog(parent, model);
            finder = local;
            return local;
        } else {
            return temp;
        }
    }

    private ViewerGUI factory(final IDriverModel model) {
        return new ViewerGUI(new ViewerModel(model), options.copy(), cli);
    }

    private void createWindow(final MenuBroker menuHandler) {
        final ViewerFrame frame = new ViewerFrame(model, menuHandler, this, this::factory);
        final String backgroundFile = options.getArgument("--background");
        if (!backgroundFile.isEmpty() && !"false".equals(backgroundFile)) {
            try {
                frame.setBackgroundImage(ImageIO.read(new File(backgroundFile)));
            } catch (final FileNotFoundException | NoSuchFileException except) {
                LovelaceLogger.error("Background image file not found");
                LovelaceLogger.debug(except, "Stack trace for background missing");
            } catch (final IOException except) {
                LovelaceLogger.error(except, "I/O error reading background image");
            }
        }
        frame.addWindowListener(new WindowCloseListener(menuHandler));
        final SelectTileDialog selectTileDialogInstance = new SelectTileDialog(frame, model);
        menuHandler.registerWindowShower(selectTileDialogInstance, "go to tile");
        selectTileDialogInstance.dispose();
        menuHandler.registerWindowShower(() -> getFindDialog(frame), "find a fixture");
        menuHandler.register(ignored -> getFindDialog(frame).search(), "find next");
        try {
            menuHandler.registerWindowShower(new AboutDialog(frame, frame.getWindowName()),
                    "about");
        } catch (final IOException except) {
            LovelaceLogger.error(except, "I/O error loading About dialog contents");
        }
        // TODO: We'd like to have the starting position stored in the map
        // file, in system preferences, or some such, or simply default to HQ.
        if (options.hasOption("--starting-row") && options.hasOption("--starting-column")) {
            final int startingRow;
            final int startingCol;
            try {
                startingRow = Integer.parseInt(options.getArgument("--starting-row"));
                startingCol = Integer.parseInt(options.getArgument("--starting-column"));
                final Point starting = new Point(startingRow, startingCol);
                if (model.getMapDimensions().contains(starting)) {
                    model.setSelection(starting);
                } else {
                    LovelaceLogger.warning(
                            "Starting coordinates must be within the map's dimensions");
                }
            } catch (final NumberFormatException except) {
                LovelaceLogger.warning(
                        "Arguments to --starting-row and --starting-column must be numeric");
            }
        } else if (options.hasOption("--starting-row")) {
            LovelaceLogger.warning("--starting-row requires --starting-column");
        } else if (options.hasOption("--starting-column")) {
            LovelaceLogger.warning("--starting-column requires --starting-row");
        }
        LovelaceLogger.trace("About to show viewer GUI window");
        frame.showWindow();
    }

    /* package */
    static ViewerGUI createDriver(final ICLIHelper cli, final SPOptions options,
                                  final IDriverModel model) {
        if (model instanceof final IViewerModel vm) {
            LovelaceLogger.trace("Creating a viewer-GUI instance for a model of the proper type");
            return new ViewerGUI(vm, options, cli);
        } else {
            LovelaceLogger.trace("Creating a viewer-GUI instance after converting its type");
            return createDriver(cli, options, new ViewerModel(model));
        }
    }

    @Override
    public void startDriver() {
        LovelaceLogger.trace("In ViewerGUI.startDriver()");
        final MenuBroker menuHandler = new MenuBroker();
        menuHandler.register(new IOHandler(this, cli), "load", "save",
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
    public Iterable<Path> askUserForFiles() throws DriverFailedException {
        try {
            return SPFileChooser.open((Path) null).getFiles();
        } catch (final FileChooser.ChoiceInterruptedException except) {
            throw new DriverFailedException(except, "Choice interrupted or user didn't choose");
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
