import lovelace.util.jvm {
    BorderedPanel,
    createMenuItem,
    createHotKey,
    ListenedButton,
    InterpolatedLabel,
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
    HuntingModel,
    Speed,
    TraversalImpossibleException,
    IExplorationModel
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
    FixtureListModel,
    fixtureList
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
    IMapNG,
    PlayerImpl,
    Player,
    TileFixture,
    HasOwner,
    TileType,
    Point,
    Direction,
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
    ArrayList,
    MutableList,
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
            JPanel tilesPanel, IExplorationModel driverModel,
            Anything() explorerChangeButtonListener)
        extends BorderedPanel(verticalSplit(headerPanel, tilesPanel))
        satisfies SelectionChangeListener {
    log.trace("In ExplorationPanel initializer");
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
    object movementDeductionTracker satisfies MovementCostListener {
        shared actual void deduct(Integer cost) =>
            mpModel.\ivalue =JInteger.valueOf(mpModel.number.intValue() - cost);
    }
    driverModel.addMovementCostListener(movementDeductionTracker);
    // TODO: Cache selected unit here instead of always referring to it via the model?
    shared actual void selectedUnitChanged(IUnit? old, IUnit? newSelection) {}

    String locLabelText(Point point) =>
        "<html><body>Currently exploring ``point``; click a tile to explore it.
                 Selected fixtures in its left-hand list will be 'discovered'.
                 </body></html>";
    InterpolatedLabel<[Point]> locLabel = InterpolatedLabel<[Point]>(locLabelText,
        [Point.invalidPoint]);

    MutableMap<Direction, SelectionChangeListener> mains =
        HashMap<Direction, SelectionChangeListener>();
    MutableMap<Direction, SelectionChangeListener> seconds =
        HashMap<Direction, SelectionChangeListener>();
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
        log.trace("In ExplorationPanel.selectedPointChanged");
        for (direction in `Direction`.caseValues) {
            log.trace("ExplorationPanel.selectedPointChanged: Beginning ``direction``");
            Point point = driverModel.getDestination(newPoint, direction);
            Point? previous;
            if (exists speedChangeListener = speedChangeListeners[direction]) {
                previous = speedChangeListener.point;
                speedChangeListener.point = point;
            } else {
                previous = old;
            }
            mains[direction]?.selectedPointChanged(previous, point);
            seconds[direction]?.selectedPointChanged(previous, point);
            if (exists button = buttons[direction]) {
                button.point = point;
            }
            log.trace("ExplorationPanel.selectedPointChanged: Ending ``direction``");
        }
        locLabel.arguments = [newPoint];
    }

    JButton explorerChangeButton = ListenedButton("Select a different explorer",
            explorerChangeButtonListener);

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

    log.trace("ExplorationPanel: headerPanel contents added");

    headerLayout.setHorizontalGroup(headerLayout
        .sequentialGroupOf(explorerChangeButton, locLabel,
        remainingMPLabel, mpField, speedLabel, speedBox));
    headerLayout.setVerticalGroup(headerLayout.parallelGroupOf(explorerChangeButton,
        locLabel, remainingMPLabel, mpField, speedLabel, speedBox));

    log.trace("ExplorationPanel: headerPanel layout adjusted");

    // TODO: Add 'secondMap' field to IExplorationModel (as IMap), to improve no-second-map to a-second-map transition
    IMapNG secondMap = driverModel.subordinateMaps.first else driverModel.map;

    IDRegistrar idf = createIDFactory(driverModel.allMaps);
    HuntingModel huntingModel = HuntingModel(driverModel.map);

    log.trace("ExplorationPanel: huntingModel created");

    AnimalTracks? tracksCreator(Point point) {
        if (exists terrain = driverModel.map.baseTerrain[point]) {
            log.trace("In ExplorationPanel.tracksCreator");
            {<Point->Animal|AnimalTracks|HuntingModel.NothingFound>*}(Point) source;
            if (terrain == TileType.ocean) {
                source = huntingModel.fish;
            } else {
                source = huntingModel.hunt;
            }
            log.trace("ExplorationPanel.tracksCreator: Determined which source to use");
            value animal = source(point).map(Entry.item).first;
            log.trace("ExplorationPanel.tracksCreator: Got first item from source");
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
            satisfies SelectionChangeSource&ActionListener {
        MutableList<SelectionChangeListener> selectionListeners =
            ArrayList<SelectionChangeListener>();
        shared actual void addSelectionChangeListener(
            SelectionChangeListener listener) => selectionListeners.add(listener);
        shared actual void removeSelectionChangeListener(
            SelectionChangeListener listener) => selectionListeners.remove(listener);

        MutableList<TileFixture> selectedValuesList {
            IntArray selections = mainList.selectedIndices;
            ListModel<TileFixture> listModel = mainList.model;
            MutableList<TileFixture> retval = ArrayList<TileFixture>();
            for (index in selections) {
                assert (exists item = listModel.getElementAt(
                    (index < listModel.size) then index else listModel.size - 1));
                retval.add(item);
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

            driverModel.copyTerrainToSubMaps(destPoint);
            for (fixture in fixtures) {
                if (is FakeFixture fixture) {
                    // Skip it! It'll corrupt the output XML!
                    continue;
                //} else if (!map.fixtures[destPoint].any(fixture.equals)) { // TODO: syntax sugar once compiler bug fixed
                } else {
                    Boolean zero;
                    if (is HasOwner fixture, (fixture.owner != player ||
                            fixture is Village)) {
                        zero = true;
                    } else {
                        zero = fixture is HasPopulation<Anything>|HasExtent<out Anything>;
                    }
                    driverModel.copyToSubMaps(destPoint, fixture, zero);
                }
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
                outer.selectedPointChanged(null, selection);
                for (listener in selectionListeners) {
                    listener.selectedPointChanged(null, selection);
                }
                movementDeductionTracker.deduct(1);
            }
        }

        shared actual void actionPerformed(ActionEvent event) =>
            SwingUtilities.invokeLater(actionPerformedImpl);
    }

    deprecated("Operations requiring explicit handling of modification flag should be moved into the model")
    void markModified() {
        for (map in driverModel.allMaps) {
            driverModel.setMapModified(map, true);
        }
    }

    AnimalTracks? createNull(Point point) => null;

    for (direction in sort(`Direction`.caseValues)) {
        log.trace("ExplorationPanel: Starting to initialize for ``direction``");
        SwingList<TileFixture>&SelectionChangeListener mainList =
            fixtureList(tilesPanel, FixtureListModel(driverModel.map.fixtures.get, driverModel.map.baseTerrain.get,
                    driverModel.map.rivers.get, driverModel.map.mountainous.get, tracksCreator,
                    null, null, null, null, null, null, increasing<TileFixture>), // TODO: Replace nulls with implementations?
                idf, markModified, driverModel.map.players);
        tilesPanel.add(JScrollPane(mainList));

        log.trace("ExplorationPanel: main list set up for ``direction``");

        DualTileButton dtb = DualTileButton(driverModel.map, secondMap, matchers);
        // At some point we tried wrapping the button in a JScrollPane.
        tilesPanel.add(dtb);
        log.trace("ExplorationPanel: Added button for ``direction``");

        ExplorationClickListener ecl = ExplorationClickListener(direction, mainList);
        if (Direction.nowhere == direction) {
            dtb.componentPopupMenu = ecl.explorerActionsMenu;
        }
        createHotKey(dtb, direction.string, ecl, JComponent.whenInFocusedWindow,
            *[arrowKeys[direction], numKeys[direction]].coalesced);
        dtb.addActionListener(ecl);

        value ell = RandomDiscoverySelector(driverModel, mainList, speedSource);

        // mainList.model.addListDataListener(ell);
        driverModel.addSelectionChangeListener(ell);
        ecl.addSelectionChangeListener(ell);

        log.trace("ExplorationPanel: ell set up for ``direction``");

        SwingList<TileFixture>&SelectionChangeListener secList =
            fixtureList(tilesPanel, FixtureListModel(secondMap.fixtures.get, secondMap.baseTerrain.get,
                    secondMap.rivers.get, secondMap.mountainous.get, createNull, driverModel.setSubMapTerrain,
                    driverModel.copyRiversToSubMaps, driverModel.setMountainousInSubMap, driverModel.copyToSubMaps,
                    driverModel.removeRiversFromSubMaps, driverModel.removeFixtureFromSubMaps, increasing<TileFixture>),
                idf, markModified, secondMap.players);
        tilesPanel.add(JScrollPane(secList));

        log.trace("ExploratonPanel: Second list set up for ``direction``");

        SpeedChangeListener scl = SpeedChangeListener(ell);
        speedModel.addListDataListener(scl);
        speedChangeListeners[direction] = scl;

        mains[direction] = mainList;
        buttons[direction] = dtb;
        seconds[direction] = secList;
        ell.selectedPointChanged(null, driverModel.selectedUnitLocation);
        log.trace("ExplorationPanel: Done with ``direction``");
    }
    log.trace("End of ExplorationPanel initializer");
    shared actual void interactionPointChanged() {}
    shared actual void cursorPointChanged(Point? oldCursor, Point newCursor) {}
}
