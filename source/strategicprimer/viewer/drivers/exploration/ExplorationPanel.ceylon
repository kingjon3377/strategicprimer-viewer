import lovelace.util.jvm {
    BorderedPanel,
    createMenuItem,
    createHotKey,
    ListenedButton,
    InterpolatedLabel,
    ListModelWrapper,
    FunctionalPopupMenu,
    ImprovedComboBox,
    FunctionalGroupLayout,
    verticalSplit
}
import strategicprimer.drivers.common {
    SelectionChangeListener,
    FixtureMatcher,
    SelectionChangeSource
}
import strategicprimer.drivers.exploration.common {
    MovementCostListener,
    MovementCostSource,
    simpleMovementModel,
    HuntingModel,
    Speed,
    Direction,
    TraversalImpossibleException,
    IExplorationModel
}
import ceylon.random {
    randomize
}
import strategicprimer.model.common.map.fixtures.towns {
    Village
}
import java.lang {
    JInteger=Integer,
    IntArray
}
import java.awt.event {
    ActionEvent,
    KeyEvent,
    ActionListener
}
import strategicprimer.viewer.drivers.map_viewer {
    FixtureFilterTableModel,
    SelectionChangeSupport,
    FixtureListModel,
    fixtureList
}
import strategicprimer.model.common.map.fixtures.resources {
    CacheFixture
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit,
    AnimalTracks,
    Animal
}
import java.awt {
    Dimension
}
import strategicprimer.model.common.map {
    FakeFixture,
    HasExtent,
    IMutableMapNG,
    PlayerImpl,
    Player,
    TileFixture,
    HasOwner,
    TileType,
    Point,
    HasPopulation
}
import javax.swing.event {
    ListDataListener,
    ListDataEvent
}
import javax.swing {
    ListModel,
    SwingList=JList,
    JScrollPane,
    JPopupMenu,
    JComponent,
    JButton,
    JLabel,
    JSpinner,
    SwingUtilities,
    SpinnerNumberModel,
    JPanel,
    ComboBoxModel,
    KeyStroke
}
import strategicprimer.model.common.idreg {
    IDRegistrar,
    createIDFactory
}
import ceylon.collection {
    MutableSet,
    ArrayList,
    MutableList,
    HashSet,
    MutableMap,
    HashMap
}
import lovelace.util.common {
    todo,
    simpleMap
}

todo("try to split controller-functionality from presentation")
class ExplorationPanel(SpinnerNumberModel mpModel, ComboBoxModel<Speed> speedModel,
            JPanel headerPanel, FunctionalGroupLayout headerLayout,
            JPanel tilesPanel, IExplorationModel driverModel)
        extends BorderedPanel(verticalSplit(headerPanel, tilesPanel))
        satisfies SelectionChangeListener&CompletionSource&MovementCostListener {
    KeyStroke key(Integer code) => KeyStroke.getKeyStroke(code, 0);
    Map<Direction, KeyStroke> arrowKeys = simpleMap(
        Direction.north->key(KeyEvent.vkUp), Direction.south->key(KeyEvent.vkDown),
        Direction.west->key(KeyEvent.vkLeft), Direction.east->key(KeyEvent.vkRight)
    );

    Map<Direction, KeyStroke> numKeys = simpleMap(
        Direction.north->key(KeyEvent.vkNumpad8),
        Direction.south->key(KeyEvent.vkNumpad2),
        Direction.west->key(KeyEvent.vkNumpad4),
        Direction.east->key(KeyEvent.vkNumpad6),
        Direction.northeast->key(KeyEvent.vkNumpad9),
        Direction.northwest->key(KeyEvent.vkNumpad7),
        Direction.southeast->key(KeyEvent.vkNumpad3),
        Direction.southwest->key(KeyEvent.vkNumpad1),
        Direction.nowhere->key(KeyEvent.vkNumpad5)
    );
    // TODO: Extract to a separate object, passed in as needed
    shared actual void deduct(Integer cost) =>
        mpModel.\ivalue = JInteger.valueOf(mpModel.number.intValue() - cost);
    // TODO: Cache selected unit here instead of always referring to it via the model?
    shared actual void selectedUnitChanged(IUnit? old, IUnit? newSelection) {}

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

    class SpeedChangeListener(SelectionChangeListener scs)
        satisfies ListDataListener {
        shared variable Point point = Point.invalidPoint;
        void apply() => scs.selectedPointChanged(null, point);
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
            Point point = driverModel.getDestination(newPoint, direction);
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

    JButton explorerChangeButton = ListenedButton("Select a different explorer",
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
    Speed speedSource() {
        assert (is Speed retval = speedModel.selectedItem);
        return retval;
    }
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
    if (exists entry = driverModel.subordinateMaps.first) {
        secondMap = entry.key;
    } else {
        secondMap = driverModel.map;
    }

    IDRegistrar idf = createIDFactory(driverModel.allMaps.map(Entry.key));
    HuntingModel huntingModel = HuntingModel(driverModel.map);

    AnimalTracks? tracksCreator(Point point) {
        if (exists terrain = driverModel.map.baseTerrain[point]) {
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
            driverModel.swearVillages();
            //model.map.fixtures[model.selectedUnitLocation] // TODO: syntax sugar once compiler bug fixed
            driverModel.map.fixtures.get(driverModel.selectedUnitLocation)
                .narrow<Village>().each(selectedValuesList.add);
        }

        "Copy fixtures from the given list to subordinate maps."
        void discoverFixtures({TileFixture*} fixtures) {
            Point destPoint = driverModel.selectedUnitLocation;
            Player player = driverModel.selectedUnit ?. owner else
            PlayerImpl(- 1, "no-one");

            MutableSet<CacheFixture> caches = HashSet<CacheFixture>();
            for (map->[file, _] in driverModel.subordinateMaps) {
                map.baseTerrain[destPoint] = driverModel.map
//                              .baseTerrain[destPoint]; // TODO: syntax sugar once compiler bug fixed
                    .baseTerrain.get(destPoint);
                for (fixture in fixtures) {
                    if (is FakeFixture fixture) {
                        // Skip it! It'll corrupt the output XML!
                        continue;
                        //} else if (!map.fixtures[destPoint].any(fixture.equals)) { // TODO: syntax sugar once compiler bug fixed
                    } else if (!map.fixtures.get(destPoint).any(fixture.equals)) {
                        Boolean zero;
                        if (is HasOwner fixture, fixture.owner != player || // TODO: add clarifying parentheses
                        fixture is Village) {
                            zero = true;
                        } else if (is HasPopulation<Anything>|HasExtent<out Anything> fixture) {
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
                driverModel.setModifiedFlag(map, true);
            }
            for (cache in caches) {
                driverModel.map.removeFixture(destPoint, cache);
            }
        }

        "The action of searching the current tile, since on moving 'nowhere' the
         listener now aborts its normal process."
        void searchCurrentTile() {
            value fixtures = selectedValuesList;
            driverModel.move(Direction.nowhere, speedSource());
            discoverFixtures(fixtures);
        }

        "A menu of actions the explorer can take when moving 'nowhere'."
        shared JPopupMenu explorerActionsMenu = FunctionalPopupMenu(
            createMenuItem("Swear any villages", KeyEvent.vkV,
                "Swear any independent villages on this tile to the player's service",
                villageSwearingAction),
            createMenuItem("Dig to expose ground", KeyEvent.vkD,
                "Dig to find what kind of ground is here", driverModel.dig),
            createMenuItem("Search again", KeyEvent.vkS,
                "Search this tile, as if arriving on it again", searchCurrentTile));

        void actionPerformedImpl() {
            try {
                value fixtures = selectedValuesList;
                if (Direction.nowhere == direction) {
                    explorerActionsMenu.show(mainList, mainList.width, 0);
                } else {
                    driverModel.move(direction, speedSource());
                    discoverFixtures(fixtures);
                }
            } catch (TraversalImpossibleException except) {
                log.debug("Attempted movement to impassable destination", except);
                Point selection = driverModel.selectedUnitLocation;
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
        for (map->_ in driverModel.allMaps) {
            driverModel.setModifiedFlag(map, true);
        }
    }

    object selectionChangeListenerObject satisfies SelectionChangeListener {
        shared actual void selectedPointChanged(Point? old, Point newSel) =>
            outer.selectedPointChanged(old, newSel);
        shared actual void selectedUnitChanged(IUnit? old, IUnit? newSel) =>
            outer.selectedUnitChanged(old, newSel);
    }

    object movementCostProxy satisfies MovementCostListener {
        shared actual void deduct(Integer cost) => outer.deduct(cost);
    }

    for (direction in sort(`Direction`.caseValues)) {
        SelectionChangeSupport mainPCS = SelectionChangeSupport();
        SwingList<TileFixture>&SelectionChangeListener mainList =
            fixtureList(tilesPanel, FixtureListModel(driverModel.map,
                tracksCreator),
                idf, markModified, driverModel.map.players);
        mainPCS.addSelectionChangeListener(mainList);
        tilesPanel.add(JScrollPane(mainList));

        DualTileButton dtb = DualTileButton(driverModel.map, secondMap,
            matchers);
        // At some point we tried wrapping the button in a JScrollPane.
        tilesPanel.add(dtb);

        ExplorationClickListener ecl = ExplorationClickListener(direction, mainList);
        if (Direction.nowhere == direction) {
            dtb.componentPopupMenu = ecl.explorerActionsMenu;
        }
        createHotKey(dtb, direction.string, ecl, JComponent.whenInFocusedWindow,
        *[arrowKeys[direction], numKeys[direction]].coalesced);
        dtb.addActionListener(ecl);
        ecl.addSelectionChangeListener(selectionChangeListenerObject);
        ecl.addMovementCostListener(movementCostProxy);

        """A list-data-listener to select a random but suitable set of fixtures to
            be "discovered" if the tile is explored."""
        object ell satisfies SelectionChangeListener {
            variable Boolean outsideCritical = true;
            void selectedPointChangedImpl() {
                if (outsideCritical, exists selectedUnit =
                    driverModel.selectedUnit) {
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
            }
            shared actual void selectedPointChanged(Point? old, Point newPoint) { // TODO: =>
                SwingUtilities.invokeLater(selectedPointChangedImpl);
            }
            shared actual void selectedUnitChanged(IUnit? old, IUnit? newSel) {}
        }

        // mainList.model.addListDataListener(ell);
        driverModel.addSelectionChangeListener(ell);
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
        ell.selectedPointChanged(null, driverModel.selectedUnitLocation);
    }
}
