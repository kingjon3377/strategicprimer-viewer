import controller.map.misc {
    ICLIHelper,
    MenuBroker,
    WindowCloser
}
import model.misc {
    IDriverModel
}
import model.exploration {
    IExplorationModel,
    ExplorationModel,
    HuntingModel,
    PlayerListModel,
    ExplorationUnitListModel
}
import java.io {
    IOException
}
import javax.swing {
    SwingList=JList,
    DefaultListCellRenderer,
    ListCellRenderer,
    JLabel,
    JTextField,
    DefaultComboBoxModel,
    ComboBoxModel,
    JScrollPane,
    SwingUtilities,
    JPanel,
    KeyStroke,
    JComponent,
    JButton,
    AbstractAction,
    JOptionPane,
    ListModel
}
import view.util {
    SystemOut
}
import model.exploration.old {
    ExplorationRunner,
    EncounterTable,
    QuadrantTable,
    RandomTable,
    TerrainTable,
    LegacyTable,
    ConstantTable
}
import lovelace.util.common {
    todo
}
import ceylon.collection {
    HashSet,
    MutableSet,
    MutableList,
    LinkedList,
    ArrayList,
    Queue,
    MutableMap,
    HashMap
}
import java.lang {
    ObjectArray, JString=String,
    JInteger=Integer,
    IllegalArgumentException,
    IllegalStateException,
    IntArray
}
import strategicprimer.viewer.about {
    aboutDialog
}
import ceylon.file {
    File,
    Directory,
    parsePath
}
import ceylon.interop.java {
    JavaList,
    javaString,
    CeylonList,
    CeylonIterable,
    JavaIterable,
    createJavaIntArray
}
import util {
    ComparablePair,
    Pair,
    IsNumeric
}
import model.map {
    TileType,
    MapDimensions,
    MapDimensionsImpl,
    PointFactory,
    Point,
    TileFixture,
    Player,
    IMutableMapNG,
    HasOwner,
    IMapNG,
    PlayerImpl
}
import ceylon.test {
    assertEquals,
    test,
    assertThatException
}
import java.util.stream {
    Stream
}
import java.util {
    JList=List
}
import model.map.fixtures.mobile {
    IUnit,
    SimpleMovement,
    Animal
}
import model.listeners {
    MovementCostSource,
    MovementCostListener,
    CompletionListener,
    CompletionSource,
    SelectionChangeListener,
    SelectionChangeSupport,
    PlayerChangeSource,
    PlayerChangeListener,
    SelectionChangeSource
}
import model.map.fixtures {
    Ground
}
import model.map.fixtures.terrain {
    Forest
}
import model.map.fixtures.resources {
    CacheFixture
}
import java.awt.event {
    ActionListener,
    KeyEvent,
    ActionEvent
}
import java.awt {
    Dimension,
    CardLayout,
    Component,
    GridLayout,
    Graphics,
    Polygon
}
import java.text {
    NumberFormat
}
import model.viewer {
    FixtureMatcher,
    FixtureFilterTableModel,
    FixtureListModel,
    TileTypeFixture
}
import javax.swing.text {
    BadLocationException,
    Document
}
import javax.swing.event {
    ListSelectionEvent
}
import lovelace.util.jvm {
    listenedButton,
    ImprovedComboBox,
    boxPanel,
    BoxAxis,
    BorderedPanel,
    horizontalSplit,
    verticalSplit,
    shuffle,
    ListModelWrapper,
    createHotKey,
    FormattedLabel
}
import view.map.main {
    TileDrawHelperFactory,
    TileDrawHelper
}
import model.map.fixtures.towns {
    Village
}
import com.bric.window {
    WindowMenu
}
"The logic split out of [[explorationCLI]]"
class ExplorationCLIHelper(IExplorationModel model, ICLIHelper cli)
        satisfies MovementCostSource {
    HuntingModel huntingModel = HuntingModel(model.map);
    MutableList<Anything(Integer)|MovementCostListener> listeners = ArrayList<Anything(Integer)|MovementCostListener>();
    shared actual void addMovementCostListener(MovementCostListener listener) =>
            listeners.add(listener);
    shared actual void removeMovementCostListener(MovementCostListener listener) =>
            listeners.remove(listener);
    void fireMovementCost(Integer cost) {
        for (listener in listeners) {
            if (is MovementCostListener listener) {
                listener.deduct(cost);
            } else {
                listener(cost);
            }
        }
    }
    "Have the user choose a player."
    shared Player? choosePlayer() {
        JList<Player> players = model.playerChoices;
        Integer playerNum = cli.chooseFromList(players,
            "Players shared by all the maps:", "No players shared by all the maps:",
            "Chosen player: ", true);
        return CeylonList(players).get(playerNum);
    }
    "Have the user choose a unit belonging to that player."
    shared IUnit? chooseUnit(Player player) {
        JList<IUnit> units = model.getUnits(player);
        Integer unitNum = cli.chooseFromList(units, "Player's units:",
            "That player has no units in the master map", "Chosen unit: ", true);
        return CeylonList(units).get(unitNum);
    }
    "The explorer's current movement speed."
    variable IExplorationModel.Speed speed = IExplorationModel.Speed.normal;
    "Let the user change the explorer's speed"
    void changeSpeed() {
//        IExplorationModel.Speed[] speeds = `IExplorationModel.Speed`.caseValues;
        IExplorationModel.Speed[] speeds = [*IExplorationModel.Speed.values()];
        Integer newSpeed = cli.chooseFromList(
            JavaList(speeds),
            "Possible Speeds:", "No speeds available", "Chosen Speed: ", true);
        if (exists temp = speeds[newSpeed]) {
            speed = temp;
        }
    }
    "Copy the given fixture to subordinate maps and print it to the output stream."
    void printAndTransferFixture(Point destPoint, TileFixture? fixture, HasOwner mover) {
        if (exists fixture) {
            cli.println(fixture.string);
            Boolean zero;
            if (is HasOwner fixture, fixture.owner != mover.owner) {
                zero = true;
            } else {
                zero = false;
            }
            for (pair in model.subordinateMaps) {
                IMutableMapNG map = pair.first();
                if (is Ground fixture, !map.getGround(destPoint) exists) {
                    map.setGround(destPoint, fixture.copy(false));
                } else if (is Forest fixture, !map.getForest(destPoint) exists) {
                    map.setForest(destPoint, fixture.copy(false));
                } else {
                    map.addFixture(destPoint, fixture.copy(zero));
                }
            }
            if (is CacheFixture fixture) {
                model.map.removeFixture(destPoint, fixture);
            }
        }
    }
    "Have the player move the selected unit. Throws an assertion-error exception if no
     unit is selected. Movement cost is reported by the driver model to all registered
     MovementCostListeners, while any additional costs for non-movement actions are
     reported by this class, so a listener should be attached to both."
    shared void move() {
        assert (exists mover = model.selectedUnit);
        Integer directionNum = cli.inputNumber("Direction to move: ");
        if (directionNum == 9) {
            changeSpeed();
            return;
        } else if (directionNum > 9) {
            fireMovementCost(JInteger.maxValue);
            return;
        } else if (directionNum < 0) {
            return;
        }
        assert (exists direction = IExplorationModel.Direction.values()[directionNum]);
        Point point = model.selectedUnitLocation;
        Point destPoint = model.getDestination(point, direction);
        try {
            model.move(direction, speed);
        } catch (SimpleMovement.TraversalImpossibleException except) {
            log.debug("Attempted movement to impossible destination");
            cli.println("That direction is impassable; we've made sure all maps show that
                         at a cost of 1 MP");
            return;
        }
        MutableList<TileFixture> constants = ArrayList<TileFixture>();
        IMutableMapNG map = model.map;
        MutableList<TileFixture> allFixtures = ArrayList<TileFixture>();
        for (fixture in {map.getGround(destPoint), map.getForest(destPoint),
                *CeylonIterable(map.getOtherFixtures(destPoint))}.coalesced) {
            if (SimpleMovement.shouldAlwaysNotice(mover, fixture)) {
                constants.add(fixture);
            } else if (SimpleMovement.shouldSometimesNotice(mover, speed, fixture)) {
                allFixtures.add(fixture);
            }
        }
        String tracks;
        if (TileType.ocean == model.map.getBaseTerrain(destPoint)) {
            tracks = huntingModel.fish(destPoint, 1).get(0).string;
        } else {
            tracks = huntingModel.hunt(destPoint, 1).get(0).string;
        }
        if (HuntingModel.nothing != tracks) {
            allFixtures.add(Animal(tracks, true, false, "wild", -1));
        }
        if (IExplorationModel.Direction.nowhere == direction) {
            if (cli.inputBooleanInSeries(
                    "Should any village here swear to the player?  ")) {
                model.swearVillages();
            }
            if (cli.inputBooleanInSeries("Dig to expose some ground here? ")) {
                model.dig();
            }
        }
        String mtn;
        if (map.isMountainous(destPoint)) {
            mtn = "mountainous ";
            for (pair in model.subordinateMaps) {
                pair.first().setMountainous(destPoint, true);
            }
        } else {
            mtn = "";
        }
        cli.println("The explorer comes to ``destPoint``, a ``mtn``tile with terrain ``
            map.getBaseTerrain(destPoint)``");
        {TileFixture*} noticed = CeylonIterable(
            SimpleMovement.selectNoticed(JavaList(allFixtures), identity<TileFixture>,
                mover, speed));
        if (noticed.empty) {
            cli.println("The following were automatically noticed:");
        } else if (noticed.size > 1) {
            cli.println(
                "The following were noticed, all but the last ``noticed
                    .size`` automatically:");
        } else {
            cli.println("The following were noticed, all but the last automatically:");
        }
        constants.addAll(noticed);
        for (fixture in constants) {
            printAndTransferFixture(destPoint, fixture, mover);
        }
    }
    "Ask the user for directions the unit should move until it runs out of MP or the user
      decides to quit."
    todo("Inline back into [[explorationCLI]]?")
    shared void moveUntilDone() {
        if (exists mover = model.selectedUnit) {
            cli.println("Details of the unit:");
            cli.println(mover.verbose());
            Integer totalMP = cli.inputNumber("MP the unit has: ");
            variable Integer movement = totalMP;
            void handleCost(Integer cost) {
                movement -= cost;
            }
            model.addMovementCostListener(handleCost);
//            addMovementCostListener(handleCost);
            listeners.add(handleCost);
            while (movement > 0) {
                cli.println("``movement`` MP of ``totalMP`` remaining.");
                cli.println("Current speed: ``speed.name``");
                cli.println("""0 = N, 1 = NE, 2 = E, 3 = SE, 4 = S, 5 = SW, 6 = W, 7 = NW,
                               8 = Stay Here, 9 = Change Speed, 10 = Quit.""");
                move();
            }
        } else {
            cli.println("No unit is selected");
        }
    }
}
"A button (visually) representing a tile in two maps."
class DualTileButton(IMapNG master, IMapNG subordinate, {FixtureMatcher*} matchers)
        extends JButton() {
    Integer margin = 2;
    variable Point localPoint = PointFactory.invalidPoint;
    shared Point point => localPoint;
    assign point {
        localPoint = point;
        repaint();
    }
    TileDrawHelper helper = TileDrawHelperFactory.instance.factory(2, super.imageUpdate,
        (TileFixture fix) => true, JavaIterable(matchers).iterator);
    shared actual void paintComponent(Graphics pen) {
        super.paintComponent(pen);
        pen.clip = Polygon(createJavaIntArray({width - margin, margin, margin}),
            createJavaIntArray({margin, height - margin, margin}), 3);
        helper.drawTileTranslated(pen, master, point, width, height);
        pen.clip = Polygon(createJavaIntArray({width - margin, width - margin, margin}),
            createJavaIntArray({margin, height - margin, height - margin}), 3);
        helper.drawTileTranslated(pen, subordinate, point, width, height);
    }
}
"A CLI to help running exploration."
object explorationCLI satisfies SimpleCLIDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        shortOption = "-x";
        longOption = "--explore";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Run exploration.";
        longDescription = "Move a unit around the map, updating the player's map with what it sees.";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options, IDriverModel model) {
        IExplorationModel explorationModel;
        if (is IExplorationModel model) {
            explorationModel = model;
        } else {
            explorationModel = ExplorationModel(model);
        }
        try {
            ExplorationCLIHelper eCLI = ExplorationCLIHelper(explorationModel, cli);
            if (exists player = eCLI.choosePlayer(), exists unit = eCLI.chooseUnit(player)) {
                explorationModel.selectUnit(unit);
                eCLI.moveUntilDone();
            }
        } catch (IOException except) {
            throw DriverFailedException(except, "I/O error interacting with user");
        }
    }
}
"The main window for the exploration GUI."
SPFrame explorationFrame(IExplorationModel model,
        Anything(ActionEvent) menuHandler) {
    Map<IExplorationModel.Direction, KeyStroke> arrowKeys = HashMap {
        IExplorationModel.Direction.north->KeyStroke.getKeyStroke(KeyEvent.vkUp, 0),
        IExplorationModel.Direction.south->KeyStroke.getKeyStroke(KeyEvent.vkDown, 0),
        IExplorationModel.Direction.west->KeyStroke.getKeyStroke(KeyEvent.vkLeft, 0),
        IExplorationModel.Direction.east->KeyStroke.getKeyStroke(KeyEvent.vkRight, 0)
    };
    Map<IExplorationModel.Direction, KeyStroke> numKeys = HashMap {
        IExplorationModel.Direction.north->KeyStroke.getKeyStroke(KeyEvent.vkNumpad8, 0),
        IExplorationModel.Direction.south->KeyStroke.getKeyStroke(KeyEvent.vkNumpad2, 0),
        IExplorationModel.Direction.west->KeyStroke.getKeyStroke(KeyEvent.vkNumpad4, 0),
        IExplorationModel.Direction.east->KeyStroke.getKeyStroke(KeyEvent.vkNumpad6, 0),
        IExplorationModel.Direction.northeast->KeyStroke.getKeyStroke(KeyEvent.vkNumpad9,
            0),
        IExplorationModel.Direction.northwest->KeyStroke.getKeyStroke(KeyEvent.vkNumpad7,
            0),
        IExplorationModel.Direction.southeast->KeyStroke.getKeyStroke(KeyEvent.vkNumpad3,
            0),
        IExplorationModel.Direction.southwest->KeyStroke.getKeyStroke(KeyEvent.vkNumpad1,
            0),
        IExplorationModel.Direction.nowhere->KeyStroke.getKeyStroke(KeyEvent.vkNumpad5, 0)
    };
    object retval extends SPFrame("Exploration", model.mapFile.orElse(null),
            Dimension(768, 480)) {
        shared actual String windowName = "Exploration";
        CardLayout layoutObj = CardLayout();
        setLayout(layoutObj);
        JTextField mpField = JTextField(5);
        ExplorationUnitListModel unitListModel =
                ExplorationUnitListModel(model);
        SwingList<IUnit> unitList = ConstructorWrapper.jlist<IUnit>(unitListModel);
        PlayerListModel playerListModel = PlayerListModel(model);
        SwingList<Player> playerList = SwingList<Player>(playerListModel);
        MutableList<CompletionListener> completionListeners =
                ArrayList<CompletionListener>();
        void buttonListener(ActionEvent event) {
            if (exists selectedValue = unitList.selectedValue,
                !unitList.selectionEmpty) {
                model.selectUnit(selectedValue);
                for (listener in completionListeners) {
                    listener.finished();
                }
            }
        }
        shared ComboBoxModel<IExplorationModel.Speed> speedModel =
                DefaultComboBoxModel<IExplorationModel.Speed>(
                    IExplorationModel.Speed.values());
        object explorerSelectingPanel extends BorderedPanel()
                satisfies PlayerChangeSource&CompletionSource {
            MutableList<PlayerChangeListener> listeners =
                    ArrayList<PlayerChangeListener>();
            shared Document mpDocument => mpField.document;
            shared actual void addPlayerChangeListener(PlayerChangeListener listener) =>
                    listeners.add(listener);
            shared actual void removePlayerChangeListener(PlayerChangeListener listener)
                    => listeners.remove(listener);
            shared actual void addCompletionListener(CompletionListener listener) =>
                    completionListeners.add(listener);
            shared actual void removeCompletionListener(CompletionListener listener) =>
                    completionListeners.remove(listener);
            model.addMapChangeListener(playerListModel);
            playerList.addListSelectionListener((ListSelectionEvent event) {
                if (!playerList.selectionEmpty,
                        exists newPlayer = playerList.selectedValue) {
                    for (listener in listeners) {
                        listener.playerChanged(null, newPlayer);
                    }
                }
            });
            addPlayerChangeListener(unitListModel);
            DefaultListCellRenderer defaultRenderer = DefaultListCellRenderer();
            object renderer satisfies ListCellRenderer<IUnit> {
                shared actual Component getListCellRendererComponent(
                        SwingList<out IUnit>? list, IUnit? val, Integer index,
                        Boolean isSelected, Boolean cellHasFocus) {
                    Component retval = defaultRenderer.getListCellRendererComponent(list,
                        val, index, isSelected, cellHasFocus);
                    if (exists val, is JLabel retval) {
                        retval.text = "Unit of type ``val.kind``, named ``val.name``";
                    }
                    return retval;
                }
            }
            unitList.cellRenderer = renderer;
            mpField.addActionListener(buttonListener);
            speedModel.selectedItem = IExplorationModel.Speed.normal;
        }
        explorerSelectingPanel.center = horizontalSplit(0.5, 0.5,
            BorderedPanel.verticalPanel(JLabel("Players in all maps:"), playerList,
                null),
            BorderedPanel.verticalPanel(JLabel(
                """<html><body><p>Units belonging to that player:</p>
                   <p>(Selected unit will be used for exploration.)</p>
                   </body></html>"""),
                JScrollPane(unitList), BorderedPanel.verticalPanel(
                    BorderedPanel.horizontalPanel(JLabel("Unit's Movement Points"),
                        null, mpField),
                    BorderedPanel.horizontalPanel(JLabel("Unit's Relative Speed"),
                        null, ImprovedComboBox<IExplorationModel.Speed>.withModel(
                            speedModel)),
                    listenedButton("Start exploring!", buttonListener))));
        JPanel tilesPanel = JPanel(GridLayout(3, 12, 2, 2));
        JPanel headerPanel = boxPanel(BoxAxis.lineAxis);
        object explorationPanel extends BorderedPanel()
                satisfies SelectionChangeListener&CompletionSource&MovementCostListener {
            Document mpDocument = explorerSelectingPanel.mpDocument;
            NumberFormat numParser = NumberFormat.integerInstance;
            shared actual void deduct(Integer cost) {
                String mpText;
                try {
                    mpText = mpDocument.getText(0, mpDocument.length).trimmed;
                } catch (BadLocationException except) {
                    log.error("Exception trying to update MP counter", except);
                    return;
                }
                if (IsNumeric.isNumeric(mpText)) {
                    variable Integer movePoints;
                    try {
                        movePoints = numParser.parse(mpText).intValue();
                    } catch (ParseException except) {
                        log.error("Non-numeric data in movement-points field", except);
                        return;
                    }
                    movePoints -= cost;
                    try {
                        mpDocument.remove(0, mpDocument.length);
                        mpDocument.insertString(0, movePoints.string, null);
                    } catch (BadLocationException except) {
                        log.error("Exception trying to update MP counter", except);
                    }
                }
            }
            FormattedLabel locLabel = FormattedLabel(
                "<html><body>Currently exploring (%d, %d); click a tile to explore it.
                 Selected fixtures in its left-hand list will be 'discovered'.
                 </body></html>", JInteger(-1), JInteger(-1));
            MutableMap<IExplorationModel.Direction, SelectionChangeSupport> mains =
                    HashMap<IExplorationModel.Direction, SelectionChangeSupport>();
            MutableMap<IExplorationModel.Direction, SelectionChangeSupport> seconds =
                    HashMap<IExplorationModel.Direction, SelectionChangeSupport>();
            MutableMap<IExplorationModel.Direction, DualTileButton> buttons =
                    HashMap<IExplorationModel.Direction, DualTileButton>();
            {FixtureMatcher*} matchers = CeylonIterable(FixtureFilterTableModel());
            shared actual void selectedPointChanged(Point? old, Point newPoint) {
                // TODO: use the provided old point instead?
                Point selPoint = model.selectedUnitLocation;
//                for (direction in `IExplorationModel.Direction`.caseValues) {
                for (direction in IExplorationModel.Direction.values()) {
                    Point point = model.getDestination(selPoint, direction);
                    mains.get(direction)?.fireChanges(selPoint, point);
                    seconds.get(direction)?.fireChanges(selPoint, point);
                    if (exists button = buttons.get(direction)) {
                        button.point = point;
                        button.repaint();
                    }
                }
                locLabel.setArgs(selPoint.row, selPoint.col);
            }
            MutableList<CompletionListener> completionListeners =
                    ArrayList<CompletionListener>();
            shared actual void addCompletionListener(CompletionListener listener) =>
                    completionListeners.add(listener);
            shared actual void removeCompletionListener(CompletionListener listener) =>
                    completionListeners.remove(listener);
            headerPanel.add(listenedButton("Select a different explorer",
                (ActionEvent event) {
                    for (listener in completionListeners) {
                        listener.finished();
                    }
                }));
            headerPanel.add(locLabel);
            headerPanel.add(JLabel("Remaining Movement Points:"));
            JTextField mpField = JTextField(explorerSelectingPanel.mpDocument, null, 5);
            mpField.maximumSize = Dimension(JInteger.maxValue,
                mpField.preferredSize.height.integer);
            headerPanel.add(mpField);
            headerPanel.add(JLabel("Current relative speed:"));
            IExplorationModel.Speed() speedSource = () {
                assert (is IExplorationModel.Speed retval = speedModel.selectedItem);
                return retval;
            };
            headerPanel.add(ImprovedComboBox<IExplorationModel.Speed>.withModel(
                speedModel));
            IMutableMapNG secondMap;
            if (exists pair = CeylonIterable(model.subordinateMaps).first) {
                secondMap = pair.first();
            } else {
                secondMap = model.map;
            }
            for (direction in {IExplorationModel.Direction.northwest,
                    IExplorationModel.Direction.north,
                    IExplorationModel.Direction.northeast,
                    IExplorationModel.Direction.west, IExplorationModel.Direction.nowhere,
                    IExplorationModel.Direction.east,
                    IExplorationModel.Direction.southwest,
                    IExplorationModel.Direction.south,
                    IExplorationModel.Direction.southeast}) {
                SelectionChangeSupport mainPCS = SelectionChangeSupport();
                SwingList<TileFixture>&SelectionChangeListener mainList =
                        fixtureList(tilesPanel, FixtureListModel(model.map, true),
                            CeylonIterable(model.map.players()));
                mainPCS.addSelectionChangeListener(mainList);
                tilesPanel.add(JScrollPane(mainList));
                DualTileButton dtb = DualTileButton(model.map, secondMap,
                    matchers);
                // At some point we tried wrapping the button in a JScrollPane.
                tilesPanel.add(dtb);
                object ecl extends AbstractAction()
                        satisfies MovementCostSource&SelectionChangeSource {
                    MutableList<MovementCostListener> movementListeners =
                            ArrayList<MovementCostListener>();
                    MutableList<SelectionChangeListener> selectionListeners =
                            ArrayList<SelectionChangeListener>();
                    shared actual void addSelectionChangeListener(
                        SelectionChangeListener listener) =>
                            selectionListeners.add(listener);
                    shared actual void removeSelectionChangeListener(
                        SelectionChangeListener listener) =>
                            selectionListeners.remove(listener);
                    shared actual void addMovementCostListener(
                        MovementCostListener listener) => movementListeners.add(listener);
                    shared actual void removeMovementCostListener(
                        MovementCostListener listener) =>
                            movementListeners.remove(listener);
                    MutableList<TileFixture> selectedValuesList {
                        IntArray selections = mainList.selectedIndices;
                        ListModel<TileFixture> listModel = mainList.model;
                        MutableList<TileFixture> retval = ArrayList<TileFixture>();
                        for (index in selections) {
                            if (index < listModel.size) {
                                assert (exists item = listModel.getElementAt(index));
                                retval.add(item);
                            } else {
                                assert (exists item = listModel.getElementAt(
                                    listModel.size - 1));
                                retval.add(item);
                            }
                        }
                        return retval;
                    }
                    "A list of things the explorer can do: pairs of explanations (in the
                     form of questions to ask the user to see if the explorer does them)
                     and references to methods for doing them."
                    {[String, Anything()]*} explorerActions = {["Should the explorer swear any villages on this tile?", () {
                        model.swearVillages();
                        for (fixture in model.map.getOtherFixtures(model.selectedUnitLocation)) {
                            if (is Village fixture) {
                                selectedValuesList.add(fixture);
                            }
                        }
                    }], ["Should the explorer dig to find what kind of ground is here?",
                        model.dig]};
                    shared actual void actionPerformed(ActionEvent event) =>
                            SwingUtilities.invokeLater(() {
                                try {
                                    value fixtures = selectedValuesList;
                                    if (IExplorationModel.Direction.nowhere == direction) {
                                        for ([query, method] in explorerActions) {
                                            Integer resp =
                                                    JOptionPane.showConfirmDialog(null,
                                                        query);
                                            if (resp == JOptionPane.cancelOption) {
                                                return;
                                            } else if (resp == JOptionPane.yesOption) {
                                                method();
                                            }
                                        }
                                    }
                                    model.move(direction, speedSource());
                                    Point destPoint = model.selectedUnitLocation;
                                    Player player = model.selectedUnit ?. owner else
                                        PlayerImpl(- 1, "no-one");
                                    MutableSet<CacheFixture> caches = HashSet<CacheFixture>();
                                    for (pair in model.subordinateMaps) {
                                        IMutableMapNG map = pair.first();
                                        map.setBaseTerrain(destPoint, model.map
                                            .getBaseTerrain(destPoint));
                                        for (fixture in fixtures) {
                                            if (is TileTypeFixture fixture) {
                                                // Skip it! It'll corrupt the output XML!
                                                continue ;
                                            } else if (is Ground fixture,
                                                !map.getGround(destPoint) exists) {
                                                map.setGround(destPoint, fixture.copy(false));
                                            } else if (is Forest fixture,
                                                !map.getForest(destPoint) exists) {
                                                map.setForest(destPoint, fixture.copy(false));
                                            } else if (map.streamAllFixtures(destPoint)
                                                .noneMatch((that) => fixture == that)) {
                                                Boolean zero;
                                                if (is HasOwner fixture, fixture.owner != player) {
                                                    zero = true;
                                                } else {
                                                    zero = false;
                                                }
                                                map.addFixture(destPoint,
                                                    fixture.copy(zero));
                                                if (is CacheFixture fixture) {
                                                    caches.add(fixture);
                                                }
                                            }
                                        }
                                    }
                                    for (cache in caches) {
                                        model.map.removeFixture(destPoint, cache);
                                    }
                                } catch (SimpleMovement.TraversalImpossibleException
                                except) {
                                    log.debug("Attempted movement to impassable destination",
                                        except);
                                    Point selection = model.selectedUnitLocation;
                                    for (listener in selectionListeners) {
                                        listener.selectedPointChanged(null, selection);
                                    }
                                    for (listener in movementListeners) {
                                        listener.deduct(1);
                                    }
                                }
                            });
                }
                createHotKey(dtb, direction.string, ecl, JComponent.whenInFocusedWindow,
                    *{arrowKeys.get(direction), numKeys.get(direction)}.coalesced);
                dtb.addActionListener(ecl);
                ecl.addSelectionChangeListener(selectedPointChanged);
                ecl.addMovementCostListener(deduct);
                """A list-data-listener to select a random but suitable set of fixtures to
                    be "discovered" if the tile is explored."""
                object ell satisfies SelectionChangeListener {
                    "A list of animal-tracks objects, which we want to remove from the
                     main map whenever the list's target gets changed."
                    MutableList<[Point, Animal]> tracks = ArrayList<[Point, Animal]>();
                    """A "hunting model," to get the animals to have traces of."""
                    HuntingModel huntingModel = HuntingModel(model.map);
                    variable Boolean outsideCritical = true;
                    shared actual void selectedPointChanged(Point? old, Point newPoint) {
                        SwingUtilities.invokeLater(() {
                            if (outsideCritical, exists selectedUnit =
                                    model.selectedUnit) {
                                outsideCritical = false;
                                for ([location, animal] in tracks) {
                                    model.map.removeFixture(location, animal);
                                }
                                tracks.clear();
                                mainList.clearSelection();
                                MutableList<[Integer, TileFixture]> constants =
                                        ArrayList<[Integer, TileFixture]>();
                                MutableList<[Integer, TileFixture]> possibles =
                                        ArrayList<[Integer, TileFixture]>();
                                for (index->fixture in ListModelWrapper(mainList.model).indexed) {
                                    if (SimpleMovement.shouldAlwaysNotice(selectedUnit, fixture)) {
                                        constants.add([index, fixture]);
                                    } else if (SimpleMovement.shouldSometimesNotice(selectedUnit, speedSource(), fixture)) {
                                        possibles.add([index, fixture]);
                                    }
                                }
                                Point currentLocation = model.selectedUnitLocation;
                                if (currentLocation.valid) {
                                    String?(HuntingModel) tracksSource;
                                    // FIXME: Once HuntingModel is ported, to produce Ceylon
                                    // rather than Java list, move as much as possible out
                                    // of lambdas.
                                    if (TileType.ocean == model.map.getBaseTerrain(currentLocation)) {
                                        tracksSource = (HuntingModel hmodel) => CeylonList(hmodel.fish(currentLocation, 1)).map(Object.string).first;
                                    } else {
                                        tracksSource = (HuntingModel hmodel) => CeylonList(hmodel.hunt(currentLocation, 1)).map(Object.string).first;
                                    }
                                    assert (exists possibleTracks = tracksSource(huntingModel));
                                    if (HuntingModel.\inothing != possibleTracks) {
                                        Animal animal = Animal(possibleTracks, true, false, "wild", -1);
                                        assert (is FixtureListModel listModel = mainList.model);
                                        Integer index = listModel.size;
                                        listModel.addFixture(animal);
                                        possibles.add([index, animal]);
                                        tracks.add([currentLocation, animal]);
                                    }
                                }
                                constants.addAll(CeylonList(SimpleMovement.selectNoticed(
                                    JavaList(shuffle(possibles)),
                                    ([Integer, TileFixture] tuple) => tuple.rest.first,
                                    selectedUnit, speedSource())));
                                IntArray indices = createJavaIntArray(
                                    {for ([index, fixture] in constants) index});
                                mainList.selectedIndices = indices;
                                outsideCritical = true;
                            }
                        });
                    }
                }
                // mainList.model.addListDataListener(ell);
                model.addSelectionChangeListener(ell);
                ecl.addSelectionChangeListener(ell);
                SwingList<TileFixture>&SelectionChangeListener secList =
                        fixtureList(tilesPanel, FixtureListModel(secondMap, false),
                            CeylonIterable(secondMap.players()));
                SelectionChangeSupport secPCS = SelectionChangeSupport();
                secPCS.addSelectionChangeListener(secList);
                tilesPanel.add(JScrollPane(secList));
                mains.put(direction, mainPCS);
                buttons.put(direction, dtb);
                seconds.put(direction, secPCS);
                ell.selectedPointChanged(null, model.selectedUnitLocation);
            }
        }
        explorationPanel.center = verticalSplit(0.5, 0.5, headerPanel, tilesPanel);
        model.addMovementCostListener(explorationPanel);
        model.addSelectionChangeListener(explorationPanel);
        object swapper satisfies CompletionListener {
            "Whether we're *on* the first panel. If so, go 'next'; if not, go 'first'."
            variable Boolean first = true;
            shared actual void finished() {
                explorationPanel.validate();
                explorerSelectingPanel.validate();
                if (first) {
                    layoutObj.next(contentPane);
                    first = false;
                } else {
                    layoutObj.first(contentPane);
                    first = true;
                }
            }
        }
        explorerSelectingPanel.addCompletionListener(swapper);
        explorationPanel.addCompletionListener(swapper);
        add(explorerSelectingPanel);
        add(explorationPanel);
    }
    (retval of Component).preferredSize = Dimension(1024, 640);
    SPMenu menu = SPMenu();
    menu.add(menu.createFileMenu(menuHandler, model));
    menu.addDisabled(menu.createMapMenu(menuHandler, model));
    menu.add(menu.createViewMenu(menuHandler, model));
    menu.add(WindowMenu(retval));
    retval.jMenuBar = menu;
    retval.pack();
    return retval;
}
"An object to start the exploration GUI."
object explorationGUI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        shortOption = "-x";
        longOption = "--explore";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Run exploration.";
        longDescription = "Move a unit around the map, updating the player's map with what it sees.";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        IExplorationModel explorationModel;
        if (is IExplorationModel model) {
            explorationModel = model;
        } else {
            explorationModel = ExplorationModel(model);
        }
        MenuBroker menuHandler = MenuBroker();
        menuHandler.register(IOHandler(explorationModel, options, cli), "load", "save",
            "save as", "new", "load secondary", "save all", "open in map viewer",
            "open secondary map in map viewer");
        menuHandler.register((event) => process.exit(0), "quit");
        SwingUtilities.invokeLater(() {
            SPFrame frame = explorationFrame(explorationModel,
                menuHandler.actionPerformed);
            menuHandler.register(WindowCloser(frame), "close");
            menuHandler.register((event) =>
                aboutDialog(frame, frame.windowName).setVisible(true), "about");
            frame.setVisible(true);
        });
    }
}
"""A driver to help debug "exploration tables", which were the second "exploration results" framework
   I implemented."""
object tableDebugger satisfies SimpleCLIDriver {
    ExplorationRunner runner = ExplorationRunner();
    if (is Directory directory = parsePath("tables").resource) {
        loadAllTables(directory, runner);
    } else {
        throw IllegalStateException("Table debugger requires a tables directory");
    }
    shared actual IDriverUsage usage = DriverUsage(false, "-T", "--table-debug", ParamCount.none,
        "Debug old-model encounter tables",
        "See whether old-model encounter tables refer to a nonexistent table");
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options, IDriverModel model) {
        log.warn("tableDebugger doesn't need a driver model");
        startDriverNoArgs();
    }
    "Print all possible results from a table."
    void debugSingleTable(
            "The string to print before each result (passed from the calling table)"
            String before,
            "The string to print after each result (passed from the calling table)"
            String after,
            "The table to debug"
            EncounterTable table,
            "The name of that table"
            String tableName,
            "The stream to write to"
            Anything(String) ostream,
            "The set of tables already on the stack, to prevent infinite recursion"
            todo("Use plain {EncounterTable*} instead of a Set?")
            MutableSet<EncounterTable> set) {
        if (set.contains(table)) {
            ostream("table ``tableName`` is already on the stack, skipping ...");
            ostream("The cause was: ``before``#``tableName``#``after``");
            return;
        }
        set.add(table);
        for (item in table.allEvents()) {
            if (item.contains("#")) {
                // FIXME: This relies on java.lang.String.split(), not ceylon.lang.String
                ObjectArray<JString> parsed = item.split("#", 3);
                String callee = (parsed[1] else nothing).string;
                debugSingleTable("``before````parsed[0] else ""``",
                    "``parsed[2] else ""````after``", runner.getTable(callee), callee,
                    ostream, set);
            } else {
                ostream("``before````item````after``");
            }
        }
        set.remove(table);
    }
    todo("If a CLIHelper was passed in, write to it")
    shared actual void startDriverNoArgs() {
        runner.verboseRecursiveCheck(SystemOut.sysOut);
        EncounterTable mainTable = runner.getTable("main");
        debugSingleTable("", "", mainTable, "main",
            (string) => SystemOut.sysOut.println(string), HashSet<EncounterTable>());
    }
}
EncounterTable loadTable(String?()|File argument) {
    if (is File argument) {
        try (reader = argument.Reader()) {
            return loadTable(reader.readLine);
        }
    } else {
        if (exists line = argument()) {
            switch (line[0])
            case (null) {
                throw IOException("File doesn't start by specifying which kind of table");
            }
            case ('q'|'Q') {
                if (exists firstLine = argument()) {
                    value rows = Integer.parse(firstLine);
                    if (is Integer rows) {
                        MutableList<JString> items = LinkedList<JString>();
                        while (exists tableLine = argument()) {
                            items.add(javaString(tableLine));
                        }
                        return QuadrantTable(rows, JavaList(items));
                    } else {
                        throw IOException(
                            "File doesn't start with number of rows of quadrants", rows);
                    }
                } else {
                    throw IOException(
                        "File doesn't start with number of rows of quadrants");
                }
            }
            case ('r'|'R') {
                // TODO: Use Tuples once RandomTable is ported to Ceylon.
                MutableList<ComparablePair<JInteger, JString>> list =
                        ArrayList<ComparablePair<JInteger, JString>>();
                while (exists tableLine = argument()) {
                    value splitted = tableLine.split(" ".equals, true, false);
                    if (splitted.size < 2) {
                        log.error("Line with no blanks, coninuing ...");
                    } else {
                        String left = splitted.first;
                        assert (exists right = splitted.rest.reduce(
                            (String partial, element) => "``partial`` ``element``"));
                        value leftNum = Integer.parse(left);
                        if (is Integer leftNum) {
                            list.add(ComparablePair.\iof(JInteger(leftNum),
                                javaString(right)));
                        } else {
                            throw IOException("Non-numeric data", leftNum);
                        }
                    }
                }
                return RandomTable(JavaList(list));
            }
            case ('c'|'C') {
                if (exists tableLine = argument()) {
                    return ConstantTable(tableLine);
                } else {
                    throw IOException("constant value not present");
                }
            }
            case ('l'|'L') { return LegacyTable(); }
            case ('t'|'T') {
                // TODO: Use Tuples once RandomTable is ported to Ceylon.
                MutableList<Pair<TileType, JString>> list =
                        ArrayList<Pair<TileType, JString>>();
                while (exists tableLine = argument()) {
                    value splitted = tableLine.split(" ".equals, true, false);
                    if (splitted.size < 2) {
                        log.error("Line with no blanks, coninuing ...");
                    } else {
                        String left = splitted.first;
                        assert (exists right = splitted.rest.reduce(
                            (String partial, element) => "``partial`` ``element``"));
                        value leftVal = TileType.getTileType(left);
                        list.add(Pair.\iof(leftVal, javaString(right)));
                    }
                }
                return TerrainTable(JavaList(list));
            }
            else { throw IllegalArgumentException("unknown table type"); }
        } else {
            throw IOException("File doesn't specify a table type");
        }
    }
}
"Load all tables in the specified path."
void loadAllTables(Directory path, ExplorationRunner runner) {
    // TODO: is it possible to exclude dotfiles using the "filter" parameter to files()?
    for (child in path.files()) {
        if (child.hidden || child.name.startsWith(".")) {
            log.info("``child.name`` looks like a hidden file, skipping ...");
        } else {
            runner.loadTable(child.name, loadTable(child));
        }
    }
}

test
void testLoadQuadrantTable() {
    Queue<String> data = LinkedList<String>({"quadrant", "2", "one", "two", "three",
        "four", "five", "six"});
    EncounterTable result = loadTable(data.accept);
    Point point = PointFactory.point(0, 0);
    MapDimensions dimensions = MapDimensionsImpl(69, 88, 2);
    assertEquals("one",result.generateEvent(point, TileType.tundra,
        Stream.empty<TileFixture>(), dimensions), "loading quadrant table");
    Point pointTwo = PointFactory.point(36, 30);
    assertEquals("one",result.generateEvent(point, TileType.ocean,
        Stream.empty<TileFixture>(), dimensions), "quadrant table isn't a terrain table");
    assertEquals(result.generateEvent(pointTwo, TileType.tundra,
        Stream.empty<TileFixture>(), dimensions), "five",
        "quadrant table isn't a constant table");
    assertEquals(result.generateEvent(pointTwo, TileType.tundra,
        Stream.empty<TileFixture>(), MapDimensionsImpl(35, 32, 2)), "six",
        "quadrant table can use alternate dimensions");
    assertThatException(() => loadTable(LinkedList({"quadrant"}).accept));
}
object mockDimensions satisfies MapDimensions {
    shared actual Integer rows => nothing;
    shared actual Integer columns => nothing;
    shared actual Integer version => nothing;
}
object mockPoint satisfies Point {
    shared actual Integer col => nothing;
    shared actual Integer row => nothing;
}
test
void testLoadRandomTable() {
    EncounterTable result = loadTable(LinkedList({"random", "0 one", "99 two"}).accept);
    assertEquals(result.generateEvent(mockPoint, TileType.tundra, Stream.empty<TileFixture>(),
        mockDimensions), "one", "loading random table");
}
test
void testLoadTerrainTable() {
    EncounterTable result = loadTable(LinkedList({"terrain", "tundra one",
        "plains two", "ocean three"}).accept);
    assertEquals(result.generateEvent(mockPoint, TileType.tundra,
        Stream.empty<TileFixture>(), mockDimensions), "one",
        "loading terrain table: tundra");
    assertEquals(result.generateEvent(mockPoint, TileType.plains,
        Stream.empty<TileFixture>(), mockDimensions), "two",
        "loading terrain table: plains");
    assertEquals(result.generateEvent(mockPoint, TileType.ocean,
        Stream.empty<TileFixture>(), mockDimensions), "three",
        "loading terrain table: ocean");
}

test
void testLoadConstantTable() {
    EncounterTable result = loadTable(LinkedList({"constant", "one"}).accept);
    assertEquals(result.generateEvent(mockPoint, TileType.plains,
        Stream.empty<TileFixture>(), mockDimensions), "one");
}

test
void testTableLoadingInvalidInput() {
    // no data
    assertThatException(() => loadTable(LinkedList({""}).accept));
    // invalid header
    assertThatException(() => loadTable(LinkedList({"2", "invalidData",
        "invalidData"}).accept));
}
