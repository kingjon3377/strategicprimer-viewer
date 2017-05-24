import ceylon.collection {
    HashSet,
    HashMap,
    MutableSet,
    ArrayList,
    MutableList,
    MutableMap
}
import ceylon.interop.java {
    createJavaIntArray,
    createJavaObjectArray
}

import com.pump.window {
    WindowMenu
}

import java.awt {
    CardLayout,
    Dimension,
    Component,
    GridLayout
}
import java.awt.event {
    KeyEvent,
    ActionEvent,
    ActionListener
}
import java.lang {
    JInteger=Integer,
    IntArray
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
    JComponent,
    JLabel,
    ListCellRenderer,
    SwingUtilities
}
import javax.swing.event {
    ListSelectionEvent
}
import javax.swing.text {
    BadLocationException,
    Document
}

import lovelace.util.jvm {
    horizontalSplit,
    ListModelWrapper,
    BorderedPanel,
    listenedButton,
    createHotKey,
    FormattedLabel,
    BoxAxis,
    boxPanel,
    verticalSplit,
    ImprovedComboBox,
    parseInt,
    isNumeric
}

import strategicprimer.model.map {
    Point,
    Player,
    TileType,
    TileFixture,
    IMutableMapNG,
    PlayerImpl,
    HasOwner
}
import strategicprimer.model.map.fixtures.mobile {
    IUnit,
    Animal
}
import strategicprimer.model.map.fixtures.resources {
    CacheFixture
}

import strategicprimer.viewer.drivers {
    SPFrame,
    SPMenu
}
import strategicprimer.viewer.drivers.map_viewer {
    SelectionChangeSupport,
    fixtureFilterTableModel,
    FixtureMatcher,
    FixtureListModel,
    fixtureList,
    TileTypeFixture
}
import strategicprimer.model.map.fixtures.towns {
    Village
}
import strategicprimer.drivers.exploration.common {
    Direction,
    IExplorationModel,
    Speed,
    MovementCostListener,
    MovementCostSource,
    TraversalImpossibleException,
    selectNoticed,
    shouldSometimesNotice,
    shouldAlwaysNotice
}
import strategicprimer.drivers.common {
    SelectionChangeListener,
    SelectionChangeSource,
    PlayerChangeListener
}
import ceylon.random {
    randomize
}
import strategicprimer.model.xmlio {
    readMap
}
import java.nio.file {
    JPath=Path
}
"The main window for the exploration GUI."
SPFrame explorationFrame(IExplorationModel model,
        Anything(ActionEvent) menuHandler) {
    Map<Direction, KeyStroke> arrowKeys = HashMap {
        Direction.north->KeyStroke.getKeyStroke(KeyEvent.vkUp, 0),
        Direction.south->KeyStroke.getKeyStroke(KeyEvent.vkDown, 0),
        Direction.west->KeyStroke.getKeyStroke(KeyEvent.vkLeft, 0),
        Direction.east->KeyStroke.getKeyStroke(KeyEvent.vkRight, 0)
    };
    Map<Direction, KeyStroke> numKeys = HashMap {
        Direction.north->KeyStroke.getKeyStroke(KeyEvent.vkNumpad8, 0),
        Direction.south->KeyStroke.getKeyStroke(KeyEvent.vkNumpad2, 0),
        Direction.west->KeyStroke.getKeyStroke(KeyEvent.vkNumpad4, 0),
        Direction.east->KeyStroke.getKeyStroke(KeyEvent.vkNumpad6, 0),
        Direction.northeast->KeyStroke.getKeyStroke(KeyEvent.vkNumpad9, 0),
        Direction.northwest->KeyStroke.getKeyStroke(KeyEvent.vkNumpad7, 0),
        Direction.southeast->KeyStroke.getKeyStroke(KeyEvent.vkNumpad3, 0),
        Direction.southwest->KeyStroke.getKeyStroke(KeyEvent.vkNumpad1, 0),
        Direction.nowhere->KeyStroke.getKeyStroke(KeyEvent.vkNumpad5, 0)
    };
    object retval extends SPFrame("Exploration", model.mapFile,
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
        SwingList<IUnit> unitList = SwingList<IUnit>(unitListModel);
        PlayerListModel playerListModel = PlayerListModel(model);
        SwingList<Player> playerList = SwingList<Player>(playerListModel);
        MutableList<CompletionListener> completionListeners =
                ArrayList<CompletionListener>();
        void buttonListener(ActionEvent event) {
            if (exists selectedValue = unitList.selectedValue,
                   !unitList.selectionEmpty) {
                model.selectedUnit = selectedValue;
                for (listener in completionListeners) {
                    listener.finished();
                }
            }
        }
        shared ComboBoxModel<Speed> speedModel = DefaultComboBoxModel<Speed>(
            createJavaObjectArray<Speed>(`Speed`.caseValues));
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
            speedModel.selectedItem = Speed.normal;
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
                        null, ImprovedComboBox<Speed>.withModel(speedModel)),
                    listenedButton("Start exploring!", buttonListener))));
        JPanel tilesPanel = JPanel(GridLayout(3, 12, 2, 2));
        JPanel headerPanel = boxPanel(BoxAxis.lineAxis);
        object explorationPanel extends BorderedPanel()
                satisfies SelectionChangeListener&CompletionSource&MovementCostListener {
            Document mpDocument = explorerSelectingPanel.mpDocument;
            shared actual void deduct(Integer cost) {
                String mpText;
                try {
                    mpText = mpDocument.getText(0, mpDocument.length).trimmed;
                } catch (BadLocationException except) {
                    log.error("Exception trying to update MP counter", except);
                    return;
                }
                if (isNumeric(mpText)) {
                    assert (exists temp = parseInt(mpText));
                    variable Integer movePoints = temp;
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
            MutableMap<Direction, SelectionChangeSupport> mains =
                    HashMap<Direction, SelectionChangeSupport>();
            MutableMap<Direction, SelectionChangeSupport> seconds =
                    HashMap<Direction, SelectionChangeSupport>();
            MutableMap<Direction, DualTileButton> buttons =
                    HashMap<Direction, DualTileButton>();
            {FixtureMatcher*} matchers = fixtureFilterTableModel();
            shared actual void selectedPointChanged(Point? old, Point newPoint) {
                // TODO: use the provided old point instead?
                Point selPoint = model.selectedUnitLocation;
                for (direction in `Direction`.caseValues) {
                    Point point = model.getDestination(selPoint, direction);
                    mains[direction]?.fireChanges(selPoint, point);
                    seconds[direction]?.fireChanges(selPoint, point);
                    if (exists button = buttons[direction]) {
                        button.point = point;
                        button.repaint();
                    }
                }
                locLabel.setArgs(selPoint.row, selPoint.column);
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
            Speed() speedSource = () {
                assert (is Speed retval = speedModel.selectedItem);
                return retval;
            };
            headerPanel.add(ImprovedComboBox<Speed>.withModel(speedModel));
            IMutableMapNG secondMap;
            if (exists pair = model.subordinateMaps.first) {
                secondMap = pair.first;
            } else {
                secondMap = model.map;
            }
            for (direction in {Direction.northwest,
                    Direction.north,
                    Direction.northeast,
                    Direction.west, Direction.nowhere,
                    Direction.east,
                    Direction.southwest,
                    Direction.south,
                    Direction.southeast}) {
                SelectionChangeSupport mainPCS = SelectionChangeSupport();
                SwingList<TileFixture>&SelectionChangeListener mainList =
                        fixtureList(tilesPanel, FixtureListModel(model.map, true),
                            model.map.players);
                mainPCS.addSelectionChangeListener(mainList);
                tilesPanel.add(JScrollPane(mainList));
                DualTileButton dtb = DualTileButton(model.map, secondMap,
                    matchers);
                // At some point we tried wrapping the button in a JScrollPane.
                tilesPanel.add(dtb);
                object ecl satisfies MovementCostSource&SelectionChangeSource&
                        ActionListener {
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
                            MovementCostListener listener) =>
                            movementListeners.add(listener);
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
                    {[String, Anything()]*} explorerActions = {[
                            "Should the explorer swear any villages on this tile?", () {
                        model.swearVillages();
//                        for (fixture in model.map.fixtures[model.selectedUnitLocation] // TODO: syntax sugar once compiler bug fixed
                        for (fixture in model.map.fixtures.get(model.selectedUnitLocation)
                                .narrow<Village>()) {
                            selectedValuesList.add(fixture);
                        }
                    }], ["Should the explorer dig to find what kind of ground is here?",
                        model.dig]};
                    shared actual void actionPerformed(ActionEvent event) =>
                            SwingUtilities.invokeLater(() {
                                try {
                                    value fixtures = selectedValuesList;
                                    if (Direction.nowhere == direction) {
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
                                    MutableSet<CacheFixture> caches =
                                            HashSet<CacheFixture>();
                                    for ([map, file] in model.subordinateMaps) {
                                        map.baseTerrain[destPoint] = model.map
//                                            .baseTerrain[destPoint]; // TODO: syntax sugar once compiler bug fixed
                                            .baseTerrain.get(destPoint);
                                        for (fixture in fixtures) {
                                            if (is TileTypeFixture fixture) {
                                                // Skip it! It'll corrupt the output XML!
                                                continue ;
//                                            } else if (!map.fixtures[destPoint] // TODO: syntax sugar once compiler bug fixed
                                            } else if (!map.fixtures.get(destPoint)
                                                    .any((that) => fixture == that)) {
                                                Boolean zero;
                                                if (is HasOwner fixture,
                                                        fixture.owner != player) {
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
                                } catch (TraversalImpossibleException except) {
                                    log.debug(
                                        "Attempted movement to impassable destination",
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
                    *{arrowKeys[direction], numKeys[direction]}.coalesced);
                dtb.addActionListener(ecl);
                ecl.addSelectionChangeListener(object satisfies SelectionChangeListener {
                    shared actual void selectedPointChanged(Point? old, Point newSel) =>
                            outer.selectedPointChanged(old, newSel);
                });
                ecl.addMovementCostListener(object satisfies MovementCostListener {
                    shared actual void deduct(Integer cost) => outer.deduct(cost);
                });
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
                                for (index->fixture in ListModelWrapper(mainList.model)
                                        .indexed) {
                                    if (shouldAlwaysNotice(selectedUnit, fixture)) {
                                        constants.add([index, fixture]);
                                    } else if (shouldSometimesNotice(selectedUnit,
                                            speedSource(), fixture)) {
                                        possibles.add([index, fixture]);
                                    }
                                }
                                Point currentLocation = model.selectedUnitLocation;
                                if (currentLocation.valid) {
                                    {String*}(Point, Integer) tracksSource;
                                    if (TileType.ocean == model.map
//                                            .baseTerrain[currentLocation]) { // TODO: syntax sugar once compiler bug fixed
                                            .baseTerrain.get(currentLocation)) {
                                        tracksSource = huntingModel.fish;
                                    } else {
                                        tracksSource = huntingModel.hunt;
                                    }
                                    if (exists possibleTracks =
                                                tracksSource(currentLocation, 1).first,
                                            HuntingModel.noResults != possibleTracks) {
                                        Animal animal = Animal(possibleTracks, true,
                                            false, "wild", -1);
                                        assert (is FixtureListModel listModel =
                                                mainList.model);
                                        Integer index = listModel.size;
                                        listModel.addFixture(animal);
                                        possibles.add([index, animal]);
                                        tracks.add([currentLocation, animal]);
                                    }
                                }
                                constants.addAll(selectNoticed(randomize(possibles),
                                            ([Integer, TileFixture] tuple) => tuple.rest
                                                .first,
                                    selectedUnit, speedSource()));
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
                            secondMap.players);
                SelectionChangeSupport secPCS = SelectionChangeSupport();
                secPCS.addSelectionChangeListener(secList);
                tilesPanel.add(JScrollPane(secList));
                mains[direction] = mainPCS;
                buttons[direction] = dtb;
                seconds[direction] = secPCS;
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
        shared actual void acceptDroppedFile(JPath file) =>
                model.addSubordinateMap(readMap(file), file);
        shared actual Boolean supportsDroppedFiles = true;
        explorerSelectingPanel.addCompletionListener(swapper);
        explorationPanel.addCompletionListener(swapper);
        add(explorerSelectingPanel);
        add(explorationPanel);
    }
    (retval of Component).preferredSize = Dimension(1024, 640);
    SPMenu menu = SPMenu();
    menu.add(SPMenu.createFileMenu(menuHandler, model));
    menu.addDisabled(SPMenu.createMapMenu(menuHandler, model));
    menu.add(SPMenu.createViewMenu(menuHandler, model));
    menu.add(WindowMenu(retval));
    retval.jMenuBar = menu;
    retval.pack();
    return retval;
}
