import com.bric.window {
    WindowMenu
}
import model.viewer {
    IViewerModel,
    ZOrderFilter
}
import lovelace.util.common {
    todo,
    Comparator
}
import java.awt {
    Dimension,
    Component
}
import model.map {
    TileFixture
}
import javax.swing.table {
    AbstractTableModel,
    TableColumn
}
import strategicprimer.viewer.drivers {
    SPFrame,
    SPMenu
}
import javax.swing {
    JPanel,
    JScrollPane,
    JComponent,
    ListSelectionModel,
    DropMode,
    JButton,
    JLabel,
    JTable
}
import javax.swing.event {
    TableModelEvent
}
import java.awt.event {
    WindowEvent,
    WindowAdapter,
    ActionEvent
}
import model.listeners {
    VersionChangeListener,
    MapChangeListener,
    GraphicalParamsListener,
    SelectionChangeListener
}
import lovelace.util.jvm {
    centeredHorizontalBox,
    platform,
    listenedButton,
    horizontalSplit,
    BorderedPanel,
    verticalSplit
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
    object retval extends SPFrame("Map Viewer", driverModel.mapFile.orElse(null))
            satisfies IViewerFrame {
        shared actual IViewerModel model = driverModel;
        shared actual String windowName = "Map Viewer";
    }
    AbstractTableModel&{FixtureMatcher*}&ZOrderFilter&Comparator<TileFixture> tableModel = fixtureFilterTableModel();
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
    JComponent createFilterPanel() {
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
    SPMenu menu = SPMenu();
    menu.add(menu.createFileMenu(menuHandler, driverModel));
    menu.add(menu.createMapMenu(menuHandler, driverModel));
    menu.add(menu.createViewMenu(menuHandler, driverModel));
    menu.add(WindowMenu(retval));
    retval.jMenuBar = menu;
    return retval;
}
