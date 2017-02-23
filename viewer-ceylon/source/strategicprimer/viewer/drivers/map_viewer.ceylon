import controller.map.misc {
    ICLIHelper,
    MenuBroker,
    WindowCloser,
    IDFactoryFiller,
    IDRegistrar
}
import model.misc {
    IDriverModel,
    IMultiMapModel
}
import model.viewer {
    IViewerModel,
    ViewerModel,
    PointIterator,
    FixtureFilterTableModel,
    ZOrderFilter,
    TileViewSize,
    VisibleDimensions
}
import view.map.main {
    ZoomListener,
    FixtureFilterList,
    ScrollListener,
    MapWindowSizeListener,
    FixtureFilterTransferHandler,
    ViewerMenu,
    MapGUI,
    TileDrawHelper,
    TileDrawHelperFactory,
    DirectionSelectionChanger,
    ArrowKeyListener
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
    JComponent,
    JPopupMenu,
    JMenuItem,
    JFormattedTextField
}
import strategicprimer.viewer.about {
    aboutDialog
}
import java.awt {
    Frame,
    Component,
    Dimension,
    Window,
    Image,
    Graphics,
    Rectangle,
    Color
}
import java.awt.event {
    ActionEvent,
    WindowAdapter,
    MouseMotionAdapter,
    MouseEvent,
    ComponentEvent,
    MouseListener,
    MouseAdapter
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
    OnMac,
    IsNumeric
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
    PointFactory,
    IMapNG,
    TerrainFixture,
    TileType
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
import model.listeners {
    MapChangeListener,
    SelectionChangeListener,
    GraphicalParamsListener,
    SelectionChangeSource,
    VersionChangeListener,
    SelectionChangeSupport,
    NewUnitSource,
    PlayerChangeListener,
    NewUnitListener
}
import java.awt.image {
    ImageObserver
}
import ceylon.math.float {
    halfEven
}
import ceylon.collection {
    ArrayList,
    MutableList
}
import lovelace.util.jvm {
    ceylonComparator
}
import model.map.fixtures.mobile {
    IUnit,
    Unit
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
"A dialog to let the user add a new unit."
SPDialog&NewUnitSource&PlayerChangeListener newUnitDialog(variable Player player,
        IDRegistrar idf) {
    MutableList<NewUnitListener> listeners = ArrayList<NewUnitListener>();
    JTextField nameField = JTextField(10);
    JTextField kindField = JTextField(10);
    JFormattedTextField idField = JFormattedTextField(NumberFormat.integerInstance);
    object retval extends SPDialog(null, "Add a New Unit")
            satisfies NewUnitSource&PlayerChangeListener {
        shared actual void playerChanged(Player? old, Player newPlayer) =>
                player = newPlayer;
        shared actual void addNewUnitListener(NewUnitListener listener) =>
                listeners.add(listener);
        shared actual void removeNewUnitListener(NewUnitListener listener) =>
                listeners.remove(listener);
    }
    void okListener(ActionEvent event) {
        String name = nameField.text.trimmed;
        String kind = kindField.text.trimmed;
        if (name.empty) {
            nameField.requestFocusInWindow();
        } else if (kind.empty) {
            kindField.requestFocusInWindow();
        } else {
            String reqId = idField.text.trimmed;
            variable Integer idNum;
            if (IsNumeric.isNumeric(reqId)) {
                try {
                    idNum = NumberFormat.integerInstance.parse(reqId).intValue();
                    idf.register(idNum);
                } catch (ParseException except) {
                    log.info("Parse error parsing user-specified ID", except);
                    idNum = idf.createID();
                }
            } else {
                idNum = idf.createID();
            }
            IUnit unit = Unit(player, kind, name, idNum);
            for (listener in listeners) {
                listener.addNewUnit(unit);
            }
            kindField.text = "";
            nameField.text = "";
            retval.setVisible(false);
            retval.dispose();
        }
    }
    retval.add(JLabel("<html><b>Unit Name:&nbsp;</b></html>"));
    void setupField(JTextField field) {
        field.setActionCommand("OK");
        field.addActionListener(okListener);
        retval.add(field);
    }
    setupField(nameField);
    retval.add(JLabel("<html><b>Kind of Unit:&nbsp;</b></html>"));
    setupField(kindField);
    retval.add(JLabel("ID #: "));
    idField.columns = 10;
    setupField(idField);
    ListenedButton okButton = ListenedButton("OK", okListener);
    retval.add(okButton);
    ListenedButton cancelButton = ListenedButton("Cancel", (ActionEvent event) {
        nameField.text = "";
        kindField.text = "";
        retval.setVisible(false);
        retval.dispose();
    });
    OnMac.makeButtonsSegmented(okButton, cancelButton);
    retval.add(cancelButton);
    retval.setMinimumSize(Dimension(150, 80));
    (retval of Component).preferredSize = Dimension(200, 90);
    (retval of Component).maximumSize = Dimension(300, 90);
    retval.pack();
    return retval;
}
"A popup menu to let the user change a tile's terrain type, or add a unit."
JPopupMenu&VersionChangeListener&SelectionChangeSource&SelectionChangeListener
        terrainChangingMenu(Integer mapVersion, IViewerModel model) {
    SPDialog&NewUnitSource&PlayerChangeListener nuDialog =
            newUnitDialog(model.map.currentPlayer,
                IDFactoryFiller.createFactory(model.map));
    SelectionChangeSupport scs = SelectionChangeSupport();
    JMenuItem newUnitItem = JMenuItem("Add New Unit");
    variable Point point = PointFactory.invalidPoint;
    nuDialog.addNewUnitListener((IUnit unit) {
        model.map.addFixture(point, unit);
        model.setSelection(point);
        scs.fireChanges(null, point);
    });
    object retval extends JPopupMenu() satisfies VersionChangeListener&
            SelectionChangeListener&SelectionChangeSource {
        void updateForVersion(Integer version) {
            removeAll();
            for (type in TileType.valuesForVersion(version)) {
                JMenuItem item = JMenuItem(type.string);
                add(item);
                item.addActionListener((ActionEvent event) {
                    model.map.setBaseTerrain(point, type);
                    scs.fireChanges(null, point);
                });
            }
            addSeparator();
            add(newUnitItem);
        }
        shared actual void changeVersion(Integer old, Integer newVersion) =>
                updateForVersion(newVersion);
        shared actual void addSelectionChangeListener(SelectionChangeListener listener) =>
                scs.addSelectionChangeListener(listener);
        shared actual void removeSelectionChangeListener(SelectionChangeListener listener)
                => scs.removeSelectionChangeListener(listener);
        shared actual void selectedPointChanged(Point? old, Point newPoint) {
            point = newPoint;
            if (newPoint.valid,
                    TileType.notVisible != model.map.getBaseTerrain(newPoint)) {
                newUnitItem.enabled = true;
            } else {
                newUnitItem.enabled = false;
            }
        }
        updateForVersion(mapVersion);
    }
    newUnitItem.addActionListener((ActionEvent event) => nuDialog.setVisible(true));
    nuDialog.dispose();
    return retval;
}
"An interface for the method to get the tool-tip message for the location the mouse
 cursor is over."
interface ToolTipSource {
    shared formal String? getToolTipText(MouseEvent event);
}
"A mouse listener for the map panel, to show the terrain-changing menu as needed."
MouseListener&ToolTipSource&SelectionChangeSource componentMouseListener(
        IViewerModel model, ZOrderFilter zof,
        Comparison(TileFixture, TileFixture) comparator) {
    JPopupMenu&VersionChangeListener&SelectionChangeSource&SelectionChangeListener menu =
            terrainChangingMenu(model.mapDimensions.version, model);
    model.addSelectionChangeListener(menu);
    model.addVersionChangeListener(menu);
    String terrainFixturesAndTop(Point point) {
        IMapNG map = model.map;
        StringBuilder builder = StringBuilder();
        void accept(TileFixture fixture) {
            if (!builder.empty) {
                builder.append("<br />");
            }
            builder.append(fixture.string);
        }
        {TileFixture*} stream = {map.getGround(point), map.getForest(point), *map.getOtherFixtures(point)}
            .filter(zof.shouldDisplay).coalesced.sort(comparator);
        if (exists top = stream.first) {
            accept(top);
        }
        for (fixture in stream) {
            if (is TerrainFixture fixture) {
                accept(fixture);
            }
        }
        return builder.string;
    }
    object retval extends MouseAdapter() satisfies SelectionChangeSource&ToolTipSource {
        shared actual String? getToolTipText(MouseEvent event) {
            value eventPoint = event.point;
            MapDimensions mapDimensions = model.mapDimensions;
            Integer tileSize = TileViewSize.scaleZoom(model.zoomLevel,
                mapDimensions.version);
            VisibleDimensions visibleDimensions = model.dimensions;
            Point point = PointFactory.point(
                halfEven((eventPoint.y / tileSize) + visibleDimensions.minimumRow)
                    .plus(0.1).integer,
                halfEven((eventPoint.x / tileSize) + visibleDimensions.minimumCol)
                    .plus(0.1).integer);
            if (point.valid, point.row < mapDimensions.rows,
                    point.col < mapDimensions.columns) {
                String mountainString = (model.map.isMountainous(point))
                    then ", mountainous" else "";
                return "<html><body>``point``: ``model.map
                    .getBaseTerrain(point)````mountainString``<br />``
                    terrainFixturesAndTop(point)``</body></html>";
            } else {
                return null;
            }
        }
        shared actual void mouseClicked(MouseEvent event) {
            event.component.requestFocusInWindow();
            value eventPoint = event.point;
            VisibleDimensions visibleDimensions = model.dimensions;
            MapDimensions mapDimensions = model.mapDimensions;
            Integer tileSize = TileViewSize.scaleZoom(model.zoomLevel,
                mapDimensions.version);
            Point point = PointFactory.point(
                halfEven((eventPoint.y / tileSize) + visibleDimensions.minimumRow)
                    .plus(0.1).integer,
                halfEven((eventPoint.x / tileSize) + visibleDimensions.minimumCol)
                    .plus(0.1).integer);
            if (point.valid, point.row < mapDimensions.rows,
                    point.col < mapDimensions.columns) {
                model.setSelection(point);
                if (event.popupTrigger) {
                    menu.show(event.component, event.x, event.y);
                }
            }
        }
        shared actual void mousePressed(MouseEvent event) {
            if (event.popupTrigger) {
                menu.show(event.component, event.x, event.y);
            }
        }
        shared actual void mouseReleased(MouseEvent event) {
            if (event.popupTrigger) {
                menu.show(event.component, event.x, event.y);
            }
        }
        shared actual void addSelectionChangeListener(SelectionChangeListener listener) =>
                menu.addSelectionChangeListener(listener);
        shared actual void removeSelectionChangeListener(SelectionChangeListener listener)
                => menu.removeSelectionChangeListener(listener);
    }
    return retval;
}
"A component to display the map, even a large one, without the performance problems that
 came from drawing the entire map every time and letting Java manage the scrolling or,
 worse, instantiating a GUITile object for every visible tile every time the map was
 scrolled (or, yet worse again, a GUITile for every tile in the map, and removing them all
 and adding the visible tiles back in every time the map was scrolled)."
JComponent&MapGUI&MapChangeListener&SelectionChangeListener&GraphicalParamsListener
        mapComponent(IViewerModel model, ZOrderFilter zof,
        FixtureFilterTableModel matchers) {
    object iobs satisfies ImageObserver {
        shared late ImageObserver wrapped;
        shared actual Boolean imageUpdate(Image? img, Integer infoflags, Integer x,
        Integer y, Integer width, Integer height) => wrapped.imageUpdate(img,
            infoflags, x, y, width, height);
    }
    variable TileDrawHelper helper = TileDrawHelperFactory.instance.factory(
        model.mapDimensions.version, iobs, zof, matchers);
    MouseListener&ToolTipSource&SelectionChangeSource cml =
            componentMouseListener(model, zof, ceylonComparator(matchers));
    DirectionSelectionChanger dsl = DirectionSelectionChanger(model);
    Rectangle boundsCheck(Rectangle? rect) {
        if (exists rect) {
            return rect;
        } else {
            Integer tileSize = TileViewSize.scaleZoom(model.zoomLevel,
                model.mapDimensions.version);
            VisibleDimensions dimensions = model.dimensions;
            return Rectangle(0, 0,
                (dimensions.maximumCol - dimensions.minimumCol) * tileSize,
                (dimensions.maximumRow - dimensions.minimumRow) * tileSize);
        }
    }
    void paintTile(Graphics pen, Point point, Integer row, Integer column,
            Boolean selected) {
        Integer tileSize = TileViewSize.scaleZoom(model.zoomLevel,
            model.mapDimensions.version);
        helper.drawTile(pen, model.map, point, PointFactory.coordinate(column * tileSize,
            row * tileSize), PointFactory.coordinate(tileSize, tileSize));
        if (selected) {
            Graphics context = pen.create();
            try {
                context.color = Color.black;
                context.drawRect((column * tileSize) + 1, (row * tileSize) + 1,
                    tileSize - 2, tileSize - 2);
            } finally {
                context.dispose();
            }
        }
    }
    void fixVisibility() {
        Point selectedPoint = model.selectedPoint;
        Integer selectedRow = largest(selectedPoint.row, 0);
        Integer selectedColumn = largest(selectedPoint.col, 0);
        VisibleDimensions visibleDimensions = model.dimensions;
        variable Integer minimumRow = visibleDimensions.minimumRow;
        variable Integer maximumRow = visibleDimensions.maximumRow;
        variable Integer minimumColumn = visibleDimensions.minimumCol;
        variable Integer maximumColumn = visibleDimensions.maximumCol;
        if (selectedRow < minimumRow) {
            Integer difference = minimumRow - selectedRow;
            minimumRow -= difference;
            maximumRow -= difference;
        } else if (selectedRow > maximumRow) {
            Integer difference = selectedRow - maximumRow;
            minimumRow += difference;
            maximumRow += difference;
        }
        if (selectedColumn < minimumColumn) {
            Integer difference = minimumColumn - selectedColumn;
            minimumColumn -= difference;
            maximumColumn -= difference;
        } else if (selectedColumn > maximumColumn) {
            Integer difference = selectedColumn - maximumColumn;
            minimumColumn += difference;
            maximumColumn += difference;
        }
        model.dimensions = VisibleDimensions(minimumRow, maximumRow, minimumColumn,
            maximumColumn);
    }
    object retval extends JComponent() satisfies MapGUI&MapChangeListener&
            SelectionChangeListener&GraphicalParamsListener {
        doubleBuffered = true;
        shared actual IViewerModel mapModel = model;
        shared actual String? getToolTipText(MouseEvent event) =>
                cml.getToolTipText(event);
        shared actual void dimensionsChanged(VisibleDimensions oldDim,
            VisibleDimensions newDim) => repaint();
        shared actual void tileSizeChanged(Integer olSize, Integer newSize) {
            ComponentEvent event = ComponentEvent(this, ComponentEvent.componentResized);
            for (listener in componentListeners) {
                listener.componentResized(event);
            }
            repaint();
        }
        Boolean selectionVisible {
            Point selectedPoint = model.selectedPoint;
            Integer selectedRow = largest(selectedPoint.row, 0);
            Integer selectedColumn = largest(selectedPoint.col, 0);
            VisibleDimensions visibleDimensions = model.dimensions;
            Integer minimumRow = visibleDimensions.minimumRow;
            Integer maximumRow = visibleDimensions.maximumRow + 1;
            Integer minimumColumn = visibleDimensions.minimumCol;
            Integer maximumColumn = visibleDimensions.maximumCol +1;
            if ((minimumRow..maximumRow).contains(selectedRow),
                    (minimumColumn..maximumColumn).contains(selectedColumn)) {
                return true;
            } else {
                return false;
            }
        }
        shared actual void selectedPointChanged(Point? old, Point newPoint) {
            SwingUtilities.invokeLater(() => requestFocusInWindow());
            if (!selectionVisible) {
                fixVisibility();
            }
            repaint();
        }
        shared actual void mapChanged() {
            helper = TileDrawHelperFactory.instance.factory(model.mapDimensions.version,
                iobs, zof, matchers);
        }
        shared actual void paint(Graphics pen) {
            Graphics context = pen.create();
            try {
                context.color = Color.white;
                context.fillRect(0, 0, width, height);
                Rectangle bounds = boundsCheck(context.clipBounds);
                MapDimensions mapDimensions = model.mapDimensions;
                Integer tileSize = TileViewSize.scaleZoom(model.zoomLevel,
                    mapDimensions.version);
                void drawMapPortion(Integer minX, Integer minY, Integer maxX, Integer maxY) {
                    Integer minRow = model.dimensions.minimumRow;
                    Integer maxRow = model.dimensions.maximumRow;
                    Integer minCol = model.dimensions.minimumCol;
                    Integer maxCol = model.dimensions.maximumCol;
                    for (i in minY .. maxY) {
                        if ((i + minRow)>=(maxRow + 1)) {
                            break;
                        }
                        for (j in minX..maxX) {
                            if ((j + minCol) >= (maxCol + 1)) {
                                break;
                            }
                            Point location = PointFactory.point(i + minRow, j + minCol);
                            paintTile(context, location, i, j,
                                model.selectedPoint == location);
                        }
                    }
                }
                drawMapPortion(halfEven(bounds.minX / tileSize).plus(0.1).integer,
                    halfEven(bounds.minY / tileSize).plus(0.1).integer,
                    smallest(halfEven(bounds.maxX / tileSize).plus(1.1).integer,
                        mapDimensions.columns),
                    smallest(halfEven(bounds.maxY / tileSize).plus(1.1).integer,
                        mapDimensions.rows));
            } finally {
                context.dispose();
            }
            super.paint(pen);
        }
    }
    iobs.wrapped = retval;
    cml.addSelectionChangeListener(retval);
    retval.addMouseListener(cml);
    retval.addMouseWheelListener(dsl);
    assert (exists actionMap = retval.actionMap,
        exists inputMap = retval.getInputMap(JComponent.whenAncestorOfFocusedComponent));
    ArrowKeyListener.setUpListeners(dsl, inputMap, actionMap);
    retval.toolTipText = "";
    object mouseMotionListener extends MouseMotionAdapter() {
        shared actual void mouseMoved(MouseEvent event) => retval.repaint();
    }
    retval.addMouseMotionListener(mouseMotionListener);
    retval.requestFocusEnabled = true;
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
    JComponent&MapGUI&MapChangeListener&SelectionChangeListener&GraphicalParamsListener
        mapPanel = mapComponent(driverModel, tableModel, tableModel);
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