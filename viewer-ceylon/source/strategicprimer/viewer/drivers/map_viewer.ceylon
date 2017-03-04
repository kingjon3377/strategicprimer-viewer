import controller.map.misc {
    ICLIHelper,
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
    ZOrderFilter,
    TileViewSize,
    VisibleDimensions,
    FixtureListModel,
    FixtureTransferable,
    CurriedFixtureTransferable,
    FixtureListDropListener,
    TileTypeFixture
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
    ImageIcon,
    InputMap,
    ActionMap,
    ListModel,
    DefaultListModel,
    TransferHandler
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
    Graphics2D,
    Shape,
    Polygon
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
    ActionListener,
    ComponentAdapter,
    WindowEvent,
    InputEvent,
    MouseWheelListener,
    MouseWheelEvent
}
import util {
    IsNumeric,
    ResourceInputStream,
    ReorderableListModel,
    IntTransferable,
    Reorderable
}
import java.lang {
    JIterable = Iterable, JString=String,
    IllegalArgumentException,
    IllegalStateException, JBoolean=Boolean, JClass=Class
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
    HasImage,
    River,
    IEvent
}
import java.util {
    JComparator=Comparator
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
    createJavaObjectArray,
    createJavaIntArray,
    javaClass,
    javaClassFromInstance
}
import model.map.fixtures {
    RiverFixture,
    UnitMember,
    Ground,
    TextFixture
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
    TableColumn,
    TableModel,
    AbstractTableModel
}
import lovelace.util.common {
    todo, Comparator
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
    MutableSet,
    MutableMap,
    HashMap
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
    createHotKey,
    FormattedLabel,
    platform,
    ActionWrapper
}
import model.map.fixtures.mobile {
    IUnit,
    Unit,
    SimpleImmortal,
    Giant,
    Dragon,
    Centaur,
    Fairy,
    Animal
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
    Transferable,
    DataFlavor,
    UnsupportedFlavorException
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
import java.awt.geom {
    Line2D,
    Rectangle2D,
    Ellipse2D
}
import view.util {
    Coordinate
}
import model.map.fixtures.towns {
    Fortress,
    AbstractTown,
    Village
}
import model.map.fixtures.terrain {
    Forest,
    Oasis,
    Sandbar,
    Hill
}
import ceylon.language.meta {
    type
}
import ceylon.language.meta.model {
    ClassModel,
    ClassOrInterface
}
import javax.imageio {
    ImageIO
}
import model.map.fixtures.resources {
    Grove,
    Meadow,
    StoneDeposit,
    MineralVein,
    Mine,
    Shrub,
    CacheFixture
}
import model.map.fixtures.explorable {
    Battlefield,
    Cave,
    Portal,
    AdventureFixture
}
"A class for moving the cursor around the single-component map UI, including scrolling
 using a mouse wheel."
class DirectionSelectionChanger(IViewerModel model) satisfies MouseWheelListener {
    "Move the cursor up a row."
    shared void up() {
        Point old = model.selectedPoint;
        if (old.row > 0) {
            model.setSelection(PointFactory.point(old.row - 1, old.col));
        }
    }
    "Move the cursor left a column."
    shared void left() {
        Point old = model.selectedPoint;
        if (old.col > 0) {
            model.setSelection(PointFactory.point(old.row, old.col - 1));
        }
    }
    "Move the cursor down a row."
    shared void down() {
        Point old = model.selectedPoint;
        if (old.row < model.mapDimensions.rows) {
            model.setSelection(PointFactory.point(old.row + 1, old.col));
        }
    }
    "Move the cursor right a column."
    shared void right() {
        Point old = model.selectedPoint;
        if (old.col<model.mapDimensions.columns) {
            model.setSelection(PointFactory.point(old.row, old.col + 1));
        }
    }
    "Move the cursor all the way to the top."
    shared void jumpUp() {
        Point old = model.selectedPoint;
        if (old.row > 0) {
            model.setSelection(PointFactory.point(0, old.col));
        }
    }
    "Move the cursor all the way to the bottom."
    shared void jumpDown() {
        Point old = model.selectedPoint;
        if (old.row < model.mapDimensions.rows) {
            model.setSelection(PointFactory.point(model.mapDimensions.rows - 1, old.col));
        }
    }
    "Move the cursor all the way to the left."
    shared void jumpLeft() {
        Point old = model.selectedPoint;
        if (old.col > 0) {
            model.setSelection(PointFactory.point(old.row, 0));
        }
    }
    "Move the cursor all the way to the right."
    shared void jumpRight() {
        Point old = model.selectedPoint;
        if (old.col<model.mapDimensions.columns) {
            model.setSelection(PointFactory.point(old.row, model.mapDimensions.columns - 1));
        }
    }
    "Scroll."
    void scroll("Whether to scroll horizontally" Boolean horizontal,
            "Whether to scroll forward (down/right)" Boolean forward,
            "How many times to scroll" Integer count) {
        Anything() func;
        if (horizontal, forward) {
            func = right;
        } else if (horizontal) {
            func = left;
        } else if (forward) {
            func = down;
        } else {
            func = up;
        }
        for (i in 0..count) {
            func();
        }
    }
    "Scroll when the user scrolls the mouse wheel."
    shared actual void mouseWheelMoved(MouseWheelEvent event) {
        if (platform.hotKeyPressed(event)) {
            // Zoom if Command-scroll/Control-scroll
            Integer count = event.wheelRotation;
            if (count < 0) {
                for (i in 0..count) {
                    model.zoomIn();
                }
            } else {
                for (i in 0..count) {
                    model.zoomOut();
                }
            }
        } else if (event.shiftDown) {
            // Scroll sideways on Shift-scroll
            Integer count = event.wheelRotation;
            if (count < 0) {
                scroll(true, false, 0 - count);
            } else {
                scroll(true, true, count);
            }
        } else {
            // Otherwise, no relevant modifiers being pressed, scroll vertically
            // TODO: should Control have meaning on Mac?
            Integer count = event.wheelRotation;
            if (count < 0) {
                scroll(false, false, 0 - count);
            } else {
                scroll(false, true, count);
            }
        }
    }
}
"A transfer-handler to let the user drag items in the list to control Z-order."
object fixtureFilterTransferHandler extends TransferHandler() {
    DataFlavor flavor = DataFlavor(`FixtureMatcher`, "FixtureMatcher");
    "A drag/drop operation is supported iff it is a supported flavor and it is or
     can be coerced to be a move operation."
    shared actual Boolean canImport(TransferSupport support) {
        if (support.drop, support.isDataFlavorSupported(flavor),
                TransferHandler.move.and(support.sourceDropActions) ==
                    TransferHandler.move) {
            support.dropAction = TransferHandler.move;
            return true;
        } else {
            return false;
        }
    }
    "Create a wrapper to transfer contents of the given component, which must be a
     [[SwingList]] or a [[JTable]]."
    shared actual Transferable createTransferable(JComponent component) {
        if (is SwingList<out Anything> component) {
            return IntTransferable(flavor, component.selectedIndex);
        } else if (is JTable component) {
            return IntTransferable(flavor, component.selectedRow);
        } else {
            throw IllegalStateException(
                "Tried to create transferable from non-list");
        }
    }
    "This listener only allows move operations."
    shared actual Integer getSourceActions(JComponent component) => TransferHandler.move;
    "Handle a drop."
    shared actual Boolean importData(TransferSupport support) {
        if (!support.drop) {
            return false;
        }
        Component component = support.component;
        DropLocation dropLocation = support.dropLocation;
        Transferable transfer = support.transferable;
        Integer payload;
        try {
            assert (is Integer temp = transfer.getTransferData(flavor));
            payload = temp;
        } catch (UnsupportedFlavorException|IOException except) {
            log.debug("Transfer failure", except);
            return false;
        }
        if (is SwingList<out Anything> component,
                is Reorderable model = component.model,
                is SwingList<out Anything>.DropLocation dropLocation) {
            Integer index = dropLocation.index;
            model.reorder(payload, index);
            return true;
        } else if (is JTable component, is Reorderable model = component.model,
                is JTable.DropLocation dropLocation) {
            Integer index = dropLocation.row;
            Integer selection = component.selectedRow;
            model.reorder(payload, index);
            if (selection == payload) {
                component.setRowSelectionInterval(index, index);
            } else if (selection > index, selection < payload) {
                component.setRowSelectionInterval(selection - 1, selection - 1);
            }
            return true;
        } else {
            return false;
        }
    }
}
"A list to let the user select which fixtures ought to be searched."
SwingList<FixtureMatcher>&ZOrderFilter fixtureFilterList() {
    DefaultListModel<FixtureMatcher> matcherListModel = ReorderableListModel<FixtureMatcher>();
    for (matcher in {simpleMatcher<Ground>(Ground.exposed, "Ground (exposed)"),
            simpleMatcher<Ground>((Ground ground) => !ground.exposed, "Ground"),
            simpleMatcher<Grove>(Grove.orchard, "Orchards"),
            simpleMatcher<Grove>((Grove grove) => !grove.orchard, "Groves"),
            simpleMatcher<Meadow>(Meadow.field, "Fields"),
            simpleMatcher<Meadow>((Meadow meadow) => !meadow.field, "Meadows")}) {
        matcherListModel.addElement(matcher);
    }
    object retval extends SwingList<FixtureMatcher>(matcherListModel)
            satisfies ZOrderFilter {
        shared actual Boolean shouldDisplay(TileFixture fixture) {
            for (i in 0..matcherListModel.size) {
                FixtureMatcher matcher = matcherListModel.getElementAt(i);
                if (matcher.matches(fixture)) {
                    return matcher.displayed;
                }
            }
            value cls = type(fixture);
            matcherListModel.addElement(
                FixtureMatcher((TileFixture fix) => cls.typeOf(fix), fixture.plural()));
            Integer size = matcherListModel.size;
            selectionModel.addSelectionInterval(size - 1, size - 1);
            return true;
        }
    }
    ListSelectionModel selectionModel = retval.selectionModel;
    selectionModel.selectionMode = ListSelectionModel.multipleIntervalSelection;
    selectionModel.addListSelectionListener((ListSelectionEvent event) {
        for (i in 0.. matcherListModel.size) {
            matcherListModel.getElementAt(i).displayed = selectionModel.isSelectedIndex(i);
        }
    });
    DefaultListCellRenderer defaultRenderer = DefaultListCellRenderer();
    object renderer satisfies ListCellRenderer<FixtureMatcher> {
        shared actual Component getListCellRendererComponent(
                SwingList<out FixtureMatcher> list, FixtureMatcher item,
                Integer index, Boolean isSelected, Boolean cellHasFocus) {
            value retval = defaultRenderer.getListCellRendererComponent(list, item,
                index, isSelected, cellHasFocus);
            if (is JLabel retval) {
                retval.text = item.description;
            }
            return retval;
        }
    }
    retval.cellRenderer = renderer;
    retval.transferHandler = fixtureFilterTransferHandler;
    retval.dropMode = DropMode.insert;
    retval.dragEnabled = true;
    return retval;
}
"""A dialog to let the user find fixtures by ID, name, or "kind"."""
class FindDialog(Frame parent, IViewerModel model) extends SPDialog(parent, "Find") {
    JTextField searchField = JTextField("", 20);
    JCheckBox backwards = JCheckBox("Search backwards");
    JCheckBox vertically = JCheckBox("Search vertically then horizontally");
    JCheckBox caseSensitive = JCheckBox("Case-sensitive search");
    Component&ZOrderFilter filterList = fixtureFilterList();
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
    platform.makeButtonsSegmented(okButton, cancelButton);
    buttonPanel.add(okButton);
    if (platform.systemIsMac) {
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
    platform.makeButtonsSegmented(okButton, cancelButton);
    buttonPanel.add(okButton);
    if (!platform.systemIsMac) {
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
    platform.makeButtonsSegmented(okButton, cancelButton);
    retval.add(cancelButton);
    retval.setMinimumSize(Dimension(150, 80));
    (retval of Component).preferredSize = Dimension(200, 90);
    (retval of Component).maximumSize = Dimension(300, 90);
    retval.pack();
    return retval;
}
"An interface for helpers that do the drawing of tiles in various components."
interface TileDrawHelper {
    "Draw a tile. Assumes that the graphics context has been translated so that its origin
     coincides with the tile's upper-left-hand corner."
    shared formal void drawTileTranslated(
        "The graphics context"
        Graphics pen,
        "The map to draw a tile from"
        IMapNG map,
        "The (location of the) tile to draw"
        Point location,
        "The width of the drawing area (i.e. how wide to draw the tile)"
        Integer width,
        "The height of the drawing area (i.e. how tall to draw the tile)"
        Integer height);
    "Draw a tile, at the given coordinates."
    shared formal void drawTile(
        "The graphics context"
        Graphics pen,
        "The map to draw a tile from"
        IMapNG map,
        "The (location of the) tile to draw"
        Point location,
        "The coordinates of the tile's upper-left corner."
        Coordinate coordinates,
        "The width ('x') and height ('y') to draw the tile within."
        Coordinate dimensions);
    "Whether the given map has any fortresses at the given location."
    todo("Move out of the interface")
    shared default Boolean hasAnyForts(IMapNG map, Point location) =>
            CeylonIterable(map.getOtherFixtures(location))
                .any((fixture) => fixture is Fortress);
    "Whether the given map has any units at the given location."
    todo("Move out of the interface")
    shared default Boolean hasAnyUnits(IMapNG map, Point location) =>
            CeylonIterable(map.getOtherFixtures(location))
                .any((fixture) => fixture is IUnit);
    """Whether the given map has any "events" at the given location."""
    todo("Move out of the interface", "Port to use Ceylon Iterable instead of Java Stream")
    shared default Boolean hasEvent(IMapNG map, Point location) =>
            map.streamAllFixtures(location).anyMatch((fixture) => fixture is IEvent);
}
"An object encapsulating the mapping from tile-types to colors."
object colorHelper {
    String wrap(String wrapped) => "<html<p>``wrapped``</p></html>";
    "Descriptions of the types."
    Map<TileType, String> descriptions = HashMap {
        TileType.borealForest->wrap("Boreal Forest"),
        TileType.desert->wrap("Desert"),
        TileType.jungle->wrap("Jungle"),
        TileType.mountain->wrap("Mountains"),
        TileType.notVisible->wrap("Unknown"),
        TileType.ocean->wrap("Ocean"),
        TileType.plains->wrap("Plains"),
        TileType.temperateForest->wrap("Temperate Forest"),
        TileType.tundra->wrap("Tundra"),
        TileType.steppe->wrap("Steppe")
    };
    "A map from types of features to the colors they can make the tile be. Used to
      show that a tile is forested, e.g., even when that is normally represented by
       an icon and there's a higher icon on the tile."
    Map<ClassOrInterface<TileFixture>, Color> featureColors = HashMap {
        `Forest`->Color(0, 117, 0),
        `Oasis`->Color(72, 218, 164),
        `Sandbar`->Color(249, 233, 28),
        `Hill`->Color(141, 182, 0)
    };
    "A map from map versions to maps from tile-types to colors."
    Map<Integer, Map<TileType, Color>> colors = HashMap {
        1->HashMap {
            TileType.borealForest->Color(72, 218, 164),
            TileType.desert->Color(249, 233, 28),
            TileType.jungle->Color(229, 46, 46),
            TileType.mountain->Color(249, 137, 28),
            TileType.notVisible->Color.white,
            TileType.ocean->Color.\iBLUE,
            TileType.plains->Color(0, 117, 0),
            TileType.temperateForest->Color(72, 250, 72),
            TileType.tundra->Color(153, 153, 153)
        },
        2->HashMap {
            TileType.desert->Color(249, 233, 28),
            TileType.jungle->Color(229, 46, 46),
            TileType.notVisible->Color.white,
            TileType.ocean->Color.\iBLUE,
            TileType.plains->Color(72, 218, 164),
            TileType.tundra->Color(153, 153, 153),
            TileType.steppe->Color(72, 100, 72)
        }
    };
    "Whether the given map version supports the given tile type."
    shared Boolean supportsType(Integer version, TileType type) {
        if (exists map = colors.get(version), exists color = map.get(type)) {
            return true;
        } else {
            return false;
        }
    }
    "Get the color to use for the given tile type in the given map version. Throws
     if the given version does not support that tile type."
    todo("Return null instead?")
    shared Color get(Integer version, TileType type) {
        if (exists map = colors.get(version)) {
            if (exists color = map.get(type)) {
                return color;
            } else {
                throw IllegalArgumentException(
                    "``type`` is not a terrain type version ``version`` can handle");
            }
        } else {
            throw IllegalArgumentException("Not a supported version");
        }
    }
    "Get a String (HTML) representation of the given terrain type."
    todo("Return null instead of throwing on unhandled types?")
    shared String getDescription(TileType type) {
        if (exists retval = descriptions.get(type)) {
            return retval;
        } else {
            throw IllegalArgumentException("No description for that type found");
        }
    }
    "Get the color that a fixture should turn the tile if it's not on top."
    todo("Return null instead of throwing on unhandled fixtures?")
    shared Color getFeatureColor(TileFixture fixture) {
        if (exists color = featureColors.get(type(fixture))) {
            return color;
        } else {
            throw IllegalArgumentException("Not a kind of fixture we can handle");
        }
    }
    "The color to use for background mountains."
    shared Color mountainColor = Color(249, 137, 28);
}
"The size of fixture icons."
Integer fixtureIconSize = 28;
"Create a very simple background icon for a terrain type"
Icon createTerrainIcon(TileType tileType) {
    BufferedImage retval = BufferedImage(fixtureIconSize, fixtureIconSize,
        BufferedImage.typeIntArgb);
    Graphics pen = retval.createGraphics();
    if (colorHelper.supportsType(2, tileType)) {
        pen.color = colorHelper.get(2, tileType);
    }
    pen.fillRect(0, 0, retval.width, retval.height);
    pen.dispose();
    return ImageIcon(retval);
}
"An icon cache."
MutableMap<String, Icon> iconCache = HashMap<String, Icon> {
    for (tileType in TileType.values())
        "``tileType.toXML()``.png"->createTerrainIcon(tileType)
};
"A cache of loaded images."
MutableMap<String, Image> imageCache = HashMap<String, Image>();
"Load an image from the cache, or if not in it, from file (and add it to the cache)"
Image loadImage(String file) {
    if (exists cached = imageCache.get(file)) {
        return cached;
    } else {
        try (res = ResourceInputStream("images/``file``")) {
            if (exists image = ImageIO.read(res)) {
                imageCache.put(file, image);
                return image;
            } else {
                throw IOException("No reader could read the images/``file``");
            }
        }
    }
}
"Load an icon from cache, or if not in the cache from file (adding it to the cache)"
Icon loadIcon(String file) {
    if (exists cached = iconCache.get(file)) {
        return cached;
    } else {
        Image orig = loadImage(file);
        BufferedImage temp = BufferedImage(fixtureIconSize, fixtureIconSize,
            BufferedImage.typeIntArgb);
        Graphics pen = temp.graphics;
        pen.drawImage(temp, 0, 0, temp.width, temp.height, null);
        pen.dispose();
        Icon icon = ImageIcon(temp);
        iconCache.put(file, icon);
        return icon;
    }
}
"A class to hold numeric constants useful for drawing tiles' contents."
class DrawingNumericConstants {
    "The part of a tile's width or height that a river's short dimension should occupy."
    shared static Float riverShortDimension = 1.0 / 8.0;
    "Where the short side of a river starts, along the edge of the tile."
    shared static Float riverShortStart = 7.0 / 16.0;
    "The part of a tile's width or height its long dimension should occupy."
    shared static Float riverLongDimension = 1.0 / 2.0;
    "The coordinates in an 'event' other than [[eventStart]], 0, and 100%."
    shared static Float eventOther = 1.0 / 2.0;
    "How far along a tile's dimension a lake should start."
    shared static Float lakeStart = 1.0 / 4.0;
    "How big a unit should be. Also its starting position (?)."
    shared static Float unitSize = 1.0 / 4.0;
    "How wide and tall a fortress should be."
    shared static Float fortSize = 1.0 / 3.0;
    "Where a fortress should start."
    shared static Float fortStart = 2.0 / 3.0;
    """Where an "event" should start."""
    shared static Float eventStart = 3.0 / 4.0;
    shared new () { }
}
"A fortress is drawn in brown."
Color fortColor = Color(160, 82, 45);
"Units are drawn in purple."
Color unitColor = Color(148, 0, 211);
"Events are drawn in pink."
Color eventColor = Color.pink;
"A class to do the drawing of a tile, whether on a component representing a single tile or
 a single-component map, using cached [[Shape]]s. Note that this is limited to version-1
 maps."
class CachingTileDrawHelper satisfies TileDrawHelper {
    static Float approximatelyZero = 0.000001;
    static Boolean areFloatsDifferent(Float first, Float second) =>
            (first - second).magnitude > approximatelyZero;
    shared new () {}
    "Shapes representing the rivers on the tile."
    MutableMap<River, Shape> rivers = HashMap<River, Shape>();
    "A cached copy of the background."
    variable Rectangle backgroundShape = Rectangle(0, 0, 1, 1);
    "Shape representing an event, or relative text, associated with the tile."
    variable Shape event = Line2D.Double();
    "Shape representing the fortress that might be on the tile."
    variable Shape fortress = event;
    "Shape representing the unit that might be on the tile."
    variable Shape unit = event;
    "Check, and possibly regenerate, the cache: regenerate if the width and height have
     changed."
    void updateCache(Integer width, Integer height) {
        if (areFloatsDifferent(backgroundShape.width, width.float) ||
                areFloatsDifferent(backgroundShape.height, height.float)) {
            backgroundShape = Rectangle(0, 0, width, height);
            rivers.clear();
            rivers.put(River.east, Rectangle2D.Double(
                width * DrawingNumericConstants.riverLongDimension,
                height * DrawingNumericConstants.riverShortStart,
                width * DrawingNumericConstants.riverLongDimension,
                height * DrawingNumericConstants.riverShortDimension));
            rivers.put(River.lake, Ellipse2D.Double(
                width * DrawingNumericConstants.lakeStart,
                height * DrawingNumericConstants.lakeStart,
                width * DrawingNumericConstants.riverLongDimension,
                height * DrawingNumericConstants.riverLongDimension));
            rivers.put(River.north, Rectangle2D.Double(
                width * DrawingNumericConstants.riverShortStart, 0.0,
                width * DrawingNumericConstants.riverShortDimension,
                height * DrawingNumericConstants.riverLongDimension));
            rivers.put(River.south, Rectangle2D.Double(
                width * DrawingNumericConstants.riverShortStart,
                height * DrawingNumericConstants.riverLongDimension,
                width * DrawingNumericConstants.riverShortDimension,
                height * DrawingNumericConstants.riverLongDimension));
            rivers.put(River.west, Rectangle2D.Double(0.0,
                height * DrawingNumericConstants.riverShortStart,
                width * DrawingNumericConstants.riverLongDimension,
                height * DrawingNumericConstants.riverShortDimension));
            fortress = Rectangle2D.Double(
                (width * DrawingNumericConstants.fortStart) - 1.0,
                (height * DrawingNumericConstants.fortStart) - 1.0,
                width * DrawingNumericConstants.fortSize,
                height * DrawingNumericConstants.fortSize);
            unit = Ellipse2D.Double(width * DrawingNumericConstants.unitSize,
                height * DrawingNumericConstants.unitSize,
                width * DrawingNumericConstants.unitSize,
                height * DrawingNumericConstants.unitSize);
            event = Polygon(
                createJavaIntArray({
                    halfEven(width * DrawingNumericConstants.eventStart)
                        .plus(approximatelyZero).integer,
                    halfEven(width * DrawingNumericConstants.eventOther)
                        .plus(approximatelyZero).integer, width}),
                createJavaIntArray({0,
                    halfEven(height * DrawingNumericConstants.eventOther)
                        .plus(approximatelyZero).integer,
                    halfEven(height * DrawingNumericConstants.eventOther)
                        .plus(approximatelyZero).integer}), 3);
        }
    }
    updateCache(2, 2);
    shared actual void drawTileTranslated(Graphics pen, IMapNG map, Point location,
            Integer width, Integer height) {
        assert (is Graphics2D pen);
        TileType terrain = map.getBaseTerrain(location);
        pen.color = colorHelper.get(map.dimensions().version, terrain);
        pen.fill(backgroundShape);
        pen.color = Color.black;
        pen.draw(backgroundShape);
        if (TileType.notVisible != terrain) {
            pen.color = Color.\iBLUE;
            for (river in map.getRivers(location)) {
                if (exists shape = rivers.get(river)) {
                    pen.fill(shape);
                }
                if (hasAnyForts(map, location)) {
                    pen.color = fortColor;
                    pen.fill(fortress);
                }
                if (hasAnyUnits(map, location)) {
                    pen.color = unitColor;
                    pen.fill(unit);
                }
                if (hasEvent(map, location)) {
                    pen.color = eventColor;
                    pen.fill(event);
                }
            }
        }
    }
    shared actual void drawTile(Graphics pen, IMapNG map, Point location,
            Coordinate coordinates, Coordinate dimensions) {
        Graphics context = pen.create(coordinates.x, coordinates.y, dimensions.x,
            dimensions.y);
        try {
            drawTileTranslated(context, map, location, dimensions.x, dimensions.y);
        } finally {
            context.dispose();
        }
    }
}
"A [[TileDrawHelper]] for version-1 maps that draws directly instead of creating Shapes,
 which proves more efficent in practice."
class DirectTileDrawHelper() satisfies TileDrawHelper {
    void drawRiver(Graphics pen, River river, Integer xCoordinate,
            Integer yCoordinate, Integer width, Integer height) {
        // TODO: Add some small number to floats before .integer?
        switch (river)
        case (River.east) {
            pen.fillRect(
                halfEven(width * DrawingNumericConstants.riverLongDimension)
                    .integer + xCoordinate,
                halfEven(height * DrawingNumericConstants.riverShortStart)
                    .integer + yCoordinate,
                halfEven(width * DrawingNumericConstants.riverLongDimension)
                    .integer,
                halfEven(height * DrawingNumericConstants.riverShortDimension)
                    .integer);
        }
        case (River.lake) {
            pen.fillOval(
                halfEven(width * DrawingNumericConstants.lakeStart).integer + xCoordinate,
                halfEven(height * DrawingNumericConstants.lakeStart).integer
                    + yCoordinate,
                halfEven(width * DrawingNumericConstants.riverLongDimension).integer,
                halfEven(height * DrawingNumericConstants.riverLongDimension).integer);
        }
        case (River.north) {
            pen.fillRect(
                halfEven(width * DrawingNumericConstants.riverShortStart).integer
                    + xCoordinate, yCoordinate,
                halfEven(width * DrawingNumericConstants.riverShortDimension).integer,
                halfEven(height * DrawingNumericConstants.riverLongDimension).integer);
        }
        case (River.south) {
            pen.fillRect(
                halfEven(width * DrawingNumericConstants.riverShortStart).integer
                    + xCoordinate,
                halfEven(height * DrawingNumericConstants.riverLongDimension).integer +
                    yCoordinate,
                halfEven(width * DrawingNumericConstants.riverShortDimension).integer,
                halfEven(height * DrawingNumericConstants.riverLongDimension).integer);
        }
        case (River.west) {
            pen.fillRect(xCoordinate,
                halfEven(height * DrawingNumericConstants.riverShortStart).integer +
                    yCoordinate,
                halfEven(width * DrawingNumericConstants.riverLongDimension).integer,
                halfEven(height * DrawingNumericConstants.riverShortDimension).integer);
        }
    }
    shared actual void drawTile(Graphics pen, IMapNG map, Point location,
            Coordinate coordinates, Coordinate dimensions) {
        Graphics context = pen.create();
        try {
            context.color = colorHelper.get(map.dimensions().version,
                map.getBaseTerrain(location));
            context.fillRect(coordinates.x, coordinates.y, dimensions.x, dimensions.y);
            context.color = Color.black;
            context.drawRect(coordinates.x, coordinates.y, dimensions.x, dimensions.y);
            if (TileType.notVisible == map.getBaseTerrain(location)) {
                return;
            }
            context.color = Color.\iBLUE;
            for (river in map.getRivers(location)) {
                drawRiver(context, river, coordinates.x, coordinates.y, dimensions.x,
                    dimensions.y);
            }
            if (hasAnyForts(map, location)) {
                context.color = fortColor;
                context.fillRect(
                    halfEven(dimensions.x * DrawingNumericConstants.fortStart - 1.0)
                        .integer + coordinates.x,
                    halfEven(dimensions.y * DrawingNumericConstants.fortStart - 1.0)
                        .integer + coordinates.y,
                    halfEven(dimensions.x * DrawingNumericConstants.fortSize).integer,
                    halfEven(dimensions.y * DrawingNumericConstants.fortSize).integer);
            }
            if (hasAnyUnits(map, location)) {
                context.color = unitColor;
                context.fillOval(
                    halfEven(dimensions.x * DrawingNumericConstants.unitSize).integer +
                        coordinates.x,
                    halfEven(dimensions.y * DrawingNumericConstants.unitSize).integer +
                        coordinates.y,
                    halfEven(dimensions.x * DrawingNumericConstants.unitSize).integer,
                    halfEven(dimensions.y * DrawingNumericConstants.unitSize).integer);
            } // Java version had else-if here, not just if
            if (hasEvent(map, location)) {
                context.color = eventColor;
                context.fillPolygon(
                    createJavaIntArray({
                        halfEven(dimensions.x *
                            DrawingNumericConstants.eventStart).integer + coordinates.x,
                        halfEven(dimensions.x *
                            DrawingNumericConstants.eventOther).integer + coordinates.x,
                        dimensions.x + coordinates.x}),
                    createJavaIntArray({coordinates.y,
                        halfEven(dimensions.y *
                            DrawingNumericConstants.eventOther).integer + coordinates.y,
                        halfEven(dimensions.y *
                            DrawingNumericConstants.eventOther).integer + coordinates.y}),
                    3);
            }
        } finally {
            context.dispose();
        }
    }
    shared actual void drawTileTranslated(Graphics pen, IMapNG map,
            Point location, Integer width, Integer height) =>
        drawTile(pen, map, location, PointFactory.coordinate(0, 0),
            PointFactory.coordinate(width, height));
}
"A version-1 tile-draw-helper."
TileDrawHelper verOneHelper = DirectTileDrawHelper(); // CachingTileDrawHelper();
"A [[TileDrawHelper]] for version-2 maps."
class Ver2TileDrawHelper(
        "The object to arrange to be notified as images finish drawing. In Java it's the
         [[ImageObserver]] interface, but we don't want to have to construct *objects*
         for this when a lambda will do."
        Boolean(Image, Integer, Integer, Integer, Integer, Integer) observer,
        "The object to query about whether to display a fixture."
        Boolean(TileFixture) filter,
        "A series of matchers to use to determine what's on top."
        {FixtureMatcher*} matchers) satisfies TileDrawHelper {
    "A comparator to put fixtures in order by the order of the first fixture that matches
     them."
    Comparison compareFixtures(TileFixture one, TileFixture two) {
        for (matcher in matchers) {
            if (matcher.matches(one)) {
                if (matcher.matches(two)) {
                    return equal;
                } else {
                    return smaller;
                }
            } else if (matcher.matches(two)) {
                return larger;
            }
        }
        return equal;
    }
    "Images we've already determined aren't there."
    MutableSet<String> missingFiles = HashSet<String>();
    "A mapping from river-sets to filenames."
    Map<Set<River>, String> riverFiles = HashMap<Set<River>, String> {
        HashSet<River> { }->"riv00.png", HashSet { River.north }->"riv01.png",
        HashSet { River.east }->"riv02.png", HashSet {River.south}->"riv03.png",
        HashSet {River.west}->"riv04.png", HashSet {River.lake}->"riv05.png",
        HashSet{River.north, River.east}->"riv06.png",
        HashSet{River.north,River.south}->"riv07.png",
        HashSet{River.north,River.west}->"riv08.png",
        HashSet{River.north,River.lake}->"riv09.png",
        HashSet{River.east,River.south}->"riv10.png",
        HashSet{River.east,River.west}->"riv11.png",
        HashSet{River.east,River.lake}->"riv12.png",
        HashSet{River.south,River.west}->"riv13.png",
        HashSet{River.south,River.lake}->"riv14.png",
        HashSet{River.west,River.lake}->"riv15.png",
        HashSet{River.north,River.east,River.south}->"riv16.png",
        HashSet{River.north,River.east,River.west}->"riv17.png",
        HashSet{River.north,River.east,River.lake}->"riv18.png",
        HashSet{River.north,River.south,River.west}->"riv19.png",
        HashSet{River.north,River.south,River.lake}->"riv20.png",
        HashSet{River.north,River.west,River.lake}->"riv21.png",
        HashSet{River.east,River.south,River.west}->"riv22.png",
        HashSet{River.east,River.south,River.lake}->"riv23.png",
        HashSet{River.east,River.west,River.lake}->"riv24.png",
        HashSet{River.south,River.west,River.lake}->"riv25.png",
        HashSet{River.north,River.east,River.south,River.west}->"riv26.png",
        HashSet{River.north,River.south,River.west,River.lake}->"riv27.png",
        HashSet{River.north,River.east,River.west,River.lake}->"riv28.png",
        HashSet{River.north,River.east,River.south,River.lake}->"riv29.png",
        HashSet{River.east,River.south,River.west,River.lake}->"riv30.png",
        HashSet{River.north,River.east,River.south,River.west,River.lake}->"riv31.png"
    };
    "Log, but otherwise ignore, file-not-found or other I/O error from loading an image."
    todo("Essentially inline this")
    void logLoadingError(IOException except,
            "The file we were trying to load from" String filename,
            "True if this was the fallback image (making this error more serious)"
            Boolean fallback) {
        if (except is FileNotFoundException || except is NoSuchFileException) {
            String message = "Image ``filename`` not found";
            if (fallback) {
                log.error(message, except);
            } else {
                log.info(message, except);
            }
        } else {
            log.error("I/O error while loading image ``filename``", except);
        }
    }
    for (file in {"trees.png", "mountain.png"}) {
        try {
            loadImage(file);
        } catch (IOException except) {
            logLoadingError(except, file, false);
        }
    }
    "Create the fallback image---made a method so the object reference can be immutable"
    Image createFallbackImage() {
        try {
            return loadImage("event_fallback.png");
        } catch (IOException except) {
            logLoadingError(except, "event_fallback.png", true);
            return BufferedImage(1, 1, BufferedImage.typeIntArgb);
        }
    }
    "A fallback image for when an image file is missing or fails to load."
    Image fallbackImage = createFallbackImage();
    """Get the color representing a "not-on-top" terrain fixture at the given location."""
    Color getFixtureColor(IMapNG map, Point location) {
        if (exists top = getTopFixture(map, location)) {
            if (exists topTerrain = getDrawableFixtures(map, location)
                    .filter((fixture) => fixture != top)
                    .filter((fixture) => fixture is TerrainFixture)) {
                assert (is TerrainFixture topTerrain);
                return colorHelper.getFeatureColor(topTerrain);
            } else if (map.isMountainous(location)) {
                return colorHelper.mountainColor;
            }
        }
        return colorHelper.get(map.dimensions().version,
            map.getBaseTerrain(location));
    }
    "Return either a loaded image or, if the specified image fails to load, the generic
     one."
    Image getImage(String filename) {
        try {
            return loadImage(filename);
        } catch (FileNotFoundException|NoSuchFileException except) {
            if (!missingFiles.contains(filename)) {
                log.error("images/``filename`` not found");
                log.debug("with stack trace", except);
                missingFiles.add(filename);
            }
            return fallbackImage;
        } catch (IOException except) {
            log.error("I/O error reading image images/``filename``", except);
            return fallbackImage;
        }
    }
    "Get the image representing the given fixture."
    Image getImageForFixture(TileFixture fixture) {
        if (is HasImage fixture) {
            String image = fixture.image;
            if (image.empty || missingFiles.contains(image)) {
                return getImage(fixture.defaultImage);
            } else {
                return getImage(image);
            }
        } else if (is RiverFixture fixture) {
            return getImage(riverFiles.get(fixture.rivers) else "");
        } else {
            log.warn("Using fallback image for unexpected kind of fixture");
            return fallbackImage;
        }
    }
    object observerWrapper satisfies ImageObserver {
        shared actual Boolean imageUpdate(Image img, Integer infoflags, Integer x,
            Integer y, Integer width, Integer height) =>
                observer(img, infoflags, x, y, width, height);
    }
    "Draw a tile at the specified coordinates. Because this is at present only called in
     a loop that's the last thing before the graphics context is disposed, we alter the
     state freely and don't restore it."
    shared actual void drawTile(Graphics pen, IMapNG map, Point location,
            Coordinate coordinates, Coordinate dimensions) {
        if (needsFixtureColor(map, location)) {
            pen.color = getFixtureColor(map, location);
        } else {
            pen.color = colorHelper.get(map.dimensions().version,
                map.getBaseTerrain(location));
        }
        pen.fillRect(coordinates.x, coordinates.y, dimensions.x, dimensions.y);
        if (!CeylonIterable(map.getRivers(location)).empty) {
            pen.drawImage(getRiverImage(CeylonIterable(map.getRivers(location))), coordinates.x,
                coordinates.y, dimensions.x, dimensions.y, observerWrapper);
        }
        if (exists top = getTopFixture(map, location)) {
            pen.drawImage(getImageForFixture(top), coordinates.x, coordinates.y,
                dimensions.x, dimensions.y, observerWrapper);
        } else if (map.isMountainous(location)) {
            pen.drawImage(getImage("mountain.png"), coordinates.x, coordinates.y,
                dimensions.x, dimensions.y, observerWrapper);
        }
        pen.color = Color.black;
        pen.drawRect(coordinates.x, coordinates.y, dimensions.x, dimensions.y);
    }
    "Draw a tile at the upper left corner of the drawing surface."
    shared actual void drawTileTranslated(Graphics pen, IMapNG map, Point location,
            Integer width, Integer height) =>
        drawTile(pen, map, location, PointFactory.coordinate(0, 0),
            PointFactory.coordinate(width, height));
    "The drawable fixtures at the given location."
    {TileFixture*} getDrawableFixtures(IMapNG map, Point location) {
        return {map.getGround(location), map.getForest(location),
            *map.getOtherFixtures(location)}.coalesced
            .filter((fixture) => !fixture is TileTypeFixture).filter(filter)
            .sort(compareFixtures);
    }
    "Get the image representing the given configuration of rivers."
    Image getRiverImage({River*} rivers) {
        if (is Set<River> rivers) {
            return getImage(riverFiles.get(rivers) else "");
        } else {
            return getImage(riverFiles.get(HashSet{rivers}) else "");
        }
    }
    """Get the "top" fixture at the given location"""
    TileFixture? getTopFixture(IMapNG map, Point location) =>
            getDrawableFixtures(map, location).first;
    """Whether there is a "terrain fixture" at the gtiven location."""
    Boolean hasTerrainFixture(IMapNG map, Point location) {
        if (getDrawableFixtures(map, location).any((fixture) => fixture is TerrainFixture)) {
            return true;
        } else if (getDrawableFixtures(map, location).first exists, map.isMountainous(location)) {
            return true;
        } else {
            return false;
        }
    }
    "Whether we need a different background color to show a non-top fixture (e.g. forest)
     at the given location"
    Boolean needsFixtureColor(IMapNG map, Point location) {
        if (hasTerrainFixture(map, location), exists top = getTopFixture(map, location)) {
            if (exists bottom = getDrawableFixtures(map, location).reduce((TileFixture? partial, element) => element)) {
                return top != bottom;
            } else if (map.isMountainous(location)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
"A factory method for [[TileDrawHelper]]s."
todo("split so ver-1 omits ZOF etc. and ver-2 requires it as non-null?")
TileDrawHelper tileDrawHelperFactory(
        "The version of the map that is to be drawn."
        Integer version,
        "The object to arrange to be notified as images finish drawing. In Java it's the
          [[ImageObserver]] interface, but we don't want to have to construct *objects*
          for this when a lambda will do."
        Boolean(Image, Integer, Integer, Integer, Integer, Integer) observer,
        "The filter to tell a version-two helper which fixtures to draw."
        Boolean(TileFixture)? zof,
        "A series of matchers to tell a version-two helper which fixture is on top"
        {FixtureMatcher*} matchers) {
    switch (version)
    case (1) { return verOneHelper; }
    case (2) {
        assert (exists zof);
        return Ver2TileDrawHelper(observer, zof, matchers);
    }
    else { throw IllegalArgumentException("Unsupported map version"); }
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
        IViewerModel model, Boolean(TileFixture) zof,
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
            .coalesced.filter(zof).sort(comparator);
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
"An interface for a UI representing a map."
todo("Is this needed anymore?")
interface MapGUI {
    "The driver model the GUI represents."
    shared formal IViewerModel mapModel;
}
"A map from key-codes for arrow keys and the numeric keypad to Strings we will use to
 represent them."
Map<Integer, String> arrowInputs = HashMap<Integer, String> {
        KeyEvent.vkUp->"up", KeyEvent.vkDown->"down", KeyEvent.vkRight->"right",
        KeyEvent.vkLeft->"left", KeyEvent.vkKpDown->"down", KeyEvent.vkNumpad2->"down",
        KeyEvent.vkKpRight->"right", KeyEvent.vkNumpad6->"right", KeyEvent.vkKpUp->"up",
        KeyEvent.vkNumpad8->"up", KeyEvent.vkKpLeft->"left", KeyEvent.vkNumpad4->"left",
        KeyEvent.vkNumpad9->"up-right", KeyEvent.vkNumpad7->"up-left",
        KeyEvent.vkNumpad3->"down-right", KeyEvent.vkNumpad1->"down-left"
};
Anything(T) join<T>(Anything(T) first, Anything(T) second) {
    void retval(T arg) {
        first(arg);
        second(arg);
    }
    return retval;
}
"A map from String srepresenting arrow-key key codes to the actions that should be mapped
 to them."
Map<String, Anything(DirectionSelectionChanger)> arrowActions =
        HashMap<String, Anything(DirectionSelectionChanger)> {
                "up"->DirectionSelectionChanger.up, "down"->DirectionSelectionChanger.down,
                "left"->DirectionSelectionChanger.left,
                "right"->DirectionSelectionChanger.right,
                "up-right"->join(DirectionSelectionChanger.up,
                    DirectionSelectionChanger.right),
                "up-left"->join(DirectionSelectionChanger.up,
                    DirectionSelectionChanger.left),
                "down-right"->join(DirectionSelectionChanger.down,
                    DirectionSelectionChanger.right),
                "down-left"->join(DirectionSelectionChanger.down,
                    DirectionSelectionChanger.left)
};
Iterable<Entry<Integer, String>> maybe(Boolean condition,
        Iterable<Entry<Integer, String>> ifTrue) {
    if (condition) {
        return ifTrue;
    } else {
        return {};
    }
}
"""A map from key-codes that are used, when modified with a platgform-specific modifier,
   for "jumping," to the Strings we'll use to represent them."""
Map<Integer, String> jumpInputs = HashMap<Integer, String> {
    KeyEvent.vkHome->"ctrl-home", KeyEvent.vkEnd->"ctrl-end",
    *maybe(platform.systemIsMac, {
        KeyEvent.vkUp->"home", KeyEvent.vkKpUp->"home", KeyEvent.vkNumpad8->"home",
        KeyEvent.vkDown->"end", KeyEvent.vkKpDown->"end", KeyEvent.vkNumpad2->"end",
        KeyEvent.vkLeft->"caret",KeyEvent.vkKpLeft->"caret", KeyEvent.vkNumpad4->"caret",
        KeyEvent.vkRight->"dollar", KeyEvent.vkKpRight->"dollar",
        KeyEvent.vkNumpad6->"dollar"})
};
"A map from other key-codes to the Strings we'll use to represent them"
Map<Integer, String> otherInputs = HashMap<Integer, String> {
    KeyEvent.vkHome->"home", KeyEvent.vk0->"home", KeyEvent.vkNumpad0->"home",
        KeyEvent.vkEnd->"end", KeyEvent.vkNumberSign->"end", KeyEvent.vkDollar->"dollar",
        KeyEvent.vkCircumflex->"caret",
        // TODO: Test that this works; Java used Character.getNumericValue('#')
        '#'.integer->"end",'^'.integer->"caret"
};
void repeat<T>(Anything(T) func, T args, Integer times) {
    for (i in 0..times) {
        func(args);
    }
}
void repeatVoid(Anything() func, Integer times) {
    for (i in 0..times) {
        func();
    }
}
void setUpArrowListeners(DirectionSelectionChanger selListener, InputMap inputMap,
        ActionMap actionMap) {
    class DirectionListener(Anything() action, Integer num = 1)
            extends ActionWrapper((ActionEvent event) => repeatVoid(action, num)) { }
    Integer fiveMask = (platform.systemIsMac) then InputEvent.altDownMask
        else InputEvent.ctrlDownMask;
    for (stroke->action in arrowInputs) {
        inputMap.put(KeyStroke.getKeyStroke(stroke, 0), action);
        inputMap.put(KeyStroke.getKeyStroke(stroke, fiveMask), "ctrl-``action``");
    }
    for (action->consumer in arrowActions) {
        actionMap.put(action, DirectionListener(() => consumer(selListener)));
        actionMap.put(action, DirectionListener(() => consumer(selListener), 5));
    }
    Integer jumpModifier = platform.shortcutMask;
    for (stroke->action in jumpInputs) {
        inputMap.put(KeyStroke.getKeyStroke(stroke, jumpModifier), action);
    }
    for (stroke->action in otherInputs) {
        inputMap.put(KeyStroke.getKeyStroke(stroke, 0), action);
    }
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.vk3, InputEvent.shiftDownMask), "end");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.vk6, InputEvent.shiftDownMask), "caret");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.vk4, InputEvent.shiftDownMask), "dollar");
    Anything() join(Anything(DirectionSelectionChanger) first,
            Anything(DirectionSelectionChanger) second) {
        void retval() {
            first(selListener);
            second(selListener);
        }
        return retval;
    }
    actionMap.put("ctrl-home", DirectionListener(join(DirectionSelectionChanger.jumpUp,
        DirectionSelectionChanger.jumpLeft)));
    actionMap.put("home", DirectionListener(() => selListener.jumpUp()));
    actionMap.put("ctrl-end", DirectionListener(join(DirectionSelectionChanger.jumpDown,
        DirectionSelectionChanger.jumpRight)));
    actionMap.put("end", DirectionListener(() => selListener.jumpDown()));
    actionMap.put("caret", DirectionListener(() => selListener.jumpLeft()));
    actionMap.put("dollar", DirectionListener(() => selListener.jumpRight()));
}
"A wrapper around `Boolean(TileFixture)`, used to determine Z-order of fixtures."
class FixtureMatcher {
    shared Boolean matches(TileFixture fixture);
    shared variable Boolean displayed = true;
    shared String description;
    shared new (Boolean(TileFixture) predicate, String desc) {
        matches = predicate;
        description = desc;
    }
    shared actual String string = "Matcher for ``description``";
}
FixtureMatcher simpleMatcher<T>(Boolean(T) method, String description) {
    Boolean predicate(TileFixture fixture) {
        if (is T fixture, method(fixture)) {
            return true;
        } else {
            return false;
        }
    }
    return FixtureMatcher(predicate, description);
}
"A class to allow the Z-order of fixtures to be represented as a table."
AbstractTableModel&Reorderable&ZOrderFilter&Iterable<FixtureMatcher>&Comparator<TileFixture> fixtureFilterTableModel() {
    FixtureMatcher trivialMatcher(ClassModel<TileFixture> type,
            String description = "``type.declaration.name``s") {
        return FixtureMatcher((TileFixture fixture) => type.typeOf(fixture), description);
    }
    FixtureMatcher immortalMatcher(SimpleImmortal.SimpleImmortalKind kind) {
        return FixtureMatcher((TileFixture fixture) {
            if (is SimpleImmortal fixture) {
                return fixture.kind() == kind;
            } else {
                return false;
            }
        }, kind.plural());
    }
    {FixtureMatcher*} complements<out T>(Boolean(T) method,
            String firstDescription, String secondDescription)
            given T satisfies TileFixture {
        return {simpleMatcher<T>(method, firstDescription),
            simpleMatcher<T>((T fixture) => !method(fixture),
                secondDescription)};
    }
    MutableList<FixtureMatcher> list = ArrayList<FixtureMatcher>();
    // Can't use our preferred initialization form because an Iterable can only be spread
    // as the *last* argument.
    for (arg in {
        // TODO: Maybe units should be broken up by owner?
        trivialMatcher(`Unit`), trivialMatcher(`Fortress`, "Fortresses"),
        // TODO: Towns should be broken up by kind or size, and maybe by status or owner
        trivialMatcher(`AbstractTown`, "Cities, Towns, and Fortifications"),
        // TODO: Village through Centaur were all 45, so their ordering happened by chance
        trivialMatcher(`Village`),
        immortalMatcher(SimpleImmortal.SimpleImmortalKind.troll),
        immortalMatcher(SimpleImmortal.SimpleImmortalKind.simurgh),
        immortalMatcher(SimpleImmortal.SimpleImmortalKind.ogre),
        immortalMatcher(SimpleImmortal.SimpleImmortalKind.minotaur),
        trivialMatcher(`Mine`),
        immortalMatcher(SimpleImmortal.SimpleImmortalKind.griffin),
        immortalMatcher(SimpleImmortal.SimpleImmortalKind.sphinx),
        immortalMatcher(SimpleImmortal.SimpleImmortalKind.phoenix),
        immortalMatcher(SimpleImmortal.SimpleImmortalKind.djinn),
        trivialMatcher(`Centaur`),
        // TODO: StoneDeposit through Animal were all 40; they too should be reviewed
        trivialMatcher(`StoneDeposit`, "Stone Deposits"),
        trivialMatcher(`MineralVein`, "Mineral Veins"),
        trivialMatcher(`Fairy`, "Fairies"), trivialMatcher(`Giant`),
        trivialMatcher(`Dragon`), trivialMatcher(`Cave`), trivialMatcher(`Battlefield`),
        complements<Animal>((Animal animal) => !animal.traces, "Animals", "Animal tracks"),
        complements<Grove>(Grove.orchard, "Orchards", "Groves"),
        // TODO: Rivers are usually handled specially, so should this really be included?
        trivialMatcher(`RiverFixture`, "Rivers"),
        // TODO: TextFixture thru AdventureFixture were all 25, and should be considered
        trivialMatcher(`TextFixture`, "Arbitrary-Text Notes"),
        trivialMatcher(`Portal`), trivialMatcher(`Oasis`, "Oases"),
        trivialMatcher(`AdventureFixture`, "Adventures"),
        trivialMatcher(`CacheFixture`, "Caches"), trivialMatcher(`Forest`),
        // TODO: Shrub and Meadow were both 15; consider
        trivialMatcher(`Shrub`), complements<Meadow>(Meadow.field, "Fields", "Meadows"),
        // TODO: Sandbar and Hill were both 5; consider
        trivialMatcher(`Sandbar`), trivialMatcher(`Hill`),
        complements<Ground>(Ground.exposed, "Ground (exposed)", "Ground")
    }) {
        if (is Iterable<FixtureMatcher> arg) {
            list.addAll(arg);
        } else {
            list.add(arg);
        }
    }
    object retval extends AbstractTableModel() satisfies Reorderable&ZOrderFilter&
            Iterable<FixtureMatcher>&Comparator<TileFixture> {
        shared actual Integer rowCount => list.size;
        shared actual Integer columnCount => 2;
        shared actual Object getValueAt(Integer rowIndex, Integer columnIndex) {
            if (exists matcher = list[rowIndex]) {
                switch (columnIndex)
                case (0) { return matcher.displayed; }
                case (1) { return matcher.description; }
                else { throw IllegalArgumentException("Only two columns"); }
            } else {
                throw IllegalArgumentException("Row out of bounds");
            }
        }
        shared actual String getColumnName(Integer column) {
            switch (column)
            case (0) { return "Visible"; }
            case (1) { return "Category"; }
            else { return super.getColumnName(column); }
        }
        shared actual JClass<out Object> getColumnClass(Integer columnIndex) {
            switch (columnIndex)
            case (0) { return javaClass<JBoolean>(); }
            case (1) { return javaClass<JString>(); }
            else { return javaClass<Object>(); }
        }
        shared actual Boolean isCellEditable(Integer rowIndex, Integer columnIndex) =>
                columnIndex == 0;
        shared actual void setValueAt(Object val, Integer rowIndex, Integer columnIndex) {
            if (columnIndex == 0, exists matcher = list[rowIndex]) {
                if (is Boolean val) {
                    matcher.displayed = val;
                    fireTableCellUpdated(rowIndex, 0);
                } else if (is JBoolean val) {
                    matcher.displayed = val.booleanValue();
                    fireTableCellUpdated(rowIndex, 0);
                }
            }
        }
        shared actual void reorder(Integer fromIndex, Integer toIndex) {
            if (fromIndex != toIndex) {
                list.move(fromIndex, toIndex);
                fireTableRowsDeleted(fromIndex, fromIndex);
                fireTableRowsInserted(toIndex, toIndex);
            }
        }
        shared actual Boolean shouldDisplay(TileFixture fixture) {
            for (matcher in list) {
                if (matcher.matches(fixture)) {
                    return matcher.displayed;
                }
            }
            ClassModel<TileFixture> cls = type(fixture);
            list.add(trivialMatcher(cls, fixture.plural()));
            Integer size = list.size;
            fireTableRowsInserted(size - 1, size - 1);
            return true;
        }
        shared actual Iterator<FixtureMatcher> iterator() => list.iterator();
        shared actual Comparison compare(TileFixture first, TileFixture second) {
            for (matcher in list) {
                if (!matcher.displayed) {
                    continue;
                }
                if (matcher.matches(first)) {
                    if (matcher.matches(second)) {
                        return equal;
                    } else {
                        return smaller;
                    }
                } else if (matcher.matches(second)) {
                    return larger;
                }
            }
            return equal;
        }
        shared actual Boolean equals(Object that) => (this of Identifiable).equals(that);
    }
    return retval;
}
"A component to display the map, even a large one, without the performance problems that
 came from drawing the entire map every time and letting Java manage the scrolling or,
 worse, instantiating a GUITile object for every visible tile every time the map was
 scrolled (or, yet worse again, a GUITile for every tile in the map, and removing them all
 and adding the visible tiles back in every time the map was scrolled)."
JComponent&MapGUI&MapChangeListener&SelectionChangeListener&GraphicalParamsListener
        mapComponent(IViewerModel model, Boolean(TileFixture) zof,
        Iterable<FixtureMatcher>&Comparator<TileFixture> matchers) {
    // FIXME: can't we drop this?
    object iobs satisfies ImageObserver {
        shared late ImageObserver wrapped;
        shared actual Boolean imageUpdate(Image? img, Integer infoflags, Integer x,
        Integer y, Integer width, Integer height) => wrapped.imageUpdate(img,
            infoflags, x, y, width, height);
    }
    MouseListener&ToolTipSource&SelectionChangeSource cml =
            componentMouseListener(model, zof, matchers.compare);
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
        variable TileDrawHelper helper = tileDrawHelperFactory(
            model.mapDimensions.version, imageUpdate, zof,
            matchers);
        doubleBuffered = true;
        shared actual IViewerModel mapModel = model;
        shared actual String? getToolTipText(MouseEvent event) =>
                cml.getToolTipText(event);
        shared actual void dimensionsChanged(VisibleDimensions oldDim,
            VisibleDimensions newDim) => repaint();
        void paintTile(Graphics pen, Point point, Integer row, Integer column,
                Boolean selected) {
            Integer tileSize = TileViewSize.scaleZoom(model.zoomLevel,
                model.mapDimensions.version);
            helper.drawTile(pen, model.map, point,
                PointFactory.coordinate(column * tileSize, row * tileSize),
                PointFactory.coordinate(tileSize, tileSize));
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
            helper = tileDrawHelperFactory(model.mapDimensions.version,
                imageUpdate, zof, matchers);
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
    setUpArrowListeners(dsl, inputMap, actionMap);
    object mapSizeListener extends ComponentAdapter() {
        shared actual void componentResized(ComponentEvent event) {
            Integer tileSize = TileViewSize.scaleZoom(model.zoomLevel,
                model.mapDimensions.version);
            Integer visibleColumns = event.component.width / tileSize;
            Integer visibleRows = event.component.height / tileSize;
            variable Integer minimumColumn = model.dimensions.minimumCol;
            variable Integer maximumColumn = model.dimensions.maximumCol;
            variable Integer minimumRow = model.dimensions.minimumRow;
            variable Integer maximumRow = model.dimensions.maximumRow;
            MapDimensions mapDimensions = model.mapDimensions;
            if (visibleColumns != (maximumColumn - minimumColumn) ||
                    visibleRows != (maximumRow - minimumRow)) {
                Integer totalColumns = mapDimensions.columns;
                if (visibleColumns >= totalColumns) {
                    minimumColumn = 0;
                    maximumColumn = totalColumns - 1;
                } else if (minimumColumn + visibleColumns >= totalColumns) {
                    maximumColumn = totalColumns - 1;
                    minimumColumn = totalColumns - visibleColumns - 2;
                } else {
                    maximumColumn = (minimumColumn + visibleColumns) - 1;
                }
                Integer totalRows = mapDimensions.rows;
                if (visibleRows >= totalRows) {
                    minimumRow = 0;
                    maximumRow = totalRows - 1;
                } else if ((minimumRow + visibleRows) >= totalRows) {
                    maximumRow = totalRows - 1;
                    minimumRow = totalRows - visibleRows - 2;
                } else {
                    maximumRow = minimumRow + visibleRows - 1;
                }
                model.dimensions = VisibleDimensions(minimumRow, maximumRow,
                    minimumColumn, maximumColumn);
            }
        }
        shared actual void componentShown(ComponentEvent event) =>
                componentResized(event);
    }
    retval.addComponentListener(mapSizeListener);
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
            return loadIcon(actualImage);
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
    JComponent keyElement(Integer version, TileType type) {
        JPanel&BoxPanel retval = boxPanel(BoxAxis.lineAxis);
        retval.addGlue();
        retval.addRigidArea(7);
        JPanel&BoxPanel panel = boxPanel(BoxAxis.pageAxis);
        panel.addRigidArea(4);
        Integer tileSize = TileViewSize.scaleZoom(ViewerModel.defZoomLevel, version);
        panel.add(KeyElementComponent(colorHelper.get(version, type), Dimension(4, 4),
            Dimension(8, 8), Dimension(tileSize, tileSize)));
        panel.addRigidArea(4);
        JLabel label = JLabel(colorHelper.getDescription(type));
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
                            portrait = loadImage(portraitName);
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
            menuHandler.register((event) {
                Point selection = model.selectedPoint;
                MapDimensions dimensions = model.mapDimensions;
                VisibleDimensions visible = model.dimensions;
                Integer topRow;
                if (selection.row - (visible.height / 2) <= 0) {
                    topRow = 0;
                } else if (selection.row + (visible.height / 2) >= dimensions.rows) {
                    topRow = dimensions.rows - visible.height;
                } else {
                    topRow = selection.row - (visible.height / 2);
                }
                Integer leftColumn;
                if (selection.col - (visible.width / 2) <= 0) {
                    leftColumn = 0;
                } else if (selection.col + (visible.width / 2) >= dimensions.columns) {
                    leftColumn = dimensions.columns - visible.width;
                } else {
                    leftColumn = selection.col - (visible.width / 2);
                }
                // Java version had topRow + dimensions.rows and
                // leftColumn + dimensions.columns as max row and column; this seems
                // plainly wrong.
                model.dimensions = VisibleDimensions(topRow, topRow + visible.height,
                    leftColumn, leftColumn + visible.width);
            }, "center");
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
