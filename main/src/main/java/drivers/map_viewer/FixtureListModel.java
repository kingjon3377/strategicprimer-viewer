package drivers.map_viewer;

import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import lovelace.util.Range;
import legacy.map.Point;
import legacy.map.River;
import legacy.map.TileFixture;
import legacy.map.TileType;

import javax.swing.ListModel;

import drivers.common.SelectionChangeListener;

import java.util.ArrayList;
import java.util.List;

import legacy.map.fixtures.mobile.AnimalTracks;
import legacy.map.fixtures.mobile.IUnit;

import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.BiPredicate;
import java.util.function.BiConsumer;
import java.util.Comparator;
import java.util.Collections;

/**
 * A model for the list-based representation of the contents of a tile.
 */
public class FixtureListModel implements ListModel<TileFixture>, SelectionChangeListener {
	private final Function<Point, Collection<TileFixture>> fixturesSource;
	private final Function<Point, @Nullable TileType> terrainSource;
	private final Function<Point, Collection<River>> riversSource;
	private final Predicate<Point> mountainSource;
	private final Function<Point, @Nullable AnimalTracks> tracksSource;
	private final @Nullable BiConsumer<Point, @Nullable TileType> terrainSink;
	private final @Nullable BiConsumer<Point, River[]> addRivers;
	private final @Nullable BiConsumer<Point, Boolean> mountainSink;
	private final @Nullable BiPredicate<Point, TileFixture> addFixtureLambda;
	private final @Nullable BiConsumer<Point, River[]> removeRivers;
	private final @Nullable BiConsumer<Point, TileFixture> removeFixture;
	private final Comparator<TileFixture> comparator;

	public FixtureListModel(final Function<Point, Collection<TileFixture>> fixturesSource,
							final Function<Point, @Nullable TileType> terrainSource,
							final Function<Point, Collection<River>> riversSource, final Predicate<Point> mountainSource,
							final Function<Point, @Nullable AnimalTracks> tracksSource,
							final @Nullable BiConsumer<Point, TileType> terrainSink,
							final @Nullable BiConsumer<Point, River[]> addRivers,
							final @Nullable BiConsumer<Point, Boolean> mountainSink,
							final @Nullable BiPredicate<Point, TileFixture> addFixtureLambda,
							final @Nullable BiConsumer<Point, River[]> removeRivers,
							final @Nullable BiConsumer<Point, TileFixture> removeFixture,
							final Comparator<TileFixture> comparator) {
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
	public void selectedUnitChanged(final @Nullable IUnit oldSelection, final @Nullable IUnit newSelection) {
	}

	@Override
	public void cursorPointChanged(final @Nullable Point old, final Point newCursor) {
	}

	/**
	 * Any animal tracks that have been "added" to the current tile but kept out of the map.
	 */
	private final List<AnimalTracks> currentTracks = new ArrayList<>();

	private final List<ListDataListener> listDataListeners = new ArrayList<>();

	@Override
	public void addListDataListener(final ListDataListener listener) {
		listDataListeners.add(listener);
	}

	@Override
	public void removeListDataListener(final ListDataListener listener) {
		listDataListeners.remove(listener);
	}

	@Override
	public int getSize() {
		return fixturesSource.apply(point).size() + currentTracks.size() + cachedTerrainList.size();
	}

	private void fireIntervalReplaced(final Range oldRange, final Range newRange) {
		final ListDataEvent removeEvent = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED,
				oldRange.lowerBound(), oldRange.upperBound());
		final ListDataEvent addEvent = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED,
				newRange.lowerBound(), newRange.upperBound());
		for (final ListDataListener listener : listDataListeners) {
			listener.intervalRemoved(removeEvent);
			listener.intervalAdded(addEvent);
		}
	}

	private void fireContentsChanged(final Range range) {
		final ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED,
				range.lowerBound(), range.upperBound());
		for (final ListDataListener listener : listDataListeners) {
			listener.contentsChanged(event);
		}
	}

	private void fireIntervalAdded(final Range range) {
		final ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED,
				range.lowerBound(), range.upperBound());
		for (final ListDataListener listener : listDataListeners) {
			listener.intervalAdded(event);
		}
	}

	private void fireIntervalRemoved(final Range range) {
		final ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED,
				range.lowerBound(), range.upperBound());
		for (final ListDataListener listener : listDataListeners) {
			listener.intervalRemoved(event);
		}
	}

	@Override
	public void selectedPointChanged(final @Nullable Point old, final Point newPoint) {
		LovelaceLogger.trace("Starting FixtureListModel.selectedPointChanged");
		final int oldSize = getSize();
		cachedTerrainList = Collections.emptyList();
		final TileType terrain = terrainSource.apply(newPoint);
		if (!Objects.isNull(terrain)) {
			cachedTerrainList = new ArrayList<>(Collections.singleton(
					new TileTypeFixture(terrain)));
		}
		LovelaceLogger.trace("FixtureListModel.selectedPointChanged: Accounted for base terrain");
		final Collection<River> rivers = riversSource.apply(newPoint);
		if (rivers.iterator().hasNext()) {
			cachedTerrainList.add(new RiverFixture(rivers.toArray(River[]::new)));
		}
		// TODO: Add support for roads
		LovelaceLogger.trace("FixtureListModel.selectedPointChanged: Accounted for rivers");
		if (mountainSource.test(newPoint)) {
			cachedTerrainList.add(new MountainFixture());
		}
		LovelaceLogger.trace("FixtureListModel.selectedPointChanged: Accounted for mountain");
		point = newPoint;
		currentTracks.clear();
		final AnimalTracks tracks = tracksSource.apply(newPoint);
		if (!Objects.isNull(tracks)) {
			currentTracks.add(tracks);
		}
		LovelaceLogger.trace("FixtureListModel.selectedPointChanged: Accounted for animal tracks");
		final int newSize = getSize();
		LovelaceLogger.trace("FixtureListModel.selectedPointChanged: About to notify listeners");
		fireIntervalReplaced(new Range(0, Math.max(0, oldSize - 1)),
				new Range(0, Math.max(0, newSize - 1)));
		LovelaceLogger.trace("End of FixtureListModel.selectedPointChanged");
	}

	@Override
	public TileFixture getElementAt(final int index) {
		final List<TileFixture> main = new ArrayList<>(fixturesSource.apply(point));
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

	private int adjustedIndex(final int index) {
		return index + cachedTerrainList.size();
	}

	private static <U, T extends U> int indexOf(final Collection<U> collection, final T item) {
		if (collection instanceof final List l) {
			return l.indexOf(item);
		} else {
			return new ArrayList<>(collection).indexOf(item);
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
	public boolean addFixture(final TileFixture fixture) {
		if (fixture instanceof final TileTypeFixture ttf) {
			final TileType existingTerrain = terrainSource.apply(point);
			if (!Objects.isNull(existingTerrain)) {
				if (existingTerrain == ttf.tileType()) {
					return true;
				} else if (!Objects.isNull(terrainSink)) {
					terrainSink.accept(point, ttf.tileType());
					fireContentsChanged(new Range(0, 0));
					return true;
				} else {
					return false;
				}
			} else if (!Objects.isNull(terrainSink)) {
				terrainSink.accept(point, ttf.tileType());
				fireIntervalAdded(new Range(0, 0));
				return true;
			} else {
				return false;
			}
		} else if (fixture instanceof final RiverFixture rf) {
			final Collection<River> existingRivers = riversSource.apply(point);
			if (existingRivers.iterator().hasNext()) {
				final Collection<River> coll = new ArrayList<>(existingRivers);
				if (coll.containsAll(rf.getRivers())) {
					return true;
				} else if (!Objects.isNull(addRivers)) {
					addRivers.accept(point,
							rf.getRivers().toArray(River[]::new));
					int index = -1;
					for (int i = 0; i < cachedTerrainList.size(); i++) {
						if (cachedTerrainList.get(i) instanceof RiverFixture) {
							index = i;
							break;
						}
					}
					fireContentsChanged(new Range(index, index)); // TODO: move to after update?
					cachedTerrainList.set(index,
							new RiverFixture(riversSource.apply(point).toArray(River[]::new)));
					return true;
				} else {
					return false;
				}
			} else if (!Objects.isNull(addRivers)) {
				addRivers.accept(point,
						rf.getRivers().toArray(River[]::new));
				final int index = cachedTerrainList.size();
				cachedTerrainList.add(fixture);
				fireIntervalAdded(new Range(index, index));
				return true;
			} else {
				return false;
			} // TODO: Handle roads
		} else if (fixture instanceof MountainFixture) {
			if (mountainSource.test(point)) {
				return true;
			} else if (!Objects.isNull(mountainSink)) {
				final int index = cachedTerrainList.size();
				mountainSink.accept(point, true);
				fireIntervalAdded(new Range(index, index));
				return true;
			} else {
				return false;
			}
		} else if (!Objects.isNull(addFixtureLambda) && addFixtureLambda.test(point, fixture)) {
			final int index = indexOf(fixturesSource.apply(point), fixture);
			if (index >= 0) {
				final int adjusted = adjustedIndex(index); // FIXME: Can this be right?
				fireIntervalAdded(new Range(adjusted, adjusted));
				return true;
			} else {
				return false; // TODO: This returns failure if a more-up-to-date version is already there
			}
		} else if (!Objects.isNull(addFixtureLambda)) {
			final int index = indexOf(fixturesSource.apply(point), fixture);
			if (index >= 0) {
				final int adjusted = adjustedIndex(index);
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
	public boolean removeAll(final Collection<? extends TileFixture> fixtures) {
		boolean retval = true;
		final Range zeroRange = new Range(0, 0);
		for (final TileFixture fixture : fixtures) {
			if (fixture instanceof TileTypeFixture) {
				final TileType currentTerrain = terrainSource.apply(point);
				if (!Objects.isNull(currentTerrain) &&
						currentTerrain == ((TileTypeFixture) fixture)
								.tileType()) {
					if (Objects.isNull(terrainSink)) {
						retval = false;
					} else {
						terrainSink.accept(point, null);
						fireIntervalRemoved(zeroRange);
					}
				}
			} else if (fixture instanceof RiverFixture) {
				if (Objects.isNull(removeRivers)) {
					retval = false;
				} else {
					final int index = cachedTerrainList.indexOf(fixture);
					removeRivers.accept(point,
							((RiverFixture) fixture).getRivers().toArray(River[]::new));
					cachedTerrainList.remove(fixture);
					fireIntervalRemoved(new Range(index, index));
				}
			} else if (fixture instanceof MountainFixture) {
				if (Objects.isNull(mountainSink)) {
					retval = false;
				} else {
					final int index = cachedTerrainList.indexOf(fixture);
					mountainSink.accept(point, false);
					cachedTerrainList.remove(fixture);
					fireIntervalRemoved(new Range(index, index));
				}
			} else if (fixturesSource.apply(point).contains(fixture)) {
				final int index = indexOf(fixturesSource.apply(point), fixture);
				if (Objects.isNull(removeFixture)) {
					retval = false;
				} else {
					removeFixture.accept(point, fixture);
					final int adjusted = adjustedIndex(index);
					fireIntervalRemoved(new Range(adjusted, adjusted));
				}
			} else if (fixture instanceof AnimalTracks && currentTracks.contains(fixture)) {
				final int ctIndex = currentTracks.indexOf(fixture);
				final int index = adjustedIndex(fixturesSource.apply(point).size() + ctIndex);
				currentTracks.remove(fixture);
				fireIntervalRemoved(new Range(index, index));
			}
		}
		return retval;
	}

	@Override
	public void interactionPointChanged() {
	}
}
