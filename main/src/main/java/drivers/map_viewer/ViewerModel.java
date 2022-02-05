package drivers.map_viewer;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.nio.file.Path;
import java.util.logging.Logger;
import common.map.fixtures.FixtureIterable;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import java.util.function.Predicate;
import org.javatuples.Pair;
import java.util.List;
import java.util.ArrayList;

import drivers.common.SimpleDriverModel;
import drivers.common.IDriverModel;
import drivers.common.SelectionChangeListener;
import common.map.Point;
import common.map.HasKind;
import common.map.HasMutableKind;
import common.map.HasMutableName;
import common.map.HasMutableOwner;
import common.map.IFixture;
import common.map.IMutableMapNG;
import common.map.Player;
import common.map.River;
import common.map.TileFixture;
import common.map.TileType;

import common.map.fixtures.UnitMember;

import common.map.fixtures.mobile.IMutableUnit;
import common.map.fixtures.mobile.IUnit;

import common.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.IMutableFortress;

/**
 * A class to encapsulate the various model-type things views need to do with maps.
 *
 * TODO: Tests
 */
public class ViewerModel extends SimpleDriverModel implements IViewerModel {
	private static final Logger LOGGER = Logger.getLogger(ViewerModel.class.getName());
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
		if (entry.getValue1() instanceof IFortress) {
			return ((IFortress) entry.getValue1()).stream().map(m -> Pair.with(entry.getValue0(), (IFixture) m));
		} else {
			return Stream.of(entry);
		}
	}

	/**
	 * If {@link fixture} is a {@link IFortress fortress}, return it; otherwise,
	 * return a Singleton containing it. This is intended to be used in
	 * {@link Iterable#flatMap}.
	 */
	private static Stream<IFixture> unflattenNonFortresses(final TileFixture fixture) {
		if (fixture instanceof IFortress) {
			return ((IFortress) fixture).stream().map(IFixture.class::cast);
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
			int oldZoom = _zoomLevel;
			_zoomLevel++;
			int newZoom = _zoomLevel;
			for (GraphicalParamsListener listener : gpListeners) {
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
			int oldZoom = _zoomLevel;
			_zoomLevel--;
			int newZoom = _zoomLevel;
			for (GraphicalParamsListener listener : gpListeners) {
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
	@Nullable
	private Point interactionPoint = null;

	/**
	 * The visible dimensions of the map.
	 */
	private VisibleDimensions visDimensions;

	/**
	 * @param theMap The initial map
	 */
	public ViewerModel(final IMutableMapNG theMap) {
		super(theMap);
		visDimensions = new VisibleDimensions(0, theMap.getDimensions().getRows() - 1,
			0, theMap.getDimensions().getColumns() - 1);
	}

	// TODO: Provide static method copyConstructor() calling this?
	public ViewerModel(final IDriverModel model) {
		super(model.getRestrictedMap());
		if (model instanceof IViewerModel) {
			IViewerModel vm = (IViewerModel) model;
			visDimensions = vm.getVisibleDimensions();
			selPoint = vm.getSelection();
			_zoomLevel = vm.getZoomLevel();
		} else {
			visDimensions = new VisibleDimensions(0, model.getMapDimensions().getRows() - 1,
				0, model.getMapDimensions().getColumns() - 1);
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
			VisibleDimensions oldDimensions = visDimensions;
			visDimensions = visibleDimensions;
			// We notify listeners after the change, since one object's
			// dimensionsChanged() delegates to repaint(). (The other uses the parameter
			// we provide for robustness.)
			for (GraphicalParamsListener listener : gpListeners) {
				listener.dimensionsChanged(oldDimensions, visibleDimensions);
			}
		}
	}

	private void fixVisibility() {
		Point currSelection = selPoint;
		VisibleDimensions currDims = visDimensions;
		final int row;
		if (currSelection.getRow() < 0) {
			row = 0;
		} else if (currSelection.getRow() >= getMap().getDimensions().getRows()) {
			row = getMap().getDimensions().getRows() - 1;
		} else {
			row = currSelection.getRow();
		}
		final int column;
		if (currSelection.getColumn() < 0) {
			column = 0;
		} else if (currSelection.getColumn() >= getMap().getDimensions().getColumns()) {
			column = getMap().getDimensions().getColumns() - 1;
		} else {
			column = currSelection.getColumn();
		}
		final int minRow;
		final int maxRow;
		if (currDims.getRows().contains(row)) {
			minRow = currDims.getMinimumRow();
			maxRow = currDims.getMaximumRow();
		} else if (currDims.getMinimumRow() > row &&
				currDims.getMinimumRow() - row <= JUMP_INTERVAL) {
			minRow = row;
			maxRow = currDims.getMaximumRow() - (currDims.getMinimumRow() - row);
		} else if (currDims.getMaximumRow() < row &&
				row - currDims.getMaximumRow() <= JUMP_INTERVAL) {
			minRow = currDims.getMinimumRow() + (row - currDims.getMaximumRow());
			maxRow = row;
		} else if (row >= 0 && row < currDims.getHeight()) {
			minRow = 0;
			maxRow = currDims.getHeight() - 1;
		} else if (row >= (getMap().getDimensions().getRows() - currDims.getHeight()) &&
				row < getMap().getDimensions().getRows()) {
			minRow = getMap().getDimensions().getRows() - currDims.getHeight();
			maxRow = getMap().getDimensions().getRows() - 1;
		} else {
			minRow = row - currDims.getHeight() / 2;
			maxRow = minRow + currDims.getHeight() - 1;
		}
		final int minColumn;
		final int maxColumn;
		if (currDims.getColumns().contains(column)) {
			minColumn = currDims.getMinimumColumn();
			maxColumn = currDims.getMaximumColumn();
		} else if (currDims.getMinimumColumn() > column &&
				currDims.getMinimumColumn() - column <= JUMP_INTERVAL) {
			minColumn = column;
			maxColumn = currDims.getMaximumColumn() - (currDims.getMinimumColumn() - column);
		} else if (currDims.getMaximumColumn() < column &&
				column - currDims.getMaximumColumn() <= JUMP_INTERVAL) {
			minColumn = currDims.getMinimumColumn() + (column - currDims.getMaximumColumn());
			maxColumn = column;
		} else if (column >= 0 && column < currDims.getWidth()) {
			minColumn = 0;
			maxColumn = currDims.getWidth() - 1;
		} else if (column >= getMap().getDimensions().getColumns() - currDims.getWidth() &&
				column < getMap().getDimensions().getColumns()) {
			minColumn = getMap().getDimensions().getColumns() - currDims.getWidth();
			maxColumn = getMap().getDimensions().getColumns() - 1;
		} else {
			minColumn = column - currDims.getWidth() / 2;
			maxColumn = minColumn + currDims.getWidth() - 1;
		}
		visDimensions = new VisibleDimensions(minRow, maxRow, minColumn, maxColumn);
	}

	/**
	 * Reset the zoom level to the default.
	 */
	@Override
	public void resetZoom() {
		final int old = _zoomLevel;
		_zoomLevel = DEFAULT_ZOOM_LEVEL;
		for (GraphicalParamsListener listener : gpListeners) {
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
		Point oldCursor = cursorPoint;
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
		Point oldSel = selPoint;
		selPoint = selection;
		if (selection.isValid()) {
			cursorPoint = selection;
		} else {
			cursorPoint = new Point(Math.max(selection.getRow(), 0),
				Math.max(selection.getColumn(), 0));
		}
		scs.fireChanges(oldSel, selPoint);
		// TODO: why not fireCursorChanges() as well?
		fixVisibility();
	}

	/**
	 * The point in the map the user has just right-clicked on, if any.
	 */
	@Override
	@Nullable
	public Point getInteraction() {
		return interactionPoint;
	}

	@Override
	public void setInteraction(@Nullable final Point interaction) {
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
		visDimensions = new VisibleDimensions(0, newMap.getDimensions().getRows() - 1, 0,
			newMap.getDimensions().getColumns() - 1);
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
		for (TileFixture fixture : getMap().getFixtures(location).stream().filter(condition)
				.collect(Collectors.toList())) { // TODO: try to avoid collector step
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
	public void setBaseTerrain(final Point location, @Nullable final TileType terrain) {
		getRestrictedMap().setBaseTerrain(location, terrain);
		setMapModified(true); // TODO: Only set the flag if this was a change?
	}

	/**
	 * Move a unit-member from one unit to another.
	 */
	@Override
	public void moveMember(final UnitMember member, final IUnit old, final IUnit newOwner) {
		IMutableUnit matchingOld = getMap().streamAllFixtures()
			.flatMap(ViewerModel::unflattenNonFortresses)
			.filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
			.filter(u -> old.getOwner().equals(u.getOwner()))
			.filter(u -> old.getKind().equals(u.getKind()))
			.filter(u -> old.getName().equals(u.getName()))
			.filter(u -> old.getId() == u.getId()).findAny().orElse(null);
		IMutableUnit matchingNew = getMap().streamAllFixtures()
			.flatMap(ViewerModel::unflattenNonFortresses)
			.filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
			.filter(u -> newOwner.getOwner().equals(u.getOwner()))
			.filter(u -> newOwner.getKind().equals(u.getKind()))
			.filter(u -> newOwner.getName().equals(u.getName()))
			.filter(u -> newOwner.getId() == u.getId()).findAny().orElse(null);
		UnitMember matchingMember = Optional.ofNullable(matchingOld).map(u -> u.stream()).orElse(Stream.empty())
			.filter(member::equals).findAny().orElse(null); // TODO: equals() isn't ideal for finding a matching member ...
		if (matchingOld != null && matchingMember != null && matchingNew != null) {
			matchingOld.removeMember(matchingMember);
			matchingNew.addMember(matchingMember);
			getRestrictedMap().setModified(true);
		}
	}

	private Predicate<Pair<Point, IFixture>> unitMatching(final IUnit unit) {
		return entry -> {
			IFixture fixture = entry.getValue1();
			return fixture instanceof IUnit && fixture.getId() == unit.getId() &&
				((IUnit) fixture).getOwner().equals(unit.getOwner());
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
		LOGGER.finer("In ViewerModel.removeUnit()");
		Pair<Point, IFixture> pair = getMap().streamLocations()
				.flatMap(l -> getMap().getFixtures(l).stream()
					.map(f -> Pair.<Point, IFixture>with(l, f)))
				.flatMap(ViewerModel::flattenEntries).filter(unitMatching(unit))
				.findAny().orElse(null);
		if (pair != null) {
			Point location = pair.getValue0();
			IUnit fixture = (IUnit) pair.getValue1();
			LOGGER.finer("Map has matching unit");
			if (fixture.getKind().equals(unit.getKind()) &&
					fixture.getName().equals(unit.getName()) &&
					fixture.isEmpty()) {
				LOGGER.finer("Matching unit meets preconditions");
				if (getMap().getFixtures(location).contains(fixture)) {
					getRestrictedMap().removeFixture(location, fixture);
					LOGGER.finer("Finished removing matching unit from map");
					return true;
				} else {
					for (IMutableFortress fort : getMap().getFixtures(location).stream()
							.filter(IMutableFortress.class::isInstance)
							.map(IMutableFortress.class::cast)
							.collect(Collectors.toList())) {
						if (fort.stream().anyMatch(fixture::equals)) {
							fort.removeMember(fixture);
							getRestrictedMap().setModified(true);
							LOGGER.finer(
								"Finished removing matching unit from map");
							return true;
						}
					}
					LOGGER.warning(
						"Failed to find unit to remove that we thought might be in a fortress");
					return false;
				}
			} else {
				LOGGER.warning(String.format(
					"Matching unit in %s fails preconditions for removal",
					Optional.ofNullable(getMap().getFilename())
						.map(Path::toString).orElse("an unsaved map")));
				return false;
			}
		} else {
			LOGGER.finer("No matching units");
			return false;
		}
	}

	@Override
	public void addUnitMember(final IUnit unit, final UnitMember member) {
		IMutableUnit matching = getMap().streamAllFixtures()
			.flatMap(ViewerModel::unflattenNonFortresses)
			.filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
			// TODO: Implement a matchingValue-like helper method in lovelace.util,
			// taking a base object of the same type and a *series* of accessors all of
			// which must produce an equal value from the two objects. Wouldn't work
			// for primitive-returning // properties, though.
			.filter(u -> u.getOwner().equals(unit.getOwner()))
			.filter(u -> u.getName().equals(unit.getName()))
			.filter(u -> u.getKind().equals(unit.getKind()))
			.filter(u -> u.getId() == unit.getId()).findAny().orElse(null);
		if (matching != null) {
			matching.addMember(member.copy(false));
			getRestrictedMap().setModified(true);
		}
	}

	@Override
	public boolean renameItem(final HasMutableName item, final String newName) {
		if (item instanceof IUnit) {
			IUnit matching = getMap().streamAllFixtures()
				.flatMap(ViewerModel::unflattenNonFortresses)
				.filter(IUnit.class::isInstance).map(IUnit.class::cast)
				.filter(u -> u.getOwner().equals(((IUnit) item).getOwner()))
				.filter(u -> u.getName().equals(item.getName()))
				.filter(u -> u.getKind().equals(((IUnit) item).getKind()))
				.filter(u -> u.getId() == ((IUnit) item).getId())
				.findAny().orElse(null);
			if (matching instanceof HasMutableName) {
				((HasMutableName) matching).setName(newName);
				getRestrictedMap().setModified(true);
				return true;
			} else {
				LOGGER.warning("Unable to find unit to rename");
				return false;
			}
		} else if (item instanceof UnitMember) {
			HasMutableName matching = getMap().streamAllFixtures()
					.flatMap(ViewerModel::unflattenNonFortresses)
					.filter(IUnit.class::isInstance).map(IUnit.class::cast)
					.filter(u -> getMap().getPlayers().getCurrentPlayer()
						.equals(u.getOwner()))
					.flatMap(u -> u.stream())
					.filter(HasMutableName.class::isInstance)
					.map(HasMutableName.class::cast)
					.filter(m -> m.getName().equals(item.getName()))
					.filter(m -> ((UnitMember) m).getId() ==
						((UnitMember) item).getId())
					.findAny().orElse(null); // FIXME: We should have a firmer identification than just name and ID
			if (matching != null) {
				matching.setName(newName);
				getRestrictedMap().setModified(true);
				return true;
			} else {
				LOGGER.warning("Unable to find unit member to rename");
				return false;
			}
		} else { // FIXME: Fortresses are the obvious case here ...
			LOGGER.warning("Unable to find item to rename");
			return false;
		}
	}

	@Override
	public boolean changeKind(final HasKind item, final String newKind) {
		if (item instanceof IUnit) {
			// TODO: Extract this pipeline to a method
			IUnit matching = getMap().streamAllFixtures()
				.flatMap(ViewerModel::unflattenNonFortresses)
				.filter(IUnit.class::isInstance).map(IUnit.class::cast)
				.filter(u -> u.getOwner().equals(((IUnit) item).getOwner()))
				.filter(u -> u.getName().equals(((IUnit) item).getName()))
				.filter(u -> u.getKind().equals(item.getKind()))
				.filter(u -> u.getId() == ((IUnit) item).getId())
				.findAny().orElse(null);
			if (matching instanceof HasMutableKind) {
				((HasMutableKind) matching).setKind(newKind);
				getRestrictedMap().setModified(true);
				return true;
			} else {
				LOGGER.warning("Unable to find unit to change kind");
				return false;
			}
		} else if (item instanceof UnitMember) {
			// TODO: Extract parts of this pipeline to a method, passing in the class to narrow
			// to and relevant predicate(s).
			HasMutableKind matching = getMap().streamAllFixtures()
					.flatMap(ViewerModel::unflattenNonFortresses)
					.filter(IUnit.class::isInstance).map(IUnit.class::cast)
					.filter(u -> getMap().getPlayers().getCurrentPlayer()
						.equals(u.getOwner()))
					.flatMap(u -> u.stream())
					.filter(HasMutableKind.class::isInstance)
					.map(HasMutableKind.class::cast)
					.filter(m -> m.getKind().equals(item.getKind()))
					.filter(m -> ((UnitMember) m).getId() ==
						((UnitMember) item).getId())
					.findAny().orElse(null); // FIXME: We should have a firmer identification than just kind and ID
			if (matching != null) {
				matching.setKind(newKind);
				getRestrictedMap().setModified(true);
				return true;
			} else {
				LOGGER.warning("Unable to find unit member to change kind");
				return false;
			}
		} else { // FIXME: Fortresses are the obvious type
			LOGGER.warning("Unable to find item to change kind");
			return false;
		}
	}

	// TODO: Keep a list of dismissed members
	@Override
	public void dismissUnitMember(final UnitMember member) {
		for (IMutableUnit unit : getMap().streamAllFixtures()
				.flatMap(ViewerModel::unflattenNonFortresses)
				.filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
				.filter(u -> getMap().getPlayers().getCurrentPlayer().equals(u.getOwner()))
				.collect(Collectors.toList())) {
			UnitMember matching = unit.stream().filter(member::equals) // FIXME: equals() will really not do here ...
				.findAny().orElse(null);
			if (matching != null) {
				unit.removeMember(matching);
				getRestrictedMap().setModified(true);
				break; // TODO: Why not just return?
			}
		}
	}

	@Override
	public boolean addSibling(final UnitMember existing, final UnitMember sibling) {
		for (IMutableUnit unit : getMap().streamAllFixtures()
				.flatMap(ViewerModel::unflattenNonFortresses)
				.filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
				.filter(u -> getMap().getPlayers().getCurrentPlayer().equals(u.getOwner()))
				.collect(Collectors.toList())) {
			if (unit.stream().anyMatch(existing::equals)) { // TODO: look beyond equals() for matching-in-existing?
				unit.addMember(sibling.copy(false));
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
	public boolean changeOwner(final HasMutableOwner item, final Player newOwner) {
		HasMutableOwner matching = getMap().streamAllFixtures()
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
		IMutableUnit matching = getMap().streamAllFixtures()
			.flatMap(ViewerModel::unflattenNonFortresses)
			.filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
			.filter(u -> u.getOwner().equals(fixture.getOwner()))
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
		for (Point location : getMap().getLocations()) {
			IFortress fortress = getMap().getFixtures(location).stream()
				.filter(IFortress.class::isInstance).map(IFortress.class::cast)
				.filter(f -> f.getOwner().equals(unit.getOwner()))
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
