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
    VisibleDimensions,
    FixtureListModel,
    FixtureTransferable,
    CurriedFixtureTransferable,
    FixtureListDropListener
}
import view.map.main {
    ZoomListener,
    FixtureFilterList,
    MapWindowSizeListener,
    FixtureFilterTransferHandler,
    MapGUI,
    TileDrawHelper,
    TileDrawHelperFactory,
    DirectionSelectionChanger,
    ArrowKeyListener,
    TileUIHelper
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
    JFormattedTextField,
    JSplitPane,
    JScrollBar,
    InputVerifier,
    SwingList=JList,
    KeyStroke,
    JOptionPane,
    ListCellRenderer,
    DefaultListCellRenderer,
    Icon,
    ImageIcon
}
import strategicprimer.viewer.about {
    aboutDialog
}
import java.awt {
    Frame,
    Component,
    Dimension,
    Image,
    Graphics,
    Rectangle,
    Color,
    GridLayout,
    BorderLayout,
    Adjustable,
    Graphics2D
}
import java.awt.event {
    ActionEvent,
    WindowAdapter,
    MouseMotionAdapter,
    MouseEvent,
    ComponentEvent,
    MouseListener,
    MouseAdapter,
    AdjustmentEvent,
    KeyEvent,
    ActionListener
}
import view.util {
    FormattedLabel
}
import util {
    OnMac,
    IsNumeric,
    ImageLoader,
    ActionWrapper
}
import java.lang {
    JIterable = Iterable, JString=String
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
    TileType,
    HasPortrait,
    HasMutableName,
    HasMutableOwner,
    HasMutableKind,
    HasImage
}
import java.util.stream {
    Stream
}
import ceylon.interop.java {
    CeylonIterable,
    CeylonList,
    JavaIterable,
    JavaList,
    javaString,
    createJavaObjectArray
}
import model.map.fixtures {
    RiverFixture,
    UnitMember
}
import java.text {
    NumberFormat
}
import javax.swing.event {
    TableModelEvent,
    ListSelectionEvent,
    ListSelectionListener
}
import javax.swing.table {
    TableColumn
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
    ImageObserver,
    BufferedImage
}
import ceylon.math.float {
    halfEven,
    ceiling
}
import ceylon.collection {
    ArrayList,
    MutableList,
    HashSet,
    MutableSet
}
import lovelace.util.jvm {
    ceylonComparator,
    listenedButton,
    centeredHorizontalBox,
    BoxPanel,
    BoxAxis,
    boxPanel,
    BorderedPanel,
    horizontalSplit,
    verticalSplit,
    createHotKey
}
import model.map.fixtures.mobile {
    IUnit,
    Unit
}
import java.io {
    IOException,
    FileNotFoundException
}
import java.awt.dnd {
    DragGestureListener,
    DragGestureEvent,
    DnDConstants,
    DragSource,
    DropTarget
}
import java.awt.datatransfer {
    Transferable
}
import model.workermgmt {
    IWorkerTreeModel
}
import javax.swing.plaf.basic {
    BasicHTML
}
import javax.swing.text {
    View
}
import java.nio.file {
    NoSuchFileException
}
import com.bric.window {
    WindowMenu
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
    JPanel contentPanel = boxPanel(BoxAxis.pageAxis);
    contentPanel.add(searchBoxPane);
    contentPanel.add(backwards);
    contentPanel.add(vertically);
    contentPanel.add(caseSensitive);
    JPanel&BoxPanel buttonPanel = boxPanel(BoxAxis.lineAxis);
    buttonPanel.addGlue();
    JButton okButton = listenedButton("OK", okListener);
    JButton cancelButton = listenedButton("Cancel", (ActionEvent event) {
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
    contentPane = horizontalSplit(0.6, 0.6, contentPanel,
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
    JPanel contentPane = boxPanel(BoxAxis.pageAxis);
    contentPane.add(mainLabel);
    JPanel&BoxPanel boxPanelObj = boxPanel(BoxAxis.lineAxis);
    boxPanelObj.add(JLabel("Row: "));
    boxPanelObj.add(rowField);
    rowField.setActionCommand("OK");
    rowField.addActionListener(handleOK);
    boxPanelObj.addGlue();
    boxPanelObj.add(JLabel("Column:"));
    boxPanelObj.add(columnField);
    columnField.setActionCommand("OK");
    columnField.addActionListener(handleOK);
    boxPanelObj.addGlue();
    contentPane.add(boxPanelObj);
    contentPane.add(errorLabel);
    errorLabel.text = "";
    errorLabel.minimumSize = Dimension(200, 15);
    errorLabel.alignmentX = Component.centerAlignment;
    errorLabel.alignmentY = Component.topAlignment;
    JPanel&BoxPanel buttonPanel = boxPanel(BoxAxis.lineAxis);
    buttonPanel.addGlue();
    JButton okButton = listenedButton("OK", handleOK);
    JButton cancelButton = listenedButton("Cancel", (ActionEvent event) {
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
    JButton okButton = listenedButton("OK", okListener);
    retval.add(okButton);
    JButton cancelButton = listenedButton("Cancel", (ActionEvent event) {
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
"The part of the key showing a tile's color."
class KeyElementComponent(Color color, Dimension minimum, Dimension preferred,
        Dimension maximum) extends JComponent() {
    minimumSize = minimum;
    preferredSize = preferred;
    maximumSize = maximum;
    shared actual void paint(Graphics pen) {
        Graphics context = pen.create();
        try {
            context.color = color;
            context.fillRect(0, 0, width, height);
        } finally {
            context.dispose();
        }
    }
}
"A pop-up menu to let the user edit a fixture."
JPopupMenu fixtureEditMenu(IFixture fixture, {Player*} players,
        IWorkerTreeModel* changeListeners) {
    variable Boolean immutable = true;
    JPopupMenu retval = JPopupMenu();
    void addMenuItem(JMenuItem item, Anything(ActionEvent) listener) {
        retval.add(item);
        item.addActionListener(listener);
    }
    if (is HasMutableName fixture) {
        addMenuItem(JMenuItem("Rename", KeyEvent.vkN), (ActionEvent event) {
            String originalName = fixture.name;
            if (exists result = JOptionPane.showInputDialog(retval,
                    "Fixture's new name:", "Rename Fixture",
                    JOptionPane.plainMessage, null, null, javaString(originalName))) {
                String resultString = result.string.trimmed;
                if (resultString != originalName.trimmed) {
                    HasMutableName temp = fixture;
                    temp.setName(resultString);
                    for (listener in changeListeners) {
                        listener.renameItem(fixture);
                    }
                }
            }
        });
        immutable = false;
    }
    if (is HasMutableKind fixture) {
        addMenuItem(JMenuItem("Change kind", KeyEvent.vkK), (ActionEvent event) {
            String originalKind = fixture.kind;
            if (exists result = JOptionPane.showInputDialog(retval,
                "Fixture's new kind:", "Change Fixture Kind",
                JOptionPane.plainMessage, null, null, javaString(originalKind))) {
                String resultString = result.string.trimmed;
                if (resultString != originalKind.trimmed) {
                    HasMutableKind temp = fixture;
                    temp.setKind(resultString);
                    for (listener in changeListeners) {
                        listener.moveItem(fixture);
                    }
                }
            }
        });
        immutable = false;
    }
    if (is HasMutableOwner fixture) {
        addMenuItem(JMenuItem("Change owner", KeyEvent.vkO), (ActionEvent event) {
            if (is Player player = JOptionPane.showInputDialog(retval,
                    "Fixture's new owner:", "Change Fixture Owner",
                    JOptionPane.plainMessage, null, createJavaObjectArray(players),
                    fixture.owner)) {
                HasMutableOwner temp = fixture;
                temp.setOwner(player);
            }
        });
        immutable = false;
    }
    if (is UnitMember fixture) {
        String name;
        if (is HasName fixture) {
            name = fixture.name;
        } else {
            name = "this ``fixture``";
        }
        addMenuItem(JMenuItem("Dismiss", KeyEvent.vkD), (ActionEvent event) {
            Integer reply = JOptionPane.showConfirmDialog(retval,
                "Are you sure you want to dismiss ``name``?",
                "Confirm Dismissal", JOptionPane.yesNoOption);
            if (reply == JOptionPane.yesOption) {
                for (listener in changeListeners) {
                    listener.dismissUnitMember(fixture);
                }
            }
        });
    }
    if (immutable) {
        retval.add(JLabel("Fixture is not mutable"));
    }
    return retval;
}
"A cell renderer for tile-details GUIs."
class FixtureCellRenderer satisfies ListCellRenderer<TileFixture> {
    static DefaultListCellRenderer defaultRenderer =
            DefaultListCellRenderer();
    static MutableSet<String> missingFilenames = HashSet<String>();
    static Icon createDefaultFixtureIcon() {
        Integer imageSize = 24;
        BufferedImage temp = BufferedImage(imageSize, imageSize,
            BufferedImage.typeIntArgb);
        Graphics2D pen = temp.createGraphics();
        Color saveColor = pen.color;
        pen.color = Color.\iRED;
        Float margin = 0.15;
        Float pixelMargin = halfEven(imageSize * margin);
        Float afterMargin = halfEven(imageSize * (1.0 - (margin * 2.0)));
        Float cornerRounding = halfEven((imageSize * margin) / 2.0);
        pen.fillRoundRect(pixelMargin.integer + 1, pixelMargin.integer + 1,
            afterMargin.integer, afterMargin.integer, cornerRounding.integer,
            cornerRounding.integer);
        pen.color = saveColor;
        Float newMargin = halfEven((imageSize / 2.0) - (imageSize * margin));
        Float newAfterMargin = halfEven(imageSize * margin * 2.0);
        Float newCorner = halfEven((imageSize * margin) / 2.0);
        pen.fillRoundRect(newMargin.integer + 1, newMargin.integer + 1,
            newAfterMargin.integer, newAfterMargin.integer, newCorner.integer,
            newCorner.integer);
        return ImageIcon(temp);
    }
    "Set a component's height given a fixed with."
    by("http://blog.nobel-joergensen.com/2009/01/18/changing-preferred-size-of-a-html-jlabel/")
    static void setComponentPreferredSize(JComponent component, Integer width) {
        assert (is View view = component.getClientProperty(BasicHTML.propertyKey));
        view.setSize(width.float, 0.0);
        Integer wid = ceiling(view.getPreferredSpan(View.xAxis)).integer;
        Integer height = ceiling(view.getPreferredSpan(view.yAxis)).integer;
        component.preferredSize = Dimension(wid, height);
    }
    shared new () { }
    Icon defaultFixtureIcon = createDefaultFixtureIcon(); // TODO: make static?
    Icon getIcon(HasImage obj) {
        String image = obj.image;
        String actualImage;
        if (image.empty || missingFilenames.contains(image)) {
            actualImage = obj.defaultImage;
        } else {
            actualImage = image;
        }
        if (missingFilenames.contains(actualImage)) {
            return defaultFixtureIcon;
        }
        try {
            return ImageLoader.loader.loadIcon(actualImage);
        } catch (FileNotFoundException|NoSuchFileException except) {
            log.error("image file images/``actualImage`` not found");
            log.debug("With stack trace", except);
            missingFilenames.add(actualImage);
            return defaultFixtureIcon;
        } catch (IOException except) {
            log.error("I/O error reading image", except);
            return defaultFixtureIcon;
        }
    }
    shared actual Component getListCellRendererComponent(SwingList<out TileFixture> list,
            TileFixture val, Integer index, Boolean isSelected, Boolean cellHasFocus) {
        assert (is JLabel component = defaultRenderer.getListCellRendererComponent(list,
            val, index, isSelected, cellHasFocus));
        component.text = "<html><p>``val``</p></html>";
        if (is HasImage val) {
            component.icon = getIcon(val);
        } else {
            component.icon = defaultFixtureIcon;
        }
        component.maximumSize = Dimension(component.maximumSize.width.integer,
            (component.maximumSize.height * 2).integer);
        setComponentPreferredSize(component, list.width);
        return component;
    }
}
"A visual list-based representation of the contents of a tile."
SwingList<TileFixture>&DragGestureListener&SelectionChangeListener fixtureList(
        JComponent parentComponent, FixtureListModel listModel,
        {Player*} players) {
    object retval extends SwingList<TileFixture>(listModel)
            satisfies DragGestureListener&SelectionChangeListener {
        cellRenderer = FixtureCellRenderer();
        selectionMode = ListSelectionModel.multipleIntervalSelection;
        shared actual void dragGestureRecognized(DragGestureEvent event) {
            List<TileFixture> selection = CeylonList(selectedValuesList);
            if (exists first = selection.first) {
                Transferable payload;
                value rest = selection.rest;
                if (rest.empty) {
                    payload = FixtureTransferable(first);
                } else {
                    payload = CurriedFixtureTransferable(JavaList(selection));
                }
                event.startDrag(null, payload);
            }
        }
        shared actual Boolean equals(Object that) {
            if (is SwingList<out Anything> that) {
                return model == that.model;
            } else {
                return false;
            }
        }
        shared actual Integer hash => listModel.hash;
        shared actual void selectedPointChanged(Point? old, Point newPoint) =>
                listModel.selectedPointChanged(old, newPoint);
        object fixtureMouseListener extends MouseAdapter() {
            void handleMouseEvent(MouseEvent event) {
                if (event.popupTrigger, event.clickCount == 1) {
                    Integer index = locationToIndex(event.point);
                    if ((0..listModel.size).contains(index)) {
                        fixtureEditMenu(listModel.elementAt(index), players)
                            .show(event.component, event.x, event.y);
                    }
                }
            }
            shared actual void mouseClicked(MouseEvent event) => handleMouseEvent(event);
            shared actual void mousePressed(MouseEvent event) => handleMouseEvent(event);
            shared actual void mouseReleased(MouseEvent event) => handleMouseEvent(event);
        }
    }
    DragSource.defaultDragSource.createDefaultDragGestureRecognizer(retval,
        DnDConstants.actionCopy, retval);
    retval.dropTarget = DropTarget(retval, FixtureListDropListener(parentComponent,
        listModel));
    createHotKey(retval, "delete",
        ActionWrapper((ActionEvent event) => listModel.removeAll(
            retval.selectedValuesList)),
        JComponent.whenAncestorOfFocusedComponent,
        KeyStroke.getKeyStroke(KeyEvent.vkDelete, 0),
        KeyStroke.getKeyStroke(KeyEvent.vkBackSpace, 0));
    return retval;
}
"A panel to show the details of a tile, using a list rather than sub-panels with chits
 for its fixtures."
JComponent&VersionChangeListener&SelectionChangeListener detailPanel(
        variable Integer version, IDriverModel model) {
    TileUIHelper helper = TileUIHelper();
    JComponent keyElement(Integer version, TileType type) {
        JPanel&BoxPanel retval = boxPanel(BoxAxis.lineAxis);
        retval.addGlue();
        retval.addRigidArea(7);
        JPanel&BoxPanel panel = boxPanel(BoxAxis.pageAxis);
        panel.addRigidArea(4);
        Integer tileSize = TileViewSize.scaleZoom(ViewerModel.defZoomLevel, version);
        panel.add(KeyElementComponent(helper.get(version, type), Dimension(4, 4),
            Dimension(8, 8), Dimension(tileSize, tileSize)));
        panel.addRigidArea(4);
        JLabel label = JLabel(helper.getDescription(type));
        panel.add(label);
        panel.addRigidArea(4);
        retval.add(panel);
        retval.addRigidArea(7);
        retval.addGlue();
        retval.minimumSize = Dimension(largest(4, label.minimumSize.width.integer) + 14,
            16 + label.minimumSize.height.integer);
        return retval;
    }
    object keyPanel extends JPanel(GridLayout(0, 4)) satisfies VersionChangeListener {
        minimumSize = Dimension(
            (keyElement(version, TileType.notVisible).minimumSize.width * 4).integer,
            minimumSize.height.integer);
        preferredSize = minimumSize;
        shared actual void changeVersion(Integer old, Integer newVersion) {
            removeAll();
            for (type in TileType.valuesForVersion(newVersion)) {
                add(keyElement(version, type));
            }
        }
    }
    keyPanel.changeVersion(-1, version);
    FormattedLabel header = FormattedLabel(
        "<html><body><p>Contents of the tile at (%d, %d):</p></body></html>", -1, -1);
    object retval extends JSplitPane(JSplitPane.horizontalSplit, true)
            satisfies VersionChangeListener&SelectionChangeListener {
        shared late SelectionChangeListener delegate;
        shared actual void changeVersion(Integer old, Integer newVersion) =>
                keyPanel.changeVersion(old, newVersion);
        shared actual void selectedPointChanged(Point? old, Point newPoint) {
            delegate.selectedPointChanged(old, newPoint);
            header.setArgs(newPoint.row, newPoint.col);
        }
    }
    SwingList<TileFixture>&SelectionChangeListener fixtureListObject =
            fixtureList(retval, FixtureListModel(model.map, false),
                CeylonIterable(model.map.players()));
    retval.delegate = fixtureListObject;
    object portrait extends JComponent() satisfies ListSelectionListener {
        ImageLoader loader = ImageLoader.loader;
        variable Image? portrait = null;
        shared actual void paintComponent(Graphics pen) {
            super.paintComponent(pen);
            if (exists local = portrait) {
                pen.drawImage(local, 0, 0, width, height, this);
            }
        }
        shared actual void valueChanged(ListSelectionEvent event) {
            List<TileFixture> selections =
                    CeylonList(fixtureListObject.selectedValuesList);
            portrait = null;
            if (!selections.empty, selections.size == 1) {
                if (is HasPortrait selectedValue = selections.first) {
                    String portraitName = selectedValue.portrait;
                    if (!portraitName.empty) {
                        try {
                            portrait = loader.loadImage(portraitName);
                        } catch (IOException except) {
                            log.warn("I/O error loading portrait", except);
                        }
                    }
                }
            }
        }
    }
    fixtureListObject.addListSelectionListener(portrait);
    JPanel listPanel = BorderedPanel.verticalPanel(header, JScrollPane(fixtureListObject),
        null);
    retval.leftComponent = horizontalSplit(0.5, 0.5, listPanel, portrait);
    retval.rightComponent = keyPanel;
    retval.resizeWeight = 0.9;
    retval.setDividerLocation(0.9);
    return retval;
}
"A class to change the visible area of the map based on the user's use of the scrollbars."
todo("Maybe keep track of visible dimensions and selected point directly instaed of
      through the model, so we can drop the reference to the model.")
class ScrollListener satisfies MapChangeListener&SelectionChangeListener&
        GraphicalParamsListener {
    static class LocalInputVerifier extends InputVerifier {
        Integer() mapDimension;
        Integer() visibleDimension;
        shared new horizontal(MapDimensions() mapDimsSource,
                VisibleDimensions() visibleDimsSource) extends InputVerifier() {
            mapDimension = () => mapDimsSource().columns;
            visibleDimension = () => visibleDimsSource().width;
        }
        shared new vertical(MapDimensions() mapDimsSource,
                VisibleDimensions() visibleDimsSource) extends InputVerifier() {
            mapDimension = () => mapDimsSource().rows;
            visibleDimension = () => visibleDimsSource().height;
        }
        "A scrollbar is valid if its value is between 0 and the size of the map minus the
         visible size of the map (that subtraction is to prevent scrolling so far that
         empty tiles show to the right of or below the map)."
        shared actual Boolean verify(JComponent input) {
            if (is JScrollBar input) {
                return (0..(mapDimension() - visibleDimension())).contains(input.\ivalue);
            } else {
                return false;
            }
        }
    }
    IViewerModel model;
    JScrollBar horizontalBar;
    JScrollBar verticalBar;
    variable MapDimensions mapDimensions;
    variable VisibleDimensions visibleDimensions;
    shared new (IViewerModel mapModel, JScrollBar horizontal, JScrollBar vertical) {
        model = mapModel;
        visibleDimensions = mapModel.dimensions;
        mapDimensions = mapModel.mapDimensions;
        Point selectedPoint = mapModel.selectedPoint;
        horizontalBar = horizontal;
        horizontal.model.setRangeProperties(largest(selectedPoint.col, 0), 1, 0,
            mapDimensions.columns - visibleDimensions.width, false);
        horizontal.inputVerifier = LocalInputVerifier.horizontal(() => mapDimensions,
            () => visibleDimensions);
        verticalBar = vertical;
        vertical.model.setRangeProperties(largest(selectedPoint.row, 0), 1, 0,
            mapDimensions.rows - visibleDimensions.height, false);
        vertical.inputVerifier = LocalInputVerifier.vertical(() => mapDimensions,
            () => visibleDimensions);
        void adjustmentListener(AdjustmentEvent event) =>
                model.dimensions = VisibleDimensions(verticalBar.\ivalue,
                    verticalBar.\ivalue + visibleDimensions.height, horizontalBar.\ivalue,
                    horizontalBar.\ivalue + visibleDimensions.width);
        horizontalBar.addAdjustmentListener(adjustmentListener);
        verticalBar.addAdjustmentListener(adjustmentListener);
    }
    "Alternate constructor that adds new scroll-bars to an existing component. This only
     works if that component is laid out using a [[BorderLayout]] and doesn't already have
      members at page-end and line-end."
    shared new createScrollBars(IViewerModel mapModel, BorderedPanel component)
            extends ScrollListener(mapModel, JScrollBar(Adjustable.horizontal),
                JScrollBar(Adjustable.vertical)) {
        component.add(horizontalBar, BorderLayout.pageEnd);
        component.add(verticalBar, BorderLayout.lineEnd);
    }
    "Handle a change in visible dimensions."
    shared actual void dimensionsChanged(VisibleDimensions oldDimensions,
            VisibleDimensions newDimensions) {
        visibleDimensions = newDimensions;
        horizontalBar.model.setRangeProperties(largest(model.selectedPoint.col, 0), 1, 0,
            mapDimensions.columns - newDimensions.width, false);
        verticalBar.model.setRangeProperties(largest(model.selectedPoint.row, 0), 1, 0,
            mapDimensions.rows - newDimensions.height, false);
    }
    "Ignored."
    todo("Should we really ignore this?")
    shared actual void tileSizeChanged(Integer oldSize, Integer newSize) { }
    "Handle a change to the selected location in the map. The property-change based
     version this replaced went to the model for the selected point rather than looking
     at the reported new value; since it's typesafe here, and probably faster, this
     switched to using the new value it was passed."
    shared actual void selectedPointChanged(Point? old, Point newPoint) {
        VisibleDimensions temp = model.dimensions;
        if (!((temp.minimumCol)..(temp.maximumCol + 1)).contains(newPoint.col)) {
            horizontalBar.model.\ivalue = largest(newPoint.col, 0);
        }
        if (!((temp.minimumRow)..(temp.maximumRow + 1)).contains(newPoint.row)) {
            verticalBar.model.\ivalue = largest(newPoint.row, 0);
        }
    }
    "Handle notification that a new map was loaded."
    shared actual void mapChanged() {
        mapDimensions = model.mapDimensions;
        visibleDimensions = model.dimensions;
        horizontalBar.model.setRangeProperties(0, 1, 0,
            mapDimensions.columns - visibleDimensions.width, false);
        verticalBar.model.setRangeProperties(0, 1, 0,
            mapDimensions.rows - visibleDimensions.height, false);
    }
}
"Encapsulate the map component in a panel with scroll-bars and set up the connection
 between the scroll-bars and the map's scrolling."
BorderedPanel mapScrollPanel(IViewerModel model, JComponent component) {
    BorderedPanel retval = BorderedPanel(component, null, null, null, null);
    ScrollListener scrollListener = ScrollListener.createScrollBars(model, retval);
    model.addGraphicalParamsListener(scrollListener);
    model.addMapChangeListener(scrollListener);
    return retval;
}
"The main window for the map viewer app."
SPFrame&IViewerFrame viewerFrame(IViewerModel driverModel,
        Anything(ActionEvent) menuHandler) {
    object retval extends SPFrame("Map Viewer", driverModel.mapFile.orElse(null))
            satisfies IViewerFrame {
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
    JComponent&SelectionChangeListener&VersionChangeListener detailPane =
            detailPanel(driverModel.mapDimensions.version, driverModel);
    driverModel.addVersionChangeListener(detailPane);
    driverModel.addSelectionChangeListener(detailPane);
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
        OnMac.makeButtonsSegmented(allButton, noneButton);
        JPanel buttonPanel = (OnMac.systemIsMac) then
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
    WindowAdapter windowSizeListener = MapWindowSizeListener(mapPanel);
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
                SPFrame&IViewerFrame frame = viewerFrame(model,
                    menuHandler.actionPerformed);
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
