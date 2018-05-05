import com.pump.window {
    WindowMenu
}

import java.awt {
    Dimension,
    Component,
	Container
}
import java.awt.event {
    WindowEvent,
    WindowAdapter,
    ActionEvent
}
import java.nio.file {
    JPath=Path
}

import javax.swing {
    JPanel,
    JScrollPane,
    JComponent,
    ListSelectionModel,
    DropMode,
    JButton,
    JLabel,
    JTable,
    SwingUtilities,
	JFrame
}
import javax.swing.event {
    TableModelEvent
}
import javax.swing.table {
    AbstractTableModel,
    TableColumn
}

import lovelace.util.common {
    Comparator
}
import lovelace.util.jvm {
    centeredHorizontalBox,
    platform,
    listenedButton,
    horizontalSplit,
    BorderedPanel,
    verticalSplit
}

import strategicprimer.drivers.common {
    VersionChangeListener,
    MapChangeListener,
    SelectionChangeListener,
    mapReaderAdapter,
	FixtureMatcher
}
import strategicprimer.model.map {
    TileFixture
}
import strategicprimer.viewer.drivers {
    SPMenu
}
import strategicprimer.model.xmlio {
    warningLevels
}
import strategicprimer.drivers.gui.common {
    SPFrame
}
"The main window for the map viewer app."
shared final class ViewerFrame extends SPFrame satisfies MapGUI {
	static JFrame containingWindow(Component component) {
		if (is JFrame component) {
			return component;
		} else {
			assert (exists parent = component.parent);
			return containingWindow(parent);
		}
	}
	AbstractTableModel&{FixtureMatcher*}&ZOrderFilter&Comparator<TileFixture> tableModel =
			FixtureFilterTableModel();
	shared actual IViewerModel mapModel;
	shared actual String windowName = "Map Viewer";
	Anything(ActionEvent) menuHandler;
	shared new(IViewerModel model, Anything(ActionEvent) menuListener)
			extends SPFrame("Map Viewer", model.mapFile) {
		mapModel = model;
		menuHandler = menuListener;
	}
	// TODO: Keep track of whether the map has been modified and if not replace it
	// instead of opening a new window
	shared actual void acceptDroppedFile(JPath file) =>
			SwingUtilities.invokeLater(() =>
				ViewerFrame(ViewerModel.copyConstructor(
					mapReaderAdapter.readMapModel(file, warningLevels.default)),
						menuHandler).setVisible(true));
	shared actual Boolean supportsDroppedFiles = true;
	JComponent&MapGUI&MapChangeListener&SelectionChangeListener&GraphicalParamsListener
		mapPanel = mapComponent(mapModel, tableModel.shouldDisplay, tableModel);
	tableModel.addTableModelListener((TableModelEvent event) => mapPanel.repaint()); // can't use silentListener because repaint() is overloaded
	mapModel.addGraphicalParamsListener(mapPanel);
	mapModel.addMapChangeListener(mapPanel);
	mapModel.addSelectionChangeListener(mapPanel);
	JComponent&SelectionChangeListener&VersionChangeListener detailPane =
			detailPanel(mapModel.mapDimensions.version, mapModel);
	mapModel.addVersionChangeListener(detailPane);
	mapModel.addSelectionChangeListener(detailPane);
	JComponent createFilterPanel() { // TODO: This probably creates a separate class ...
		JTable table = JTable(tableModel);
		table.dragEnabled = true;
		table.dropMode = DropMode.insertRows;
		table.transferHandler = fixtureFilterTransferHandler;
		table.setSelectionMode(ListSelectionModel.singleSelection);
		TableColumn firstColumn = table.columnModel.getColumn(0);
		firstColumn.minWidth = 30;
		firstColumn.maxWidth = 50;
		table.preferredScrollableViewportSize = table.preferredSize;
		table.fillsViewportHeight = true;
		table.autoResizeMode = JTable.autoResizeLastColumn;
		JButton allButton = listenedButton("Display All", (ActionEvent event) {
			for (matcher in tableModel) {
				matcher.displayed = true;
			}
			tableModel.fireTableRowsUpdated(0, tableModel.rowCount);
		});
		JButton noneButton = listenedButton("Display None", (ActionEvent event) {
			for (matcher in tableModel) {
				matcher.displayed = false;
			}
			tableModel.fireTableRowsUpdated(0, tableModel.rowCount);
		});
		platform.makeButtonsSegmented(allButton, noneButton);
		JPanel buttonPanel = (platform.systemIsMac) then
		centeredHorizontalBox(allButton, noneButton)
		else BorderedPanel.horizontalPanel(allButton, null, noneButton);
		return BorderedPanel.verticalPanel(JLabel("Display ..."), JScrollPane(table),
			buttonPanel);
	}
	contentPane = verticalSplit(0.9, 0.9, horizontalSplit(0.95, 0.95,
		mapScrollPanel(mapModel, mapPanel), createFilterPanel()), detailPane);
	(super of Container).preferredSize = Dimension(800, 600);
	setSize(800, 600);
	setMinimumSize(Dimension(800, 600));
	pack();
	mapPanel.requestFocusInWindow();
	"When the window is maximized, restored, or de-iconified, force the listener that is
	 listening for *resize* events to adjust the number of tiles displayed properly."
	object windowSizeListener extends WindowAdapter() {
		"Whether we should add or subtract 1 to force recalculation this time."
		variable Boolean add = false;
		void recalculate() {
			Integer addend = (add) then 1 else -1;
			add = !add;
			mapPanel.setSize(mapPanel.width + addend, mapPanel.height + addend);
		}
		shared actual void windowDeiconified(WindowEvent event) => recalculate();
		shared actual void windowStateChanged(WindowEvent event) => recalculate();
	}
	addWindowListener(windowSizeListener);
	addWindowStateListener(windowSizeListener);
	jMenuBar = SPMenu(SPMenu.createFileMenu(menuHandler, mapModel),
		SPMenu.createMapMenu(menuHandler, mapModel),
		SPMenu.createViewMenu(menuHandler, mapModel),
		WindowMenu(containingWindow(mapPanel)));
}
