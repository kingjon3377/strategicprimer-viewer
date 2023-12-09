package drivers.map_viewer;

import common.map.HasName;
import legacy.map.HasOwner;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.nio.file.Path;

import legacy.map.fixtures.FixtureIterable;

import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.function.Predicate;

import org.javatuples.Pair;

import java.util.List;
import java.util.ArrayList;

import drivers.common.SimpleDriverModel;
import drivers.common.IDriverModel;
import drivers.common.SelectionChangeListener;
import legacy.map.Point;
import legacy.map.HasKind;
import legacy.map.HasMutableKind;
import legacy.map.HasMutableName;
import legacy.map.HasMutableOwner;
import legacy.map.IFixture;
import legacy.map.IMutableMapNG;
import common.map.Player;
import legacy.map.River;
import legacy.map.TileFixture;
import legacy.map.TileType;

import legacy.map.fixtures.UnitMember;

import legacy.map.fixtures.mobile.IMutableUnit;
import legacy.map.fixtures.mobile.IUnit;

import legacy.map.fixtures.towns.IFortress;
import legacy.map.fixtures.towns.IMutableFortress;
import lovelace.util.Range;

/**
 * A class to encapsulate the various model-type things views need to do with maps.
 *
 * TODO: Tests
 */
public class ViewerModel extends SimpleDriverModel implements IViewerModel {
    /**
     * The starting zoom level.
     */
    public static final int DEFAULT_ZOOM_LEVEL = 8;

    /**
     * The maximum zoom level, to make sure that the tile size never overflows.
     */
    public static final int MAX_ZOOM_LEVEL = Short.MAX_VALUE / 2;

    /**
     * The distance a 'jump' will take the cursor.
     */
    public static final int JUMP_INTERVAL = 5;

    private static Stream<IFixture> flattenIncluding(final IFixture fixture) {
        if (fixture instanceof FixtureIterable) {
            return Stream.concat(Stream.of(fixture), ((FixtureIterable<?>) fixture).stream());
        } else {
            return Stream.of(fixture);
        }
    }

    /**
     * If the item in the entry is a {@link IFortress fortress}, return a
     * stream of its contents paired with its location; otherwise, return a
     * stream of the provided entry alone.
     */
    private static Stream<Pair<Point, IFixture>> flattenEntries(final Pair<Point, IFixture> entry) {
        if (entry.getValue1() instanceof final IFortress f) {
            return f.stream().map(m -> Pair.with(entry.getValue0(), m));
        } else {
            return Stream.of(entry);
        }
    }

    /**
     * If the given fixture is a {@link IFortress fortress}, return it; otherwise,
     * return a Singleton containing it. This is intended to be used in
     * {@link Stream#flatMap}.
     */
    private static Stream<IFixture> unflattenNonFortresses(final TileFixture fixture) {
        if (fixture instanceof final IFortress f) {
            return f.stream().map(IFixture.class::cast);
        } else {
            return Stream.of(fixture);
        }
    }

    /**
     * The list of graphical-parameter listeners.
     */
    private final List<GraphicalParamsListener> gpListeners = new ArrayList<>();

    /**
     * The object to handle notifying selection-change listeners.
     */
    private final SelectionChangeSupport scs = new SelectionChangeSupport();

    /**
     * Previously dismissed members.
     */
    private final Set<UnitMember> dismissedMembers = new HashSet<>();

    /**
     * The current zoom level.
     */
    private int _zoomLevel = DEFAULT_ZOOM_LEVEL;

    /**
     * The current zoom level.
     */
    @Override
    public int getZoomLevel() {
        return _zoomLevel;
    }

    /**
     * Zoom in, increasing the zoom level.
     */
    @Override
    public void zoomIn() {
        if (_zoomLevel < MAX_ZOOM_LEVEL) {
            final int oldZoom = _zoomLevel;
            _zoomLevel++;
            final int newZoom = _zoomLevel;
            for (final GraphicalParamsListener listener : gpListeners) {
                listener.tileSizeChanged(oldZoom, newZoom);
            }
        }
    }

    /**
     * Zoom out, decreasing the zoom level.
     */
    @Override
    public void zoomOut() {
        if (_zoomLevel > 1) {
            final int oldZoom = _zoomLevel;
            _zoomLevel--;
            final int newZoom = _zoomLevel;
            for (final GraphicalParamsListener listener : gpListeners) {
                listener.tileSizeChanged(oldZoom, newZoom);
            }
        }
    }

    /**
     * The currently selected point in the main map.
     */
    private Point selPoint = Point.INVALID_POINT;

    /**
     * The point currently pointed to by the scroll-bars.
     */
    private Point cursorPoint = new Point(0, 0);

    /**
     * The point in the map the user has just right-clicked on, if any.
     */
    private @Nullable Point interactionPoint = null;

    /**
     * The visible dimensions of the map.
     */
    private VisibleDimensions visDimensions;

    /**
     * @param theMap The initial map
     */
    public ViewerModel(final IMutableMapNG theMap) {
        super(theMap);
        visDimensions = new VisibleDimensions(0, theMap.getDimensions().rows() - 1,
                0, theMap.getDimensions().columns() - 1);
    }

    // TODO: Provide static method copyConstructor() calling this?
    public ViewerModel(final IDriverModel model) {
        super(model.getRestrictedMap());
        if (model instanceof final IViewerModel vm) {
            visDimensions = vm.getVisibleDimensions();
            selPoint = vm.getSelection();
            _zoomLevel = vm.getZoomLevel();
        } else {
            visDimensions = new VisibleDimensions(0, model.getMapDimensions().rows() - 1,
                    0, model.getMapDimensions().columns() - 1);
            _zoomLevel = DEFAULT_ZOOM_LEVEL;
        }
    }

    /**
     * The visible dimensions of the map.
     */
    @Override
    public VisibleDimensions getVisibleDimensions() {
        return visDimensions;
    }

    @Override
    public void setVisibleDimensions(final VisibleDimensions visibleDimensions) {
        if (!visDimensions.equals(visibleDimensions)) {
            final VisibleDimensions oldDimensions = visDimensions;
            visDimensions = visibleDimensions;
            // We notify listeners after the change, since one object's
            // dimensionsChanged() delegates to repaint(). (The other uses the parameter
            // we provide for robustness.)
            for (final GraphicalParamsListener listener : gpListeners) {
                listener.dimensionsChanged(oldDimensions, visibleDimensions);
            }
        }
    }

    private static Range constrain(final int selection, final Range curr, final int currMin,
                                   final int currMax, final int mapEdge) {
        // TODO: Check possible off-by-one here or in caller: Range says it is *inclusive* on both ends
        if (curr.contains(selection)) {
            return curr;
        } else if (currMin > selection && currMin - selection <= JUMP_INTERVAL) {
            return new Range(selection, currMax - (currMin - selection));
        } else if (currMax < selection && selection - currMax >= JUMP_INTERVAL) {
            return new Range(currMin + (selection - currMax), selection);
        } else if (selection >= 0 && selection < curr.size()) {
            return new Range(0, curr.size() - 1);
        } else if (selection >= mapEdge - curr.size() && selection < mapEdge) {
            return new Range(mapEdge - curr.size(), mapEdge - 1);
        } else {
            final int min = selection - curr.size() / 2;
            return new Range(min, min + curr.size() - 1);
        }
    }

    private void fixVisibility() {
        final Point currSelection = selPoint;
        final VisibleDimensions currDims = visDimensions;
        final int row;
        if (currSelection.row() < 0) {
            row = 0;
        } else if (currSelection.row() >= getMap().getDimensions().rows()) {
            row = getMap().getDimensions().rows() - 1;
        } else {
            row = currSelection.row();
        }
        final int column;
        if (currSelection.column() < 0) {
            column = 0;
        } else if (currSelection.column() >= getMap().getDimensions().columns()) {
            column = getMap().getDimensions().columns() - 1;
        } else {
            column = currSelection.column();
        }
        final Range rowRange = constrain(row, currDims.getRows(), currDims.getMinimumRow(),
                currDims.getMaximumRow(), getMap().getDimensions().rows());
        final int minRow = rowRange.lowerBound();
        final int maxRow = rowRange.upperBound();
        final Range colRange = constrain(column, currDims.getColumns(),
                currDims.getMinimumColumn(), currDims.getMaximumColumn(),
                getMap().getDimensions().columns());
        final int minColumn = colRange.lowerBound();
        final int maxColumn = colRange.upperBound();
        visDimensions = new VisibleDimensions(minRow, maxRow, minColumn, maxColumn);
    }

    /**
     * Reset the zoom level to the default.
     */
    @Override
    public void resetZoom() {
        final int old = _zoomLevel;
        _zoomLevel = DEFAULT_ZOOM_LEVEL;
        for (final GraphicalParamsListener listener : gpListeners) {
            listener.tileSizeChanged(old, _zoomLevel);
        }
        fixVisibility();
    }

    /**
     * The point currently pointed to by the scroll-bars.
     */
    @Override
    public Point getCursor() {
        return cursorPoint;
    }

    @Override
    public void setCursor(final Point cursor) {
        final Point oldCursor = cursorPoint;
        cursorPoint = cursor;
        scs.fireCursorChanges(oldCursor, cursor);
    }

    /**
     * The currently selected point in the map.
     */
    @Override
    public Point getSelection() {
        return selPoint;
    }

    @Override
    public void setSelection(final Point selection) {
        final Point oldSel = selPoint;
        selPoint = selection;
        if (selection.isValid()) {
            cursorPoint = selection;
        } else {
            cursorPoint = new Point(Math.max(selection.row(), 0),
                    Math.max(selection.column(), 0));
        }
        scs.fireChanges(oldSel, selPoint);
        // TODO: why not fireCursorChanges() as well?
        fixVisibility();
    }

    /**
     * The point in the map the user has just right-clicked on, if any.
     */
    @Override
    public @Nullable Point getInteraction() {
        return interactionPoint;
    }

    @Override
    public void setInteraction(final @Nullable Point interaction) {
        interactionPoint = interaction;
        scs.fireInteraction();
    }

    /**
     * Clear the selection.
     */
    public void clearSelection() {
        setSelection(Point.INVALID_POINT);
        setInteraction(null);
    }

    @Override
    public void addSelectionChangeListener(final SelectionChangeListener listener) {
        scs.addSelectionChangeListener(listener);
    }

    @Override
    public void removeSelectionChangeListener(final SelectionChangeListener listener) {
        scs.removeSelectionChangeListener(listener);
    }

    @Override
    public void addGraphicalParamsListener(final GraphicalParamsListener listener) {
        gpListeners.add(listener);
    }

    @Override
    public void removeGraphicalParamsListener(final GraphicalParamsListener listener) {
        gpListeners.remove(listener);
    }

    @Override
    public String toString() {
        return "ViewerModel for " + Optional.ofNullable(getMap().getFilename())
                .map(Path::toString).orElse("an unsaved map");
    }

    /**
     * Set the map and its filename, and also clear the selection and reset
     * the visible dimensions and the zoom level.
     */
    @Override
    public void setMap(final IMutableMapNG newMap) {
        super.setMap(newMap);
        clearSelection();
        visDimensions = new VisibleDimensions(0, newMap.getDimensions().rows() - 1, 0,
                newMap.getDimensions().columns() - 1);
        resetZoom();
    }

    /**
     * Set whether a tile is mountainous.
     */
    @Override
    public void setMountainous(final Point location, final boolean mountainous) {
        getRestrictedMap().setMountainous(location, mountainous);
        setMapModified(true); // TODO: Only set the flag if this was a change?
    }

    /**
     * Add a fixture to the map at a point.
     */
    @Override
    public void addFixture(final Point location, final TileFixture fixture) {
        getRestrictedMap().addFixture(location, fixture);
        setMapModified(true); // TODO: If addFixture() returns Boolean, only set this flag if this was a change?
    }

    /**
     * Remove a fixture from the map at a point.
     */
    @Override
    public void removeMatchingFixtures(final Point location, final Predicate<TileFixture> condition) {
        for (final TileFixture fixture : getMap().getFixtures(location).stream().filter(condition).toList()) { // TODO: try to avoid collector step (forEach(lambda))
            getRestrictedMap().removeFixture(location, fixture);
        }
        setMapModified(true); // TODO: Only set the flag if this was a change?
    }

    /**
     * Add a bookmark at the given location.
     */
    @Override
    public void addBookmark(final Point location) {
        getRestrictedMap().addBookmark(location);
        setMapModified(true); // TODO: Only set the flag if this was a change?
    }

    /**
     * Remove a bookmark at the current location.
     */
    @Override
    public void removeBookmark(final Point location) {
        getRestrictedMap().removeBookmark(location);
        setMapModified(true); // TODO: Only set the flag if this was a change?
    }

    /**
     * Add a river at a location.
     */
    @Override
    public void addRiver(final Point location, final River river) {
        getRestrictedMap().addRivers(location, river);
        setMapModified(true); // TODO: Only set the flag if this was a change?
    }

    /**
     * Remove a river at a location.
     */
    @Override
    public void removeRiver(final Point location, final River river) {
        getRestrictedMap().removeRivers(location, river);
        setMapModified(true); // TODO: Only set the flag if this was a change?
    }

    /**
     * Set the map's terrain type at the given point.
     */
    @Override
    public void setBaseTerrain(final Point location, final @Nullable TileType terrain) {
        getRestrictedMap().setBaseTerrain(location, terrain);
        setMapModified(true); // TODO: Only set the flag if this was a change?
    }

    /**
     * Move a unit-member from one unit to another.
     */
    @Override
    public void moveMember(final UnitMember member, final IUnit old, final IUnit newOwner) {
        final IMutableUnit matchingOld = getMap().streamAllFixtures()
                .flatMap(ViewerModel::unflattenNonFortresses)
                .filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
                .filter(u -> old.owner().equals(u.owner()))
                .filter(u -> old.getKind().equals(u.getKind()))
                .filter(u -> old.getName().equals(u.getName()))
                .filter(u -> old.getId() == u.getId()).findAny().orElse(null);
        final IMutableUnit matchingNew = getMap().streamAllFixtures()
                .flatMap(ViewerModel::unflattenNonFortresses)
                .filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
                .filter(u -> newOwner.owner().equals(u.owner()))
                .filter(u -> newOwner.getKind().equals(u.getKind()))
                .filter(u -> newOwner.getName().equals(u.getName()))
                .filter(u -> newOwner.getId() == u.getId()).findAny().orElse(null);
        final UnitMember matchingMember = Optional.ofNullable(matchingOld).map(FixtureIterable::stream).orElse(Stream.empty())
                .filter(member::equals).findAny().orElse(null); // TODO: equals() isn't ideal for finding a matching member ...
        if (matchingOld != null && matchingMember != null && matchingNew != null) {
            matchingOld.removeMember(matchingMember);
            matchingNew.addMember(matchingMember);
            getRestrictedMap().setModified(true);
        }
    }

    private static Predicate<Pair<Point, IFixture>> unitMatching(final IUnit unit) {
        return entry -> {
            final IFixture fixture = entry.getValue1();
            return fixture instanceof final IUnit u && fixture.getId() == unit.getId() &&
                    u.owner().equals(unit.owner());
        };
    }

    /**
     * Remove the given unit from the map. It must be empty, and may be
     * required to be owned by the current player. The operation will also
     * fail if "matching" units differ in name or kind from the provided
     * unit.  Returns true if the preconditions were met and the unit
     * was removed, and false otherwise. To make an edge case explicit,
     * if there are no matching units in any map the method returns false.
     */
    @Override
    public boolean removeUnit(final IUnit unit) {
        LovelaceLogger.trace("In ViewerModel.removeUnit()");
        final Pair<Point, IFixture> pair = getMap().streamLocations()
                .flatMap(l -> getMap().getFixtures(l).stream()
                        .map(f -> Pair.<Point, IFixture>with(l, f)))
                .flatMap(ViewerModel::flattenEntries).filter(unitMatching(unit))
                .findAny().orElse(null);
        if (pair == null) {
            LovelaceLogger.trace("No matching units");
            return false;
        } else {
            final Point location = pair.getValue0();
            final IUnit fixture = (IUnit) pair.getValue1();
            LovelaceLogger.trace("Map has matching unit");
            if (fixture.getKind().equals(unit.getKind()) &&
                    fixture.getName().equals(unit.getName()) &&
                    fixture.isEmpty()) {
                LovelaceLogger.trace("Matching unit meets preconditions");
                if (getMap().getFixtures(location).contains(fixture)) {
                    getRestrictedMap().removeFixture(location, fixture);
                    LovelaceLogger.trace("Finished removing matching unit from map");
                    return true;
                } else {
                    for (final IMutableFortress fort : getMap().getFixtures(location).stream()
                            .filter(IMutableFortress.class::isInstance)
                            .map(IMutableFortress.class::cast).toList()) {
                        if (fort.stream().anyMatch(Predicate.isEqual(fixture))) {
                            fort.removeMember(fixture);
                            getRestrictedMap().setModified(true);
                            LovelaceLogger.trace(
                                    "Finished removing matching unit from map");
                            return true;
                        }
                    }
                    LovelaceLogger.warning(
                            "Failed to find unit to remove that we thought might be in a fortress");
                    return false;
                }
            } else {
                LovelaceLogger.warning(
                        "Matching unit in %s fails preconditions for removal",
                        Optional.ofNullable(getMap().getFilename())
                                .map(Path::toString).orElse("an unsaved map"));
                return false;
            }
        }
    }

    @Override
    public void addUnitMember(final IUnit unit, final UnitMember member) {
        final IMutableUnit matching = getMap().streamAllFixtures()
                .flatMap(ViewerModel::unflattenNonFortresses)
                .filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
                // TODO: Implement a matchingValue-like helper method in lovelace.util,
                // taking a base object of the same type and a *series* of accessors all of
                // which must produce an equal value from the two objects. Wouldn't work
                // for primitive-returning // properties, though.
                .filter(u -> u.owner().equals(unit.owner()))
                .filter(u -> u.getName().equals(unit.getName()))
                .filter(u -> u.getKind().equals(unit.getKind()))
                .filter(u -> u.getId() == unit.getId()).findAny().orElse(null);
        if (matching != null) {
            matching.addMember(member.copy(IFixture.CopyBehavior.KEEP));
            getRestrictedMap().setModified(true);
        }
    }

    @Override
    public boolean renameItem(final HasName item, final String newName) {
        if (item instanceof final IUnit unit) {
            final IUnit matching = getMap().streamAllFixtures()
                    .flatMap(ViewerModel::unflattenNonFortresses)
                    .filter(IUnit.class::isInstance).map(IUnit.class::cast)
                    .filter(u -> u.owner().equals(unit.owner()))
                    .filter(u -> u.getName().equals(item.getName()))
                    .filter(u -> u.getKind().equals(unit.getKind()))
                    .filter(u -> u.getId() == unit.getId())
                    .findAny().orElse(null);
            if (matching instanceof final HasMutableName hmn) {
                hmn.setName(newName);
                getRestrictedMap().setModified(true);
                return true;
            } else {
                LovelaceLogger.warning("Unable to find unit to rename");
                return false;
            }
        } else if (item instanceof final UnitMember um) {
            final HasMutableName matching = getMap().streamAllFixtures()
                    .flatMap(ViewerModel::unflattenNonFortresses)
                    .filter(IUnit.class::isInstance).map(IUnit.class::cast)
                    .filter(u -> getMap().getPlayers().getCurrentPlayer()
                            .equals(u.owner()))
                    .flatMap(FixtureIterable::stream)
                    .filter(HasMutableName.class::isInstance)
                    .map(HasMutableName.class::cast)
                    .filter(m -> m.getName().equals(item.getName()))
                    .filter(m -> ((UnitMember) m).getId() == // TODO: Move above cast, to resolve spurious warnings
                            um.getId())
                    .findAny().orElse(null); // FIXME: We should have a firmer identification than just name and ID
            if (matching == null) {
                LovelaceLogger.warning("Unable to find unit member to rename");
                return false;
            } else {
                matching.setName(newName);
                getRestrictedMap().setModified(true);
                return true;
            }
        } else { // FIXME: Fortresses are the obvious case here ...
            LovelaceLogger.warning("Unable to find item to rename");
            return false;
        }
    }

    @Override
    public boolean changeKind(final HasKind item, final String newKind) {
        if (item instanceof final IUnit unit) {
            // TODO: Extract this pipeline to a method
            final IUnit matching = getMap().streamAllFixtures()
                    .flatMap(ViewerModel::unflattenNonFortresses)
                    .filter(IUnit.class::isInstance).map(IUnit.class::cast)
                    .filter(u -> u.owner().equals(((IUnit) item).owner()))
                    .filter(u -> u.getName().equals(unit.getName()))
                    .filter(u -> u.getKind().equals(item.getKind()))
                    .filter(u -> u.getId() == unit.getId())
                    .findAny().orElse(null);
            if (matching instanceof final HasMutableKind hmk) {
                hmk.setKind(newKind);
                getRestrictedMap().setModified(true);
                return true;
            } else {
                LovelaceLogger.warning("Unable to find unit to change kind");
                return false;
            }
        } else if (item instanceof final UnitMember um) {
            // TODO: Extract parts of this pipeline to a method, passing in the class to narrow
            // to and relevant predicate(s).
            final HasMutableKind matching = getMap().streamAllFixtures()
                    .flatMap(ViewerModel::unflattenNonFortresses)
                    .filter(IUnit.class::isInstance).map(IUnit.class::cast)
                    .filter(u -> getMap().getPlayers().getCurrentPlayer()
                            .equals(u.owner()))
                    .flatMap(FixtureIterable::stream)
                    .filter(HasMutableKind.class::isInstance)
                    .map(HasMutableKind.class::cast)
                    .filter(m -> m.getKind().equals(item.getKind()))
                    .filter(m -> ((UnitMember) m).getId() == // TODO: Move above cast line
                            um.getId())
                    .findAny().orElse(null); // FIXME: We should have a firmer identification than just kind and ID
            if (matching == null) {
                LovelaceLogger.warning("Unable to find unit member to change kind");
                return false;
            } else {
                matching.setKind(newKind);
                getRestrictedMap().setModified(true);
                return true;
            }
        } else { // FIXME: Fortresses are the obvious type
            LovelaceLogger.warning("Unable to find item to change kind");
            return false;
        }
    }

    @Override
    public void dismissUnitMember(final UnitMember member) {
        for (final IMutableUnit unit : getMap().streamAllFixtures()
                .flatMap(ViewerModel::unflattenNonFortresses)
                .filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
                .filter(u -> getMap().getPlayers().getCurrentPlayer().equals(u.owner())).toList()) {
            final UnitMember matching = unit.stream().filter(Predicate.isEqual(member)) // FIXME: equals() will really not do here ...
                    .findAny().orElse(null);
            if (matching != null) {
                unit.removeMember(matching);
                dismissedMembers.add(member);
                getRestrictedMap().setModified(true);
                break; // TODO: Why not just return?
            }
        }
    }

    @Override
    public Iterable<UnitMember> getDismissed() {
        return Collections.unmodifiableSet(dismissedMembers);
    }

    @Override
    public boolean addSibling(final UnitMember existing, final UnitMember sibling) {
        for (final IMutableUnit unit : getMap().streamAllFixtures()
                .flatMap(ViewerModel::unflattenNonFortresses)
                .filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
                .filter(u -> getMap().getPlayers().getCurrentPlayer().equals(u.owner())).toList()) {
            if (unit.stream().anyMatch(Predicate.isEqual(existing))) { // TODO: look beyond equals() for matching-in-existing?
                unit.addMember(sibling.copy(IFixture.CopyBehavior.KEEP));
                getRestrictedMap().setModified(true);
                return true;
            }
        }
        return false;
    }

    /**
     * Change the owner of the given item in all maps. Returns true if
     * this succeeded in any map, false otherwise.
     */
    @Override
    public boolean changeOwner(final HasOwner item, final Player newOwner) {
        final HasMutableOwner matching = getMap().streamAllFixtures()
                .flatMap(ViewerModel::flattenIncluding).flatMap(ViewerModel::flattenIncluding)
                .filter(HasMutableOwner.class::isInstance).map(HasMutableOwner.class::cast)
                .filter(item::equals) // TODO: equals() is not the best way to find it ...
                .findAny().orElse(null);
        if (matching != null) {
            if (StreamSupport.stream(getMap().getPlayers().spliterator(), true)
                    .noneMatch(newOwner::equals)) { // TODO: looser identification?
                getRestrictedMap().addPlayer(newOwner);
            }
            matching.setOwner(getMap().getPlayers().getPlayer(newOwner.getPlayerId()));
            getRestrictedMap().setModified(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean sortFixtureContents(final IUnit fixture) {
        final IMutableUnit matching = getMap().streamAllFixtures()
                .flatMap(ViewerModel::unflattenNonFortresses)
                .filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
                .filter(u -> u.owner().equals(fixture.owner()))
                .filter(u -> u.getName().equals(fixture.getName()))
                .filter(u -> u.getKind().equals(fixture.getKind()))
                .filter(u -> u.getId() == fixture.getId())
                .findAny().orElse(null);
        if (matching != null) {
            matching.sortMembers();
            getRestrictedMap().setModified(true);
            return true;
        }
        return false;
    }

    @Override
    public void addUnit(final IUnit unit) {
        Point hqLoc = Point.INVALID_POINT;
        final Predicate<Object> isFortress = IFortress.class::isInstance;
        final Function<Object, IFortress> fortressCast = IFortress.class::cast;
        final Predicate<IFortress> matchingOwner = f -> f.owner().equals(unit.owner());
        for (final Point location : getMap().getLocations()) {
            final IFortress fortress = getMap().getFixtures(location).stream()
                    .filter(isFortress).map(fortressCast)
                    .filter(matchingOwner)
                    .findAny().orElse(null);
            if (fortress != null) {
                if ("HQ".equals(fortress.getName())) {
                    hqLoc = location;
                    break;
                } else if (!hqLoc.isValid()) {
                    hqLoc = location;
                }
            }
        }
        getRestrictedMap().addFixture(hqLoc, unit);
    }
}
