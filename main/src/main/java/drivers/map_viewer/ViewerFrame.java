package drivers.map_viewer;

import java.io.Serial;
import java.util.function.Function;

import drivers.common.ViewerDriver;
import drivers.common.IDriverModel;
import drivers.common.DriverFailedException;
import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;

import javax.swing.event.TableModelEvent;
import javax.swing.table.TableColumn;

import java.nio.file.Path;

import static lovelace.util.BoxPanel.centeredHorizontalBox;

import lovelace.util.Platform;
import lovelace.util.ListenedButton;

import static lovelace.util.FunctionalSplitPane.horizontalSplit;

import lovelace.util.BorderedPanel;

import static lovelace.util.FunctionalSplitPane.verticalSplit;

import drivers.common.FixtureMatcher;
import legacy.map.IMutableLegacyMap;
import drivers.MapReaderAdapter;
import common.xmlio.Warning;

import drivers.gui.common.SPFrame;
import drivers.gui.common.SPMenu;

import java.awt.image.BufferedImage;

/**
 * The main window for the map viewer app.
 */
public final class ViewerFrame extends SPFrame implements MapGUI {
	@Serial
	private static final long serialVersionUID = 1L;

	private static JFrame containingWindow(final Component component) {
		if (component instanceof final JFrame f) {
			return f;
		} else {
			return containingWindow(component.getParent());
		}
	}

	private final FixtureFilterTableModel tableModel = new FixtureFilterTableModel();

	private final IViewerModel mapModel;

	@Override
	public IViewerModel getMapModel() {
		return mapModel;
	}

	@Override
	public String getWindowName() {
		return "Map Viewer";
	}

	private final Function<IDriverModel, ViewerDriver> driverFactory;

	public ViewerFrame(final IViewerModel model, final ActionListener menuListener, final ViewerDriver driver,
					   final Function<IDriverModel, ViewerDriver> driverFactory) {
		super("Map Viewer", driver);
		mapModel = model;
		this.driverFactory = driverFactory;
		mapPanel = new MapComponent(mapModel, tableModel::shouldDisplay, tableModel);

		tableModel.addTableModelListener(this::repaintMapPanel); // TODO: inline into here?
		mapModel.addGraphicalParamsListener(mapPanel);
		mapModel.addMapChangeListener(mapPanel);
		mapModel.addSelectionChangeListener(mapPanel);

		final DetailPanel detailPane = new DetailPanel(mapModel.getMapDimensions().version(), mapModel,
				tableModel);
		mapModel.addVersionChangeListener(detailPane);
		mapModel.addSelectionChangeListener(detailPane);

		setContentPane(verticalSplit(horizontalSplit(new MapScrollPanel(mapModel, mapPanel),
				createFilterPanel(), 0.95), detailPane, 0.9));
		setPreferredSize(new Dimension(800, 600));
		setSize(800, 600);
		setMinimumSize(new Dimension(800, 600));
		pack();

		mapPanel.requestFocusInWindow();

		final WindowSizeListener windowSizeListener = new WindowSizeListener(mapPanel);
		addWindowListener(windowSizeListener);
		addWindowStateListener(windowSizeListener);

		setJMenuBar(SPMenu.forWindowContaining(mapPanel, SPMenu.createFileMenu(menuListener, driver),
				SPMenu.createMapMenu(menuListener, driver),
				SPMenu.createViewMenu(menuListener, driver)));
	}

	private void acceptDroppedFileImpl(final Path file) {
		final IDriverModel map;
		try {
			map = MapReaderAdapter.readMapModel(file, Warning.getDefaultHandler());
		} catch (final DriverFailedException except) {
			// FIXME: Show error dialog, depending on what the error was
			LovelaceLogger.error(except.getCause(), "Driver failed");
			return;
		}
		SwingUtilities.invokeLater(() -> {
			try {
				driverFactory.apply(map).startDriver();
			} catch (final DriverFailedException except) {
				// FIXME: Show error dialog, depending on what the error was
				LovelaceLogger.error(except.getCause(), "Driver failed");
				return; // FIXME: redundant
			}
		});
	}

	private void alternateAcceptDroppedFile(final Path file) {
		try {
			final IMutableLegacyMap mapOrError = MapReaderAdapter.readMap(file,
					Warning.getDefaultHandler());
			SwingUtilities.invokeLater(() -> mapModel.setMap(mapOrError));
		} catch (final DriverFailedException except) {
			// FIXME: handle error, showing it to the user or something
		}
	}

	// TODO: Eventually, It Would Be Nice to have some sort of progress bar
	// or something; the threads here prevent loading from completely
	// freezing the UI, but it still can look like nothing's happened.
	@Override
	public void acceptDroppedFile(final Path file) {
		if (mapModel.isMapModified()) {
			new Thread(() -> acceptDroppedFileImpl(file)).start();
		} else {
			new Thread(() -> alternateAcceptDroppedFile(file)).start();
		}
	}

	@Override
	public boolean supportsDroppedFiles() {
		return true;
	}

	private final MapComponent mapPanel;

	public @Nullable BufferedImage getBackgroundImage() {
		return mapPanel.getBackgroundImage();
	}

	public void setBackgroundImage(final BufferedImage backgroundImage) {
		mapPanel.setBackgroundImage(backgroundImage);
	}

	private void repaintMapPanel(final TableModelEvent event) {
		mapPanel.repaint();
	}

	private void displayAllListener() {
		for (final FixtureMatcher matcher : tableModel) {
			matcher.setDisplayed(true);
		}
		tableModel.fireTableRowsUpdated(0, tableModel.getRowCount());
	}

	private void displayNoneListener() {
		for (final FixtureMatcher matcher : tableModel) {
			matcher.setDisplayed(false);
		}
		tableModel.fireTableRowsUpdated(0, tableModel.getRowCount());
	}

	private JComponent createFilterPanel() { // TODO: Extract to class, maybe?
		final JTable table = new JTable(tableModel);
		table.setDragEnabled(true);
		table.setDropMode(DropMode.INSERT_ROWS);
		table.setTransferHandler(new FixtureFilterTransferHandler());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final TableColumn firstColumn = table.getColumnModel().getColumn(0);
		firstColumn.setMinWidth(30);
		firstColumn.setMaxWidth(50);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		final JButton allButton = new ListenedButton("Display All", this::displayAllListener);
		final JButton noneButton = new ListenedButton("Display None", this::displayNoneListener);
		Platform.makeButtonsSegmented(allButton, noneButton);
		final JPanel buttonPanel = (Platform.SYSTEM_IS_MAC) ?
				centeredHorizontalBox(allButton, noneButton) :
				BorderedPanel.horizontalPanel(allButton, null, noneButton);
		return BorderedPanel.verticalPanel(new JLabel("Display ..."), new JScrollPane(table),
				buttonPanel);
	}

	/**
	 * When the window is maximized, restored, or de-iconified, force the
	 * listener that is listening for <em>resize</em> events to adjust the number
	 * of tiles displayed properly.
	 */
	private static class WindowSizeListener extends WindowAdapter {
		private final JComponent mapPanel;

		public WindowSizeListener(final JComponent mapPanel) {
			this.mapPanel = mapPanel;
		}

		/**
		 * Whether we should add or subtract 1 to force recalculation this time.
		 */
		private boolean add = false;

		private void recalculate() {
			final int addend = (add) ? 1 : -1;
			add = !add;
			mapPanel.setSize(mapPanel.getWidth() + addend, mapPanel.getHeight() + addend);
		}

		@Override
		public void windowDeiconified(final WindowEvent event) {
			recalculate();
		}

		@Override
		public void windowStateChanged(final WindowEvent event) {
			recalculate();
		}
	}

}
