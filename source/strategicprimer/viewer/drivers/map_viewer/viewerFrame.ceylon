import com.pump.window {
    WindowMenu
}

import java.awt {
    Dimension,
    Component
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
    SwingUtilities
}
import javax.swing.event {
    TableModelEvent
}
import javax.swing.table {
    AbstractTableModel,
    TableColumn
}

import lovelace.util.common {
    todo,
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
    readMapModel
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
"An interface for the map viewer main window, to hold the method needed by the worker
 management app."
todo("Merge into ISPWindow (with a broader return type)?")
shared interface IViewerFrame {
    shared formal IViewerModel model;
}
"The main window for the map viewer app."
shared SPFrame&IViewerFrame viewerFrame(IViewerModel driverModel,
        Anything(ActionEvent) menuHandler) {
    object retval extends SPFrame("Map Viewer", driverModel.mapFile)
            satisfies IViewerFrame {
        shared actual IViewerModel model = driverModel;
        shared actual String windowName = "Map Viewer";
        // TODO: Keep track of whether the map has been modified and if not replace it
        // instead of opening a new window
        shared actual void acceptDroppedFile(JPath file) =>
                SwingUtilities.invokeLater(() =>
                    viewerFrame(ViewerModel.copyConstructor(
                        readMapModel(file, warningLevels.default)),
                        menuHandler).setVisible(true));
        shared actual Boolean supportsDroppedFiles = true;
    }
    AbstractTableModel&{FixtureMatcher*}&ZOrderFilter&Comparator<TileFixture> tableModel =
            fixtureFilterTableModel();
    JComponent&MapGUI&MapChangeListener&SelectionChangeListener&GraphicalParamsListener
    mapPanel = mapComponent(driverModel, tableModel.shouldDisplay, tableModel);
    tableModel.addTableModelListener((TableModelEvent event) => mapPanel.repaint());
    driverModel.addGraphicalParamsListener(mapPanel);
    driverModel.addMapChangeListener(mapPanel);
    driverModel.addSelectionChangeListener(mapPanel);
    JComponent&SelectionChangeListener&VersionChangeListener detailPane =
            detailPanel(driverModel.mapDimensions.version, driverModel);
    driverModel.addVersionChangeListener(detailPane);
    driverModel.addSelectionChangeListener(detailPane);
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
    retval.contentPane = verticalSplit(0.9, 0.9, horizontalSplit(0.95, 0.95,
        mapScrollPanel(driverModel, mapPanel), createFilterPanel()), detailPane);
    (retval of Component).preferredSize = Dimension(800, 600);
    retval.setSize(800, 600);
    retval.setMinimumSize(Dimension(800, 600));
    retval.pack();
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
    retval.addWindowListener(windowSizeListener);
    retval.addWindowStateListener(windowSizeListener);
    retval.jMenuBar = SPMenu(SPMenu.createFileMenu(menuHandler, driverModel),
        SPMenu.createMapMenu(menuHandler, driverModel),
        SPMenu.createViewMenu(menuHandler, driverModel), WindowMenu(retval));
    return retval;
}
