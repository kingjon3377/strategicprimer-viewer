package drivers.map_viewer;

import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.Collection;
import lovelace.util.Range;
import common.map.Point;
import common.map.River;
import common.map.TileFixture;
import common.map.TileType;
import javax.swing.ListModel;
import drivers.common.SelectionChangeListener;
import java.util.ArrayList;
import java.util.List;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.AnimalTracks;
import common.map.fixtures.mobile.IUnit;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.BiPredicate;
import java.util.function.BiConsumer;
import java.util.Comparator;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * A model for the list-based representation of the contents of a tile.
 */
public class FixtureListModel implements ListModel<TileFixture>, SelectionChangeListener {
	private static final Logger LOGGER = Logger.getLogger(FixtureListModel.class.getName());
	private final Function<Point, Collection<TileFixture>> fixturesSource;
	private final Function<Point, @Nullable TileType> terrainSource;
	private final Function<Point, Collection<River>> riversSource;
	private final Predicate<Point> mountainSource;
	private final Function<Point, @Nullable AnimalTracks> tracksSource;
	private final @Nullable BiConsumer<Point, TileType> terrainSink;
	private final @Nullable BiConsumer<Point, River[]> addRivers;
	private final @Nullable BiConsumer<Point, Boolean> mountainSink;
	private final @Nullable BiPredicate<Point, TileFixture> addFixtureLambda;
	private final @Nullable BiConsumer<Point, River[]> removeRivers;
	private final @Nullable BiConsumer<Point, TileFixture> removeFixture;
	private final Comparator<TileFixture> comparator;

	public FixtureListModel(Function<Point, Collection<TileFixture>> fixturesSource,
			Function<Point, @Nullable TileType> terrainSource,
			Function<Point, Collection<River>> riversSource, Predicate<Point> mountainSource,
			Function<Point, @Nullable AnimalTracks> tracksSource,
			@Nullable BiConsumer<Point, TileType> terrainSink,
			@Nullable BiConsumer<Point, River[]> addRivers,
			@Nullable BiConsumer<Point, Boolean> mountainSink,
			@Nullable BiPredicate<Point, TileFixture> addFixtureLambda,
			@Nullable BiConsumer<Point, River[]> removeRivers,
			@Nullable BiConsumer<Point, TileFixture> removeFixture,
			Comparator<TileFixture> comparator) {
		this.fixturesSource = fixturesSource;
		this.terrainSource = terrainSource;
		this.riversSource = riversSource;
		this.mountainSource = mountainSource;
		this.tracksSource = tracksSource;
		this.terrainSink = terrainSink;
		this.addRivers = addRivers;
		this.mountainSink = mountainSink;
		this.addFixtureLambda = addFixtureLambda;
		this.removeRivers = removeRivers;
		this.removeFixture = removeFixture;
		this.comparator = comparator;
	}

	/**
	 * The currently selected point.
	 */
	private Point point = Point.INVALID_POINT;

	private List<TileFixture> cachedTerrainList = Collections.emptyList();

	@Override
	public void selectedUnitChanged(@Nullable IUnit oldSelection, @Nullable IUnit newSelection) {}

	@Override
	public void cursorPointChanged(@Nullable Point old, Point newCursor) {}

	/**
	 * Any animal tracks that have been "added" to the current tile but kept out of the map.
	 */
	private final List<AnimalTracks> currentTracks = new ArrayList<>();

	private final List<ListDataListener> listDataListeners = new ArrayList<>();

	@Override
	public void addListDataListener(ListDataListener listener) {
		listDataListeners.add(listener);
	}

	@Override
	public void removeListDataListener(ListDataListener listener) {
		listDataListeners.remove(listener);
	}

	@Override
	public int getSize() {
		return fixturesSource.apply(point).size() + currentTracks.size() + cachedTerrainList.size();
	}

	private void fireIntervalReplaced(Range oldRange, Range newRange) {
		ListDataEvent removeEvent = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED,
			oldRange.getLowerBound(), oldRange.getUpperBound());
		ListDataEvent addEvent = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED,
			newRange.getLowerBound(), newRange.getUpperBound());
		for (ListDataListener listener : listDataListeners) {
			listener.intervalRemoved(removeEvent);
			listener.intervalAdded(addEvent);
		}
	}

	private void fireContentsChanged(Range range) {
		ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED,
			range.getLowerBound(), range.getUpperBound());
		for (ListDataListener listener : listDataListeners) {
			listener.contentsChanged(event);
		}
	}

	private void fireIntervalAdded(Range range) {
		ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED,
			range.getLowerBound(), range.getUpperBound());
		for (ListDataListener listener : listDataListeners) {
			listener.intervalAdded(event);
		}
	}

	private void fireIntervalRemoved(Range range) {
		ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED,
			range.getLowerBound(), range.getUpperBound());
		for (ListDataListener listener : listDataListeners) {
			listener.intervalRemoved(event);
		}
	}

	@Override
	public void selectedPointChanged(@Nullable Point old, Point newPoint) {
		LOGGER.finer("Starting FixtureListModel.selectedPointChanged");
		int oldSize = getSize();
		cachedTerrainList = Collections.emptyList();
		TileType terrain = terrainSource.apply(newPoint);
		if (terrain != null) {
			cachedTerrainList = new ArrayList<TileFixture>(Collections.singleton(
				new TileTypeFixture(terrain)));
		}
		LOGGER.finer("FixtureListModel.selectedPointChanged: Accounted for base terrain");
		Collection<River> rivers = riversSource.apply(newPoint);
		if (rivers.iterator().hasNext()) {
			cachedTerrainList.add(new RiverFixture(rivers.stream().toArray(River[]::new)));
		}
		// TODO: Add support for roads
		LOGGER.finer("FixtureListModel.selectedPointChanged: Accounted for rivers");
		if (mountainSource.test(newPoint)) {
			cachedTerrainList.add(new MountainFixture());
		}
		LOGGER.finer("FixtureListModel.selectedPointChanged: Accounted for mountain");
		point = newPoint;
		currentTracks.clear();
		AnimalTracks tracks = tracksSource.apply(newPoint);
		if (tracks != null) {
			currentTracks.add(tracks);
		}
		LOGGER.finer("FixtureListModel.selectedPointChanged: Accounted for animal tracks");
		int newSize = getSize();
		LOGGER.finer("FixtureListModel.selectedPointChanged: About to notify listeners");
		fireIntervalReplaced(new Range(0, oldSize - 1), new Range(0, newSize - 1));
		LOGGER.finer("End of FixtureListModel.selectedPointChanged");
	}

	@Override
	public TileFixture getElementAt(int index) {
		List<TileFixture> main = new ArrayList<>(fixturesSource.apply(point));
		main.sort(comparator); // TODO: cache this?
		if (index < 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		} else if (index < cachedTerrainList.size()) {
			return cachedTerrainList.get(index);
		} else if (index - cachedTerrainList.size() < main.size()) {
			return main.get(index - cachedTerrainList.size());
		} else if (index - cachedTerrainList.size() - main.size() < currentTracks.size()) {
			return currentTracks.get(index - cachedTerrainList.size() - main.size());
		} else {
			throw new ArrayIndexOutOfBoundsException(index);
		}
	}

	private int adjustedIndex(int index) {
		return index + cachedTerrainList.size();
	}

	private static <U, T extends U> int indexOf(Collection<U> collection, T item) {
		if (collection instanceof List) {
			return ((List<U>) collection).indexOf(item);
		} else {
			return new ArrayList<U>(collection).indexOf(item);
		}
	}

	/**
	 * Returns true if the operation is accepted (succeeded), false if it
	 * is rejected (failed). For now no-op operations are treated as
	 * successes.
	 *
	 * TODO: Provide a way of pre-rejecting operations, so drag-and-drop
	 * failures can be warned about before the drop
	 */
	public boolean addFixture(TileFixture fixture) {
		if (fixture instanceof TileTypeFixture) {
			TileType existingTerrain = terrainSource.apply(point);
			TileTypeFixture ttf = (TileTypeFixture) fixture;
			if (existingTerrain != null) {
				if (existingTerrain.equals(ttf.getTileType())) {
					return true;
				} else if (terrainSink != null) {
					terrainSink.accept(point, ttf.getTileType());
					fireContentsChanged(new Range(0, 0));
					return true;
				} else {
					return false;
				}
			} else if (terrainSink != null) {
				terrainSink.accept(point, ttf.getTileType());
				fireIntervalAdded(new Range(0, 0));
				return true;
			} else {
				return false;
			}
		} else if (fixture instanceof RiverFixture) {
			Collection<River> existingRivers = riversSource.apply(point);
			RiverFixture rf = (RiverFixture) fixture;
			if (existingRivers.iterator().hasNext()) {
				Collection<River> coll = StreamSupport.stream(existingRivers.spliterator(),
					false).collect(Collectors.toList());
				if (coll.containsAll(rf.getRivers())) {
					return true;
				} else if (addRivers != null) {
					addRivers.accept(point,
						rf.getRivers().stream().toArray(River[]::new));
					int index = -1;
					for (int i = 0; i < cachedTerrainList.size(); i++) {
						if (cachedTerrainList.get(i) instanceof RiverFixture) {
							index = i;
							break;
						}
					}
					fireContentsChanged(new Range(index, index)); // TODO: move to after update?
					cachedTerrainList.set(index,
						new RiverFixture(riversSource.apply(point).stream()
							.toArray(River[]::new)));
					return true;
				} else {
					return false;
				}
			} else if (addRivers != null) {
				addRivers.accept(point,
					rf.getRivers().stream().toArray(River[]::new));
				int index = cachedTerrainList.size();
				cachedTerrainList.add(fixture);
				fireIntervalAdded(new Range(index, index));
				return true;
			} else {
				return false;
			} // TODO: Handle roads
		} else if (fixture instanceof MountainFixture) {
			if (mountainSource.test(point)) {
				return true;
			} else if (mountainSink != null) {
				int index = cachedTerrainList.size();
				mountainSink.accept(point, true);
				fireIntervalAdded(new Range(index, index));
				return true;
			} else {
				return false;
			}
		} else if (addFixtureLambda != null && addFixtureLambda.test(point, fixture)) {
			int index = indexOf(fixturesSource.apply(point), fixture);
			if (index >= 0) {
				int adjusted = adjustedIndex(index); // FIXME: Can this be right?
				fireIntervalAdded(new Range(adjusted, adjusted));
				return true;
			} else {
				return false; // TODO: This returns failure if a more-up-to-date version is already there
			}
		} else if (addFixtureLambda != null) {
			int index = indexOf(fixturesSource.apply(point), fixture);
			if (index >= 0) {
				int adjusted = adjustedIndex(index);
				fireContentsChanged(new Range(adjusted, adjusted));
				return true;
			} else {
				return false; // TODO: This returns failure if a more-up-to-date version is already there
			}
		} else {
			return false;
		}
	}

	/**
	 * Remove the specified items from the tile and the list.
	 */
	public boolean removeAll(Collection<? extends TileFixture> fixtures) {
		boolean retval = true;
		for (TileFixture fixture : fixtures) {
			if (fixture instanceof TileTypeFixture) {
				TileType currentTerrain = terrainSource.apply(point);
				if (currentTerrain != null &&
						currentTerrain.equals(((TileTypeFixture) fixture)
							.getTileType())) {
					if (terrainSink != null) {
						terrainSink.accept(point, null);
						fireIntervalRemoved(new Range(0, 0));
					} else {
						retval = false;
					}
				}
			} else if (fixture instanceof RiverFixture) {
				if (removeRivers != null) {
					int index = cachedTerrainList.indexOf(fixture);
					removeRivers.accept(point,
						((RiverFixture) fixture).getRivers().stream()
							.toArray(River[]::new));
					cachedTerrainList.remove(fixture);
					fireIntervalRemoved(new Range(index, index));
				} else {
					retval = false;
				}
			} else if (fixture instanceof MountainFixture) {
				if (mountainSink != null) {
					int index = cachedTerrainList.indexOf(fixture);
					mountainSink.accept(point, false);
					cachedTerrainList.remove(fixture);
					fireIntervalRemoved(new Range(index, index));
				} else {
					retval = false;
				}
			} else if (fixturesSource.apply(point).contains(fixture)) {
				int index = indexOf(fixturesSource.apply(point), fixture);
				if (removeFixture != null) {
					removeFixture.accept(point, fixture);
					int adjusted = adjustedIndex(index);
					fireIntervalRemoved(new Range(adjusted, adjusted));
				} else {
					retval = false;
				}
			} else if (fixture instanceof Animal && currentTracks.contains(fixture)) {
				int ctIndex = currentTracks.indexOf(fixture);
				int index = adjustedIndex(fixturesSource.apply(point).size() + ctIndex);
				currentTracks.remove(fixture);
				fireIntervalRemoved(new Range(index, index));
			}
		}
		return retval;
	}

	@Override
	public void interactionPointChanged() {}
}