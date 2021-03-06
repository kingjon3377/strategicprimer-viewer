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
    Comparator,
    defer,
    PathWrapper
}
import lovelace.util.jvm {
    centeredHorizontalBox,
    platform,
    ListenedButton,
    horizontalSplit,
    BorderedPanel,
    verticalSplit
}

import strategicprimer.drivers.common {
    VersionChangeListener,
    SelectionChangeListener,
    FixtureMatcher,
    SPOptions
}
import strategicprimer.model.common.map {
    TileFixture,
    IMutableMapNG
}
import strategicprimer.viewer.drivers {
    mapReaderAdapter
}
import strategicprimer.model.common.xmlio {
    warningLevels
}

import strategicprimer.drivers.gui.common {
    SPFrame,
    SPMenu
}
import java.lang {
    JThread=Thread
}
import java.awt.image {
    BufferedImage
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
    SPOptions options;
    shared actual String windowName = "Map Viewer";
    Anything(ActionEvent) menuHandler;
    ViewerGUI driver;

    shared new(IViewerModel model, Anything(ActionEvent) menuListener, ViewerGUI driver, SPOptions options)
            extends SPFrame("Map Viewer", driver) {
        mapModel = model;
        menuHandler = menuListener;
        this.driver = driver;
        this.options = options;
    }

    void acceptDroppedFileImpl(PathWrapper file) {
        value map = mapReaderAdapter.readMapModel(file, warningLevels.default);
        SwingUtilities.invokeLater(defer(compose(ViewerGUI.startDriver, ViewerGUI),
            [ViewerModel.copyConstructor(map), options.copy()]));
    }

    void setMapWrapper(IMutableMapNG map) => mapModel.setMap(map); // TODO: Inline once this will compile; TODO: find MWE and report bug

    void alternateAcceptDroppedFile(PathWrapper file) {
        value mapOrError = mapReaderAdapter.readMap(file, warningLevels.default);
        if (is IMutableMapNG mapOrError) {
            SwingUtilities.invokeLater(defer(setMapWrapper, [mapOrError]));
        } else {
            // FIXME: handle error, showing it to the user or something
        }
    }

    shared actual void acceptDroppedFile(PathWrapper file) {
        if (mapModel.mapModified) {
            JThread(curry(acceptDroppedFileImpl)(file)).start();
        } else {
            JThread(curry(alternateAcceptDroppedFile)(file)).start();
        }
    }

    shared actual Boolean supportsDroppedFiles = true;

    MapComponent mapPanel = MapComponent(mapModel, tableModel.shouldDisplay, tableModel);

    shared BufferedImage? backgroundImage => mapPanel.backgroundImage;

    assign backgroundImage {
        mapPanel.backgroundImage = backgroundImage;
    }

    // can't use silentListener because repaint() is overloaded
    void repaintMapPanel(TableModelEvent event) => mapPanel.repaint();
    tableModel.addTableModelListener(repaintMapPanel);
    mapModel.addGraphicalParamsListener(mapPanel);
    mapModel.addMapChangeListener(mapPanel);
    mapModel.addSelectionChangeListener(mapPanel);

    JComponent&SelectionChangeListener&VersionChangeListener detailPane =
            detailPanel(mapModel.mapDimensions.version, mapModel, tableModel.compare);
    mapModel.addVersionChangeListener(detailPane);
    mapModel.addSelectionChangeListener(detailPane);

    void displayAllListener() {
        for (matcher in tableModel) {
            matcher.displayed = true;
        }
        tableModel.fireTableRowsUpdated(0, tableModel.rowCount);
    }

    void displayNoneListener() {
        for (matcher in tableModel) {
            matcher.displayed = false;
        }
        tableModel.fireTableRowsUpdated(0, tableModel.rowCount);
    }

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
        JButton allButton = ListenedButton("Display All", displayAllListener);
        JButton noneButton = ListenedButton("Display None", displayNoneListener);
        platform.makeButtonsSegmented(allButton, noneButton);
        JPanel buttonPanel = (platform.systemIsMac) then
        centeredHorizontalBox(allButton, noneButton)
        else BorderedPanel.horizontalPanel(allButton, null, noneButton);
        return BorderedPanel.verticalPanel(JLabel("Display ..."), JScrollPane(table),
            buttonPanel);
    }

    contentPane = verticalSplit(horizontalSplit(mapScrollPanel(mapModel, mapPanel),
        createFilterPanel(), 0.95), detailPane, 0.9);
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

    jMenuBar = SPMenu.forWindowContaining(mapPanel,
        SPMenu.createFileMenu(menuHandler, driver),
        SPMenu.createMapMenu(menuHandler, driver),
        SPMenu.createViewMenu(menuHandler, driver));
}
