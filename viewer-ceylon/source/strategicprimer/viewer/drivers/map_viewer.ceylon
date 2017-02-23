import controller.map.misc {
    ICLIHelper,
    MenuBroker,
    WindowCloser
}
import model.misc {
    IDriverModel,
    IMultiMapModel
}
import model.viewer {
    IViewerModel,
    ViewerModel,
    PointIterator,
    FixtureFilterTableModel
}
import view.map.main {
    ZoomListener,
    FixtureFilterList,
    MapComponent,
    ScrollListener,
    MapWindowSizeListener,
    FixtureFilterTransferHandler,
    ViewerMenu
}
import javax.swing {
    SwingUtilities,
    JTextField,
    JCheckBox,
    JPanel,
    ScrollPaneConstants,
    JLabel,
    JScrollPane,
    DropMode,
    JTable,
    ListSelectionModel,
    JButton,
    JComponent
}
import strategicprimer.viewer.about {
    aboutDialog
}
import java.awt {
    Frame,
    Component,
    Dimension,
    Window
}
import java.awt.event {
    ActionEvent,
    WindowAdapter
}
import view.util {
    SPDialog,
    BoxPanel,
    ListenedButton,
    SplitWithWeights,
    BorderedPanel,
    SPFrame
}
import util {
    OnMac
}
import java.lang {
    JIterable = Iterable
}
import model.map {
    Point,
    IFixture,
    FixtureIterable,
    TileFixture,
    HasName,
    HasKind,
    HasOwner,
    Player,
    MapDimensions,
    PointFactory
}
import java.util.stream {
    Stream
}
import ceylon.interop.java {
    CeylonIterable
}
import model.map.fixtures {
    RiverFixture
}
import java.text {
    NumberFormat
}
import javax.swing.event {
    TableModelEvent
}
import javax.swing.table {
    TableColumn
}
import view.map.details {
    DetailPanelNG
}
import lovelace.util.common {
    todo
}
"""A dialog to let the user find fixtures by ID, name, or "kind"."""
class FindDialog(Frame parent, IViewerModel model) extends SPDialog(parent, "Find") {
    JTextField searchField = JTextField("", 20);
    JCheckBox backwards = JCheckBox("Search backwards");
    JCheckBox vertically = JCheckBox("Search vertically then horizontally");
    JCheckBox caseSensitive = JCheckBox("Case-sensitive search");
    FixtureFilterList filterList = FixtureFilterList();
    "Whether the fixture has a name matching the given pattern."
    Boolean matchesName(String pattern, IFixture fixture, Boolean caseSensitivity) {
        if (is HasName fixture) {
            String name = (caseSensitivity) then fixture.name else
                fixture.name.lowercased;
            return name.contains(pattern);
        } else {
            return false;
        }
    }
    "Whether the fixture has a kind matching the given pattern."
    Boolean matchesKind(String pattern, IFixture fixture, Boolean caseSensitivity) {
        if (is HasKind fixture) {
            String kind = (caseSensitivity) then fixture.kind else
                fixture.kind.lowercased;
            return kind.contains(pattern);
        } else {
            return false;
        }
    }
    "Whether the fixture has an owner matching the given pattern."
    Boolean matchesOwner(String pattern, Integer? idNum, IFixture fixture,
    Boolean caseSensitivity) {
        if (is HasOwner fixture) {
            Player owner = fixture.owner;
            String ownerName = (caseSensitivity) then owner.name else
            owner.name.lowercased;
            if (exists idNum, owner.playerId == idNum || ownerName.contains(pattern)) {
                return true;
            } else if ("me".equals(pattern.lowercased), owner.current) {
                return true;
            } else if ({"none", "independent"}.any(pattern.lowercased.equals),
                owner.independent) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    "Whether the fixture matches the pattern in any of our simple ways."
    Boolean matchesSimple(String pattern, Integer? idNum, IFixture fixture,
            Boolean caseSensitivity) {
        if (pattern.empty) {
            return false;
        } else if (is TileFixture fixture, !filterList.shouldDisplay(fixture)) {
            return false;
        } else if (exists idNum, idNum == fixture.id) {
            return true;
        } else if (matchesName(pattern, fixture, caseSensitivity) ||
                matchesKind(pattern, fixture, caseSensitivity) ||
                matchesOwner(pattern, idNum, fixture, caseSensitivity)) {
            return true;
        } else {
            return false;
        }
    }
    "Whether the given fixture matches the given pattern in any way we recognize."
    Boolean matches(String pattern, Integer? idNum, IFixture fixture,
            Boolean caseSensitivity) {
        if (matchesSimple(pattern, idNum, fixture, caseSensitivity)) {
            return true;
        } else if (is FixtureIterable<out Object> fixture) {
            for (member in fixture) {
                if (is IFixture member, matches(pattern, idNum, member, caseSensitivity)) {
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    "Search for the current pattern. If the pattern is found (as the ID of a fixture,
     or the name of a [[HasName]], or the kind of a [[HasKind]]), select the tile
     containing the thing found. If the pattern is the empty string, don't search."
    shared void search() {
        String pattern;
        Boolean caseSensitivity = caseSensitive.selected;
        if (caseSensitivity) {
            pattern = searchField.text.trimmed;
        } else {
            pattern = searchField.text.trimmed.lowercased;
        }
        if (pattern.empty) {
            return;
        }
        Integer? idNum;
        if (is Integer temp = Integer.parse(pattern)) {
            idNum = temp;
        } else {
            idNum = null;
        }
        Point? result = PointIterator(model.mapDimensions, model.selectedPoint,
            !backwards.selected, !vertically.selected).stream().filter(
            (point) => model.map.streamAllFixtures(point).anyMatch(
                (fixture) => matches(pattern, idNum, fixture, caseSensitivity)))
            .findFirst().orElse(null);
        if (exists result) {
            log.info("Found in point ``result``");
            model.setSelection(result);
        }
    }
    Anything(ActionEvent) okListener = (ActionEvent event) {
        search();
        setVisible(false);
        parent.requestFocus();
        dispose();
    };
    searchField.addActionListener(okListener);
    searchField.setActionCommand("OK");
    JPanel searchBoxPane = JPanel();
    searchBoxPane.add(searchField);
    JPanel contentPanel = BoxPanel(false);
    contentPanel.add(searchBoxPane);
    contentPanel.add(backwards);
    contentPanel.add(vertically);
    contentPanel.add(caseSensitive);
    BoxPanel buttonPanel = BoxPanel(true);
    buttonPanel.addGlue();
    ListenedButton okButton = ListenedButton("OK", okListener);
    ListenedButton cancelButton = ListenedButton("Cancel", (event) {
        setVisible(false);
        parent.requestFocus();
        dispose();
    });
    OnMac.makeButtonsSegmented(okButton, cancelButton);
    buttonPanel.add(okButton);
    if (OnMac.systemIsMac) {
        searchField.putClientProperty("JTextField.variant", "search");
        searchField.putClientProperty("JTextField.Search.FindAction", okListener);
        searchField.putClientProperty("JTextField.Search.CancelAction",
            (ActionEvent event) => searchField.text = "");
    } else {
        buttonPanel.addGlue();
    }
    buttonPanel.add(cancelButton);
    contentPanel.add(buttonPanel);
    SwingUtilities.invokeLater(() {
        void populate(Anything fixture) {
            if (is TileFixture fixture) {
                filterList.shouldDisplay(fixture);
            } else if (is JIterable<out Anything> fixture) {
                for (item in fixture) {
                    populate(item);
                }
            } else if (is Stream<out Anything> fixture) {
                fixture.forEach((Anything item) => populate(item));
            }
        }
        for (point in model.map.locations()) {
            if (!CeylonIterable(model.map.getRivers(point)).empty) {
                populate(RiverFixture());
            }
            populate(model.map.streamAllFixtures(point));
        }
    });
    contentPane = SplitWithWeights.horizontalSplit(0.6, 0.6, contentPanel,
        BorderedPanel.verticalPanel(JLabel("Find only ..."),
            JScrollPane(filterList, ScrollPaneConstants.verticalScrollbarAsNeeded,
                ScrollPaneConstants.horizontalScrollbarAsNeeded), null));
    pack();
}
class NumberState of valid|nonNumeric|negative|overflow {
    shared new valid { }
    shared new nonNumeric { }
    shared new negative { }
    shared new overflow { }
}
"A dialog to let the user select a tile by coordinates."
SPDialog selectTileDialog(Frame? parentFrame, IViewerModel model) {
    object retval extends SPDialog(parentFrame, "Go To ...") {}
    JLabel mainLabel = JLabel("Coordinates of tile to select:");
    mainLabel.alignmentX = Component.centerAlignment;
    mainLabel.alignmentY = Component.topAlignment;
    NumberFormat numParser = NumberFormat.integerInstance;
    JTextField rowField = JTextField("-1", 4);
    JTextField columnField = JTextField("-1", 4);
    JLabel errorLabel = JLabel(
        "This text should vanish from this label before it appears.");
    NumberState checkNumber(String text, Integer bound) {
        try {
            Integer number = numParser.parse(text).intValue();
            if (number < 0) {
                return NumberState.negative;
            } else if (number > bound) {
                return NumberState.overflow;
            } else {
                return NumberState.valid;
            }
        } catch (ParseException except) {
            log.debug("Non-numeric input", except);
            return NumberState.nonNumeric;
        }
    }
    String getErrorMessage(NumberState state, Integer bound) {
        switch (state)
        case (NumberState.valid) { return ""; }
        case (NumberState.nonNumeric) { return "must be a whole number. "; }
        case (NumberState.negative) { return "must be positive. "; }
        case (NumberState.overflow) { return "must be less than ``bound``."; }
    }
    void handleOK(ActionEvent ignored) {
        String rowText = rowField.text;
        String columnText = columnField.text;
        errorLabel.text = "";
        MapDimensions dimensions = model.mapDimensions;
        NumberState columnState = checkNumber(columnText, dimensions.columns - 1);
        if (columnState != NumberState.valid) {
            errorLabel.text += "Column ``getErrorMessage(columnState,
                dimensions.columns)``";
            columnField.text = "-1";
            columnField.selectAll();
        }
        NumberState rowState = checkNumber(rowText, dimensions.rows - 1);
        if (rowState != NumberState.valid) {
            errorLabel.text += "Row ``getErrorMessage(rowState, dimensions.rows)``";
            rowField.text = "-1";
            rowField.selectAll();
        }
        if (rowState == NumberState.valid, columnState == NumberState.valid) {
            try {
                model.setSelection(PointFactory.point(numParser.parse(rowText).intValue(),
                    numParser.parse(columnText).intValue()));
            } catch (ParseException except) {
                log.error("Parse failure after we checked input was numeric", except);
            }
            retval.setVisible(false);
            retval.dispose();
        } else {
            retval.pack();
        }
    }
    JPanel contentPane = BoxPanel(false);
    contentPane.add(mainLabel);
    BoxPanel boxPanel = BoxPanel(true);
    boxPanel.add(JLabel("Row: "));
    boxPanel.add(rowField);
    rowField.setActionCommand("OK");
    rowField.addActionListener(handleOK);
    boxPanel.addGlue();
    boxPanel.add(JLabel("Column:"));
    boxPanel.add(columnField);
    columnField.setActionCommand("OK");
    columnField.addActionListener(handleOK);
    boxPanel.addGlue();
    contentPane.add(boxPanel);
    contentPane.add(errorLabel);
    errorLabel.text = "";
    errorLabel.minimumSize = Dimension(200, 15);
    errorLabel.alignmentX = Component.centerAlignment;
    errorLabel.alignmentY = Component.topAlignment;
    BoxPanel buttonPanel = BoxPanel(true);
    buttonPanel.addGlue();
    ListenedButton okButton = ListenedButton("OK", handleOK);
    ListenedButton cancelButton = ListenedButton("Cancel", (ActionEvent event) {
        retval.setVisible(false);
        rowField.text = "-1";
        columnField.text = "-1";
        retval.dispose();
    });
    OnMac.makeButtonsSegmented(okButton, cancelButton);
    buttonPanel.add(okButton);
    if (!OnMac.systemIsMac) {
        buttonPanel.addGlue();
    }
    buttonPanel.add(cancelButton);
    buttonPanel.addGlue();
    contentPane.add(buttonPanel);
    retval.contentPane = contentPane;
    retval.pack();
    return retval;
}
"An interface for the map viewer main window, to hold the method needed by the worker
 management app."
todo("Merge into ISPWindow (with a broader return type)?")
interface IViewerFrame {
    shared formal IViewerModel model;
}
"The main window for the map viewer app."
SPFrame&IViewerFrame viewerFrame(IViewerModel driverModel, MenuBroker menuHandler) {
    object retval extends SPFrame("Map Viewer", driverModel.mapFile) satisfies IViewerFrame {
        shared actual IViewerModel model = driverModel;
        shared actual String windowName = "Map Viewer";
    }
    FixtureFilterTableModel tableModel = FixtureFilterTableModel();
    MapComponent mapPanel = MapComponent(driverModel, tableModel, tableModel);
    tableModel.addTableModelListener((TableModelEvent event) => mapPanel.repaint());
    driverModel.addGraphicalParamsListener(mapPanel);
    driverModel.addMapChangeListener(mapPanel);
    driverModel.addSelectionChangeListener(mapPanel);
    DetailPanelNG detailPanel = DetailPanelNG(driverModel.mapDimensions.version,
        driverModel);
    driverModel.addVersionChangeListener(detailPanel);
    driverModel.addSelectionChangeListener(detailPanel);
    JComponent createFilterPanel() {
        JTable table = JTable(tableModel);
        table.dragEnabled = true;
        table.dropMode = DropMode.insertRows;
        table.transferHandler = FixtureFilterTransferHandler();
        table.setSelectionMode(ListSelectionModel.singleSelection);
        TableColumn firstColumn = table.columnModel.getColumn(0);
        firstColumn.minWidth = 30;
        firstColumn.maxWidth = 50;
        table.preferredScrollableViewportSize = table.preferredSize;
        table.fillsViewportHeight = true;
        table.autoResizeMode = JTable.autoResizeLastColumn;
        JButton allButton = ListenedButton("Display All", (ActionEvent event) {
            for (matcher in tableModel) {
                matcher.displayed = true;
            }
            tableModel.fireTableRowsUpdated(0, tableModel.rowCount);
        });
        JButton noneButton = ListenedButton("Display None", (ActionEvent event) {
            for (matcher in tableModel) {
                matcher.displayed = false;
            }
            tableModel.fireTableRowsUpdated(0, tableModel.rowCount);
        });
        OnMac.makeButtonsSegmented(allButton, noneButton);
        JPanel buttonPanel = (OnMac.systemIsMac) then
            BoxPanel.centeredHorizBox(allButton, noneButton)
            else BorderedPanel.horizontalPanel(allButton, null, noneButton);
        return BorderedPanel.verticalPanel(JLabel("Display ..."), JScrollPane(table),
            buttonPanel);
    }
    retval.contentPane = SplitWithWeights.verticalSplit(0.9, 0.9,
        SplitWithWeights.horizontalSplit(0.95, 0.95,
            ScrollListener.mapScrollPanel(driverModel, mapPanel),
            createFilterPanel()), detailPanel);
    (retval of Component).preferredSize = Dimension(800, 600);
    retval.setSize(800, 600);
    retval.setMinimumSize(Dimension(800, 600));
    retval.pack();
    mapPanel.requestFocusInWindow();
    WindowAdapter windowSizeListener = MapWindowSizeListener(mapPanel);
    retval.addWindowListener(windowSizeListener);
    retval.addWindowStateListener(windowSizeListener);
    retval.jMenuBar = ViewerMenu(menuHandler, retval, driverModel);
    return retval;
}
"A driver to start the map viewer."
object viewerGUI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        shortOption = "-m";
        longOption = "--map";
        paramsWanted = ParamCount.one;
        shortDescription = "Map viewer";
        longDescription = "Look at the map visually. This is probably the app you want.";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IViewerModel model) {
            MenuBroker menuHandler = MenuBroker();
            menuHandler.register(IOHandler(model, options, cli), "load", "save",
                "save as", "new", "load secondary", "save all", "open in map viewer",
                "open secondary map in map viewer");
            menuHandler.register((event) => process.exit(0), "quit");
            menuHandler.register((event) => model.zoomIn(), "zoom in");
            menuHandler.register((event) => model.zoomOut(), "zoom out");
            menuHandler.register(ZoomListener(model), "center");
            SwingUtilities.invokeLater(() {
                SPFrame&IViewerFrame frame = viewerFrame(model, menuHandler);
                menuHandler.register(WindowCloser(frame), "close");
                menuHandler.register((event) =>
                    selectTileDialog(frame, model).setVisible(true), "go to tile");
                variable FindDialog? finder = null;
                FindDialog getFindDialog() {
                    if (exists temp = finder) {
                        return temp;
                    } else {
                        FindDialog local = FindDialog(frame, model);
                        finder = local;
                        return local;
                    }
                }
                menuHandler.register((event) => getFindDialog().setVisible(true),
                    "find a fixture");
                menuHandler.register((event) => getFindDialog().search(), "find next");
                menuHandler.register((event) =>
                    aboutDialog(frame, frame.windowName).setVisible(true), "about");
                frame.setVisible(true);
            });
        } else if (is IMultiMapModel model) {
            for (map in model.allMaps) {
                startDriverOnModel(cli, options.copy(), ViewerModel(map));
            }
        } else {
            startDriverOnModel(cli, options, ViewerModel(model.map, model.mapFile));
        }
    }
}