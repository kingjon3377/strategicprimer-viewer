import model.map.fixtures {
    Ground
}
import model.map.fixtures.resources {
    CacheFixture
}
import javax.swing.event {
    ListSelectionEvent
}
import util {
    IsNumeric
}
import ceylon.interop.java {
    CeylonIterable,
    CeylonList,
    createJavaIntArray,
    JavaList
}
import javax.swing.text {
    BadLocationException,
    Document
}
import com.bric.window {
    WindowMenu
}
import model.map.fixtures.towns {
    Village
}
import java.awt.event {
    KeyEvent,
    ActionEvent
}
import java.lang {
    JInteger=Integer,
    IntArray
}
import model.exploration {
    HuntingModel,
    IExplorationModel
}
import strategicprimer.viewer.drivers.map_viewer {
    fixtureFilterTableModel,
    FixtureMatcher,
    FixtureListModel,
    fixtureList,
    TileTypeFixture
}
import java.text {
    NumberFormat
}
import model.map {
    Player,
    Point,
    HasOwner,
    TileType,
    IMutableMapNG,
    TileFixture,
    PlayerImpl
}
import lovelace.util.jvm {
    horizontalSplit,
    ListModelWrapper,
    BorderedPanel,
    listenedButton,
    createHotKey,
    FormattedLabel,
    BoxAxis,
    shuffle,
    boxPanel,
    verticalSplit,
    ImprovedComboBox
}
import ceylon.collection {
    HashSet,
    HashMap,
    MutableSet,
    ArrayList,
    MutableList,
    MutableMap
}
import model.map.fixtures.terrain {
    Forest
}
import java.awt {
    CardLayout,
    Dimension,
    Component,
    GridLayout
}
import strategicprimer.viewer.drivers {
    SPFrame,
    SPMenu,
    ConstructorWrapper
}
import javax.swing {
    DefaultComboBoxModel,
    JTextField,
    JScrollPane,
    DefaultListModel,
    DefaultListCellRenderer,
    KeyStroke,
    ComboBoxModel,
    JOptionPane,
    ListModel,
    JPanel,
    SwingList=JList,
    AbstractAction,
    JComponent,
    JLabel,
    ListCellRenderer,
    SwingUtilities
}
import model.map.fixtures.mobile {
    SimpleMovement,
    IUnit,
    Animal
}
import model.listeners {
    PlayerChangeListener,
    SelectionChangeListener,
    CompletionListener,
    MovementCostListener,
    PlayerChangeSource,
    SelectionChangeSupport,
    SelectionChangeSource,
    CompletionSource,
    MovementCostSource
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
        object unitListModel extends DefaultListModel<IUnit>()
                satisfies PlayerChangeListener {
            shared actual void playerChanged(Player? old, Player newPlayer) {
                clear();
                for (unit in model.getUnits(newPlayer)) {
                    addElement(unit);
                }
            }
        }
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
            {FixtureMatcher*} matchers = fixtureFilterTableModel();
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
