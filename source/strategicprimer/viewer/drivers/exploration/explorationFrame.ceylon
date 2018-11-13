import ceylon.collection {
    HashSet,
    HashMap,
    MutableSet,
    ArrayList,
    MutableList,
    MutableMap
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
    IntArray,
    ObjectArray,
    JInteger=Integer
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
    SwingUtilities,
    JButton,
    JSpinner,
    SpinnerNumberModel
}

import lovelace.util.jvm {
    horizontalSplit,
    ListModelWrapper,
    BorderedPanel,
    listenedButton,
    createHotKey,
    verticalSplit,
    ImprovedComboBox,
    FunctionalGroupLayout,
    InterpolatedLabel
}

import lovelace.util.common {
    simpleMap,
    silentListener,
    invoke
}

import strategicprimer.model.common.map {
    HasExtent,
    HasPopulation,
    Player,
    PlayerImpl,
    HasOwner,
    Point,
    TileType,
    TileFixture,
    IMutableMapNG
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit,
    Animal,
    AnimalTracks
}
import strategicprimer.model.common.map.fixtures.resources {
    CacheFixture
}

import strategicprimer.drivers.gui.common {
    SPFrame,
    SPMenu,
    MenuBroker
}
import strategicprimer.viewer.drivers.map_viewer {
    SelectionChangeSupport,
    FixtureFilterTableModel,
    FixtureListModel,
    fixtureList,
    TileTypeFixture
}
import strategicprimer.model.common.map.fixtures.towns {
    Village
}
import strategicprimer.drivers.exploration.common {
    Direction,
    Speed,
    MovementCostListener,
    MovementCostSource,
    TraversalImpossibleException,
    simpleMovementModel
}
import strategicprimer.drivers.common {
    SelectionChangeListener,
    SelectionChangeSource,
    PlayerChangeListener,
    FixtureMatcher
}
import ceylon.random {
    randomize
}
import strategicprimer.model.impl.xmlio {
    mapIOHelper
}
import strategicprimer.model.common.idreg {
    IDRegistrar,
    createIDFactory
}
import javax.swing.event {
    ListDataListener,
    ListDataEvent
}

"The main window for the exploration GUI."
SPFrame explorationFrame(ExplorationGUI driver, // TODO: Merge parts of this back into ExplorationGUI?
        MenuBroker menuHandler) { // TODO: Do what we can to convert nested objects/classes to top-level, etc.
    Map<Direction, KeyStroke> arrowKeys = simpleMap( // TODO: Make a helper method keyStroke() that calls KeyStroke.getKeyStroke() and defaults modifiers to 0
        Direction.north->KeyStroke.getKeyStroke(KeyEvent.vkUp, 0),
        Direction.south->KeyStroke.getKeyStroke(KeyEvent.vkDown, 0),
        Direction.west->KeyStroke.getKeyStroke(KeyEvent.vkLeft, 0),
        Direction.east->KeyStroke.getKeyStroke(KeyEvent.vkRight, 0)
    );
    
    Map<Direction, KeyStroke> numKeys = simpleMap(
        Direction.north->KeyStroke.getKeyStroke(KeyEvent.vkNumpad8, 0),
        Direction.south->KeyStroke.getKeyStroke(KeyEvent.vkNumpad2, 0),
        Direction.west->KeyStroke.getKeyStroke(KeyEvent.vkNumpad4, 0),
        Direction.east->KeyStroke.getKeyStroke(KeyEvent.vkNumpad6, 0),
        Direction.northeast->KeyStroke.getKeyStroke(KeyEvent.vkNumpad9, 0),
        Direction.northwest->KeyStroke.getKeyStroke(KeyEvent.vkNumpad7, 0),
        Direction.southeast->KeyStroke.getKeyStroke(KeyEvent.vkNumpad3, 0),
        Direction.southwest->KeyStroke.getKeyStroke(KeyEvent.vkNumpad1, 0),
        Direction.nowhere->KeyStroke.getKeyStroke(KeyEvent.vkNumpad5, 0)
    );

    SPFrame retval = SPFrame("Exploration", driver, Dimension(768, 48), true,
        (file) => driver.model.addSubordinateMap(mapIOHelper.readMap(file), file)); // TODO: Use driver-interface method once it's available

    CardLayout layoutObj = CardLayout();
    retval.setLayout(layoutObj);
    SpinnerNumberModel mpModel = SpinnerNumberModel(0, 0, 2000, 0);
    JSpinner mpField = JSpinner(mpModel);

    object unitListModel extends DefaultListModel<IUnit>()
            satisfies PlayerChangeListener {
        shared actual void playerChanged(Player? old, Player newPlayer) {
            clear();
            driver.model.getUnits(newPlayer).each(addElement);
        }
    }
    SwingList<IUnit> unitList = SwingList<IUnit>(unitListModel);

    PlayerListModel playerListModel = PlayerListModel(driver.model);
    SwingList<Player> playerList = SwingList<Player>(playerListModel);

    MutableList<Anything()> completionListeners =
            ArrayList<Anything()>();

    void buttonListener(ActionEvent event) {
        if (exists selectedValue = unitList.selectedValue,
                !unitList.selectionEmpty) {
            driver.model.selectedUnit = selectedValue;
            completionListeners.each(invoke);
        }
    }

    ComboBoxModel<Speed> speedModel = DefaultComboBoxModel<Speed>(
        ObjectArray<Speed>.with(sort(`Speed`.caseValues)));

    object explorerSelectingPanel extends BorderedPanel() // TODO: Convert to top-level class, or better yet split appearance from controller-functionality
            satisfies PlayerChangeSource&CompletionSource {
        MutableList<PlayerChangeListener> listeners =
                ArrayList<PlayerChangeListener>();
        shared actual void addPlayerChangeListener(PlayerChangeListener listener) =>
                listeners.add(listener);
        shared actual void removePlayerChangeListener(PlayerChangeListener listener)
                => listeners.remove(listener);
        shared actual void addCompletionListener(Anything() listener) =>
                completionListeners.add(listener);
        shared actual void removeCompletionListener(Anything() listener) =>
                completionListeners.remove(listener);

        driver.model.addMapChangeListener(playerListModel); // TODO: Move this out of the object
        void handlePlayerChanged() {
            layoutObj.first(retval.contentPane);
            if (!playerList.selectionEmpty,
                    exists newPlayer = playerList.selectedValue) {
                for (listener in listeners) {
                    listener.playerChanged(null, newPlayer);
                }
            }
        }

        playerList.addListSelectionListener(silentListener(handlePlayerChanged));
        menuHandler.register(silentListener(handlePlayerChanged),
            "change current player");
        addPlayerChangeListener(unitListModel); // TODO: move out of the object (referring to this object rather than implicit 'this', of course)

        DefaultListCellRenderer defaultRenderer = DefaultListCellRenderer();
        object renderer satisfies ListCellRenderer<IUnit> { // TODO: convert to top-level class
            shared actual Component getListCellRendererComponent(
                    SwingList<out IUnit>? list, IUnit? val, Integer index,
                    Boolean isSelected, Boolean cellHasFocus) {
                Component retval = defaultRenderer.getListCellRendererComponent(list,
                    val, index, isSelected, cellHasFocus);
                if (exists val, is JLabel retval) {
                    retval.text = "``val.name`` (``val.kind``)";
                }
                return retval;
            }
        }

        unitList.cellRenderer = renderer;
        if (is JTextField mpEditor = mpField.editor) {
            mpEditor.addActionListener(buttonListener);
        }
        speedModel.selectedItem = Speed.normal;
    }

    explorerSelectingPanel.center = horizontalSplit(
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

    JPanel headerPanel = JPanel();
    FunctionalGroupLayout headerLayout = FunctionalGroupLayout(headerPanel);

    object explorationPanel extends BorderedPanel() // TODO: try to split controller-functionality from presentation
            satisfies SelectionChangeListener&CompletionSource&MovementCostListener {
        shared actual void deduct(Integer cost) =>
            mpModel.\ivalue = JInteger.valueOf(mpModel.number.intValue() - cost);

        String locLabelText(Point point) =>
                "<html><body>Currently exploring ``point``; click a tile to explore it.
                 Selected fixtures in its left-hand list will be 'discovered'.
                 </body></html>";
        InterpolatedLabel<[Point]> locLabel = InterpolatedLabel<[Point]>(locLabelText,
            [Point.invalidPoint]);

        MutableMap<Direction, SelectionChangeSupport> mains =
                HashMap<Direction, SelectionChangeSupport>();
        MutableMap<Direction, SelectionChangeSupport> seconds =
                HashMap<Direction, SelectionChangeSupport>();
        MutableMap<Direction, DualTileButton> buttons =
                HashMap<Direction, DualTileButton>();

        {FixtureMatcher*} matchers = FixtureFilterTableModel();

        class SpeedChangeListener(SelectionChangeListener scs) satisfies ListDataListener {
            shared variable Point point = Point.invalidPoint;
            void apply() {
                scs.selectedPointChanged(null, point);
            }
            shared actual void contentsChanged(ListDataEvent event) => apply();
            shared actual void intervalAdded(ListDataEvent event) => apply();
            shared actual void intervalRemoved(ListDataEvent event) => apply();
        }

        MutableMap<Direction, SpeedChangeListener> speedChangeListeners =
                HashMap<Direction, SpeedChangeListener>();
        shared actual void selectedPointChanged(Point? old, Point newPoint) {
            if (exists old, old == newPoint) {
                return;
            }
            for (direction in `Direction`.caseValues) {
                Point point = driver.model.getDestination(newPoint, direction);
                if (exists speedChangeListener = speedChangeListeners[direction]) {
                    speedChangeListener.point = point;
                }
                mains[direction]?.fireChanges(old, point); // TODO: 'old' wasn't previous selection *in this direction* ...
                seconds[direction]?.fireChanges(old, point);
                if (exists button = buttons[direction]) {
                    button.point = point;
                    button.repaint();
                }
            }
            locLabel.arguments = [newPoint];
        }

        MutableList<Anything()> completionListeners =
                ArrayList<Anything()>();
        shared actual void addCompletionListener(Anything() listener) =>
                completionListeners.add(listener);
        shared actual void removeCompletionListener(Anything() listener) =>
                completionListeners.remove(listener);

        JButton explorerChangeButton = listenedButton("Select a different explorer",
            (ActionEvent event) {
                for (listener in completionListeners) {
                    listener();
                }
            });

        JLabel remainingMPLabel = JLabel("Remaining Movement Points:");
        JSpinner mpField = JSpinner(mpModel);
        mpField.maximumSize = Dimension(runtime.maxArraySize,
            mpField.preferredSize.height.integer);

        JLabel speedLabel = JLabel("Current relative speed:");
        Speed() speedSource = () { // TODO: Convert to method
            assert (is Speed retval = speedModel.selectedItem);
            return retval;
        };
        value speedBox = ImprovedComboBox<Speed>.withModel(speedModel);

        headerPanel.add(explorerChangeButton);
        headerPanel.add(locLabel);
        headerPanel.add(remainingMPLabel);
        headerPanel.add(mpField);
        headerPanel.add(speedLabel);
        headerPanel.add(speedBox);

        headerLayout.setHorizontalGroup(headerLayout
            .sequentialGroupOf(explorerChangeButton, locLabel,
                remainingMPLabel, mpField, speedLabel, speedBox));
        headerLayout.setVerticalGroup(headerLayout.parallelGroupOf(explorerChangeButton,
            locLabel, remainingMPLabel, mpField, speedLabel, speedBox));

        IMutableMapNG secondMap; // TODO: Add 'secondMap' field to IExplorationModel (as IMap), to improve no-second-map to a-second-map transition
        if (exists entry = driver.model.subordinateMaps.first) {
            secondMap = entry.key;
        } else {
            secondMap = driver.model.map;
        }

        IDRegistrar idf = createIDFactory(driver.model.allMaps.map(Entry.key));
        HuntingModel huntingModel = HuntingModel(driver.model.map);

        AnimalTracks? tracksCreator(Point point) {
            if (exists terrain = driver.model.map.baseTerrain[point]) {
                {<Point->Animal|AnimalTracks|HuntingModel.NothingFound>*}(Point) source;
                if (terrain == TileType.ocean) {
                    source = huntingModel.fish;
                } else {
                    source = huntingModel.hunt;
                }
                value animal = source(point).map(Entry.item).first;
                if (is Animal animal) {
                    return AnimalTracks(animal.kind);
                } else if (is AnimalTracks animal) {
                    return animal.copy(true);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        class ExplorationClickListener(Direction direction,
                SwingList<TileFixture>&SelectionChangeListener mainList)
                satisfies MovementCostSource&SelectionChangeSource&ActionListener {
            MutableList<MovementCostListener> movementListeners =
                    ArrayList<MovementCostListener>();
            MutableList<SelectionChangeListener> selectionListeners =
                    ArrayList<SelectionChangeListener>();
            shared actual void addSelectionChangeListener(
                SelectionChangeListener listener) => selectionListeners.add(listener);
            shared actual void removeSelectionChangeListener(
                SelectionChangeListener listener) => selectionListeners.remove(listener);
            shared actual void addMovementCostListener(MovementCostListener listener) =>
                    movementListeners.add(listener);
            shared actual void removeMovementCostListener(MovementCostListener listener)
                    => movementListeners.remove(listener);

            MutableList<TileFixture> selectedValuesList {
                IntArray selections = mainList.selectedIndices;
                ListModel<TileFixture> listModel = mainList.model;
                MutableList<TileFixture> retval = ArrayList<TileFixture>();
                for (index in selections) {
                    if (index < listModel.size) {
                        assert (exists item = listModel.getElementAt(index));
                        retval.add(item);
                    } else {
                        assert (exists item = listModel.getElementAt(listModel.size - 1));
                        retval.add(item);
                    }
                }
                return retval;
            }

            void villageSwearingAction() {
                driver.model.swearVillages();
                //model.map.fixtures[model.selectedUnitLocation] // TODO: syntax sugar once compiler bug fixed
                driver.model.map.fixtures.get(driver.model.selectedUnitLocation)
                    .narrow<Village>().each(selectedValuesList.add);
            }

            "A list of things the explorer can do: pairs of explanations (in the
             form of questions to ask the user to see if the explorer does them)
             and references to methods for doing them."
            {[String, Anything()]*} explorerActions = [[
                "Should the explorer swear any villages on this tile?",
                    villageSwearingAction],
                ["Should the explorer dig to find what kind of ground is here?",
                    driver.model.dig]];

            void actionPerformedImpl() {
                try {
                    value fixtures = selectedValuesList;
                    if (Direction.nowhere == direction) {
                        for ([query, method] in explorerActions) {
                            Integer resp = JOptionPane.showConfirmDialog(null, query); // TODO: Extract confirmAction() method to lovelace.util.jvm
                            if (resp == JOptionPane.cancelOption) {
                                return;
                            } else if (resp == JOptionPane.yesOption) {
                                method();
                            }
                        }
                    }

                    driver.model.move(direction, speedSource());
                    Point destPoint = driver.model.selectedUnitLocation;
                    Player player = driver.model.selectedUnit ?. owner else
                        PlayerImpl(- 1, "no-one");

                    MutableSet<CacheFixture> caches = HashSet<CacheFixture>();
                    for (map->[file, _] in driver.model.subordinateMaps) {
                        map.baseTerrain[destPoint] = driver.model.map
//                              .baseTerrain[destPoint]; // TODO: syntax sugar once compiler bug fixed
                                .baseTerrain.get(destPoint);
                        for (fixture in fixtures) {
                            if (is TileTypeFixture fixture) {
                                // Skip it! It'll corrupt the output XML!
                                continue;
                            //} else if (!map.fixtures[destPoint].any(fixture.equals)) { // TODO: syntax sugar once compiler bug fixed
                            } else if (!map.fixtures.get(destPoint).any(fixture.equals)) {
                                Boolean zero;
                                if (is HasOwner fixture, fixture.owner != player || // TODO: add clarifying parentheses
                                        fixture is Village) {
                                    zero = true;
                                } else if (is HasPopulation<Anything>|HasExtent fixture) {
                                    zero = true;
                                } else {
                                    zero = false;
                                }
                                map.addFixture(destPoint, fixture.copy(zero));
                                if (is CacheFixture fixture) {
                                    caches.add(fixture);
                                }
                            }
                        }
                        driver.model.setModifiedFlag(map, true);
                    }
                    for (cache in caches) {
                        driver.model.map.removeFixture(destPoint, cache);
                    }
                } catch (TraversalImpossibleException except) {
                    log.debug("Attempted movement to impassable destination", except);
                    Point selection = driver.model.selectedUnitLocation;
                    for (listener in selectionListeners) {
                        listener.selectedPointChanged(null, selection);
                    }
                    for (listener in movementListeners) {
                        listener.deduct(1);
                    }
                }
            }

            shared actual void actionPerformed(ActionEvent event) =>
                    SwingUtilities.invokeLater(actionPerformedImpl);
        }

        void markModified() {
            for (map->_ in driver.model.allMaps) {
                driver.model.setModifiedFlag(map, true);
            }
        }

        object selectionChangeListenerObject satisfies SelectionChangeListener {
            shared actual void selectedPointChanged(Point? old, Point newSel) =>
                    outer.selectedPointChanged(old, newSel);
        }

        object movementCostProxy satisfies MovementCostListener {
            shared actual void deduct(Integer cost) => outer.deduct(cost);
        }

        for (direction in [Direction.northwest, // TODO: reformat // TODO: Make Direction comparable so we can replace this Tuple with sort(`Direction.caseValues`)?
                Direction.north,
                Direction.northeast,
                Direction.west, Direction.nowhere,
                Direction.east,
                Direction.southwest,
                Direction.south,
                Direction.southeast]) {
            SelectionChangeSupport mainPCS = SelectionChangeSupport();
            SwingList<TileFixture>&SelectionChangeListener mainList =
                    fixtureList(tilesPanel, FixtureListModel(driver.model.map, tracksCreator),
                idf, markModified, driver.model.map.players);
            mainPCS.addSelectionChangeListener(mainList);
            tilesPanel.add(JScrollPane(mainList));

            DualTileButton dtb = DualTileButton(driver.model.map, secondMap,
                matchers);
            // At some point we tried wrapping the button in a JScrollPane.
            tilesPanel.add(dtb);

            ExplorationClickListener ecl = ExplorationClickListener(direction, mainList);
            createHotKey(dtb, direction.string, ecl, JComponent.whenInFocusedWindow,
                *[arrowKeys[direction], numKeys[direction]].coalesced);
            dtb.addActionListener(ecl);
            ecl.addSelectionChangeListener(selectionChangeListenerObject);
            ecl.addMovementCostListener(movementCostProxy);

            """A list-data-listener to select a random but suitable set of fixtures to
                be "discovered" if the tile is explored."""
            object ell satisfies SelectionChangeListener {
                variable Boolean outsideCritical = true;
                shared actual void selectedPointChanged(Point? old, Point newPoint) {
                    SwingUtilities.invokeLater(() { // TODO: Convert lambda to named method in object
                        if (outsideCritical, exists selectedUnit =
                                driver.model.selectedUnit) {
                            outsideCritical = false;
                            mainList.clearSelection();
                            MutableList<[Integer, TileFixture]> constants =
                                    ArrayList<[Integer, TileFixture]>();
                            MutableList<[Integer, TileFixture]> possibles =
                                    ArrayList<[Integer, TileFixture]>();
                            for (index->fixture in ListModelWrapper(mainList.model)
                                    .indexed) {
                                if (simpleMovementModel.shouldAlwaysNotice(selectedUnit,
                                        fixture)) {
                                    constants.add([index, fixture]);
                                } else if (simpleMovementModel
                                        .shouldSometimesNotice(selectedUnit,
                                            speedSource(), fixture)) {
                                    possibles.add([index, fixture]);
                                }
                            }
                            constants.addAll(simpleMovementModel.selectNoticed(
                                randomize(possibles),
                                compose(Tuple<TileFixture, TileFixture, []>.first,
                                    Tuple<Integer|TileFixture, Integer,
                                    [TileFixture]>.rest),
                                selectedUnit, speedSource()));
                            IntArray indices = IntArray.with(
                                constants.map(Tuple.first));
                            mainList.selectedIndices = indices;
                            outsideCritical = true;
                        }
                    });
                }
            }

            // mainList.model.addListDataListener(ell);
            driver.model.addSelectionChangeListener(ell);
            ecl.addSelectionChangeListener(ell);

            SwingList<TileFixture>&SelectionChangeListener secList =
                    fixtureList(tilesPanel, FixtureListModel(secondMap, (point) => null),
                idf, markModified, secondMap.players);
            SelectionChangeSupport secPCS = SelectionChangeSupport();
            secPCS.addSelectionChangeListener(secList);
            tilesPanel.add(JScrollPane(secList));

            SpeedChangeListener scl = SpeedChangeListener(ell);
            speedModel.addListDataListener(scl);
            speedChangeListeners[direction] = scl;

            mains[direction] = mainPCS;
            buttons[direction] = dtb;
            seconds[direction] = secPCS;
            ell.selectedPointChanged(null, driver.model.selectedUnitLocation);
        }
    }

    explorationPanel.center = verticalSplit(headerPanel, tilesPanel);
    driver.model.addMovementCostListener(explorationPanel);
    driver.model.addSelectionChangeListener(explorationPanel);

    variable Boolean onFirstPanel = true;
    void swapPanels() {
        explorationPanel.validate();
        explorerSelectingPanel.validate();
        if (onFirstPanel) {
            layoutObj.next(retval.contentPane);
            onFirstPanel = false;
        } else {
            layoutObj.first(retval.contentPane);
            onFirstPanel = true;
        }
    }

    explorerSelectingPanel.addCompletionListener(swapPanels);
    explorationPanel.addCompletionListener(swapPanels);
    retval.add(explorerSelectingPanel);
    retval.add(explorationPanel);

    (retval of Component).preferredSize = Dimension(1024, 640);

    retval.jMenuBar = SPMenu.forWindowContaining(explorationPanel,
        SPMenu.createFileMenu(menuHandler.actionPerformed, driver),
        SPMenu.disabledMenu(SPMenu.createMapMenu(menuHandler.actionPerformed, driver)),
        SPMenu.createViewMenu(menuHandler.actionPerformed, driver));
    retval.pack();
    return retval;
}
