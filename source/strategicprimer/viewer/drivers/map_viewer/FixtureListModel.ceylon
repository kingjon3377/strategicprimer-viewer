import strategicprimer.model.common.map {
    Point,
    River,
    TileFixture,
    TileType
}
import javax.swing {
    ListModel
}
import strategicprimer.drivers.common {
    SelectionChangeListener
}
import ceylon.collection {
    ArrayList,
    MutableList
}
import strategicprimer.model.common.map.fixtures.mobile {
    Animal,
    AnimalTracks,
    IUnit
}
import javax.swing.event {
    ListDataListener,
    ListDataEvent
}
import java.lang {
    ArrayIndexOutOfBoundsException
}

"A model for the list-based representation of the contents of a tile."
shared class FixtureListModel(Iterable<TileFixture>(Point) fixturesSource, TileType?(Point) terrainSource,
            Iterable<River>(Point) riversSource, Boolean(Point) mountainSource, AnimalTracks?(Point) tracksSource,
            Anything(Point, TileType?)? terrainSink, Anything(Point, River*)? addRivers,
            Anything(Point, Boolean)? mountainSink, Boolean(Point, TileFixture)? addFixtureLambda,
            Anything(Point, River*)? removeRivers, Anything(Point, TileFixture)? removeFixture,
            Comparison(TileFixture, TileFixture) comparator)
        satisfies ListModel<TileFixture>&SelectionChangeListener {
    "The currently selected point."
    variable Point point = Point.invalidPoint;
    variable TileFixture[] cachedTerrainList = [];

    shared actual void selectedUnitChanged(IUnit? oldSelection, IUnit? newSelection) {}
    shared actual void cursorPointChanged(Point? old, Point newCursor) {}
    """Any animal tracks that have been "added" to the current tile but kept out of the
       map."""
    MutableList<AnimalTracks> currentTracks = ArrayList<AnimalTracks>();

    MutableList<ListDataListener> listDataListeners = ArrayList<ListDataListener>();
    shared actual void addListDataListener(ListDataListener listener) =>
            listDataListeners.add(listener);
    shared actual void removeListDataListener(ListDataListener listener) =>
            listDataListeners.remove(listener);

    shared actual Integer size =>
            fixturesSource(point).size + currentTracks.size + cachedTerrainList.size;

    void fireIntervalReplaced(Range<Integer> oldRange, Range<Integer> newRange) {
        ListDataEvent removeEvent = ListDataEvent(this, ListDataEvent.intervalRemoved,
            oldRange.first, oldRange.lastIndex);
        ListDataEvent addEvent = ListDataEvent(this, ListDataEvent.intervalAdded,
            newRange.first, newRange.lastIndex);
        for (listener in listDataListeners) {
            listener.intervalRemoved(removeEvent);
            listener.intervalAdded(addEvent);
        }
    }

    void fireContentsChanged(Range<Integer> range) {
        ListDataEvent event = ListDataEvent(this, ListDataEvent.contentsChanged,
            range.first, range.lastIndex);
        for (listener in listDataListeners) {
            listener.contentsChanged(event);
        }
    }

    void fireIntervalAdded(Range<Integer> range) {
        ListDataEvent event = ListDataEvent(this, ListDataEvent.intervalAdded,
            range.first, range.lastIndex);
        for (listener in listDataListeners) {
            listener.intervalAdded(event);
        }
    }

    void fireIntervalRemoved(Range<Integer> range) {
        ListDataEvent event = ListDataEvent(this, ListDataEvent.intervalRemoved,
            range.first, range.lastIndex);
        for (listener in listDataListeners) {
            listener.intervalRemoved(event);
        }
    }

    shared actual void selectedPointChanged(Point? old, Point newPoint) {
        log.trace("Starting FixtureListModel.selectedPointChanged");
        Integer oldSize = size;
        cachedTerrainList = [];
        if (exists terrain = terrainSource(newPoint)) {
            cachedTerrainList = [TileTypeFixture(terrain)];
        }
        log.trace("FixtureListModel.selectedPointChanged: Accounted for base terrain");
        if (nonempty rivers = riversSource(newPoint).sequence()) {
            cachedTerrainList = cachedTerrainList.withTrailing(RiverFixture(rivers));
        }
        // TODO: Add support for roads
        log.trace("FixtureListModel.selectedPointChanged: Accounted for rivers");
        if (mountainSource(newPoint)) {
            cachedTerrainList = cachedTerrainList.withTrailing(MountainFixture());
        }
        log.trace("FixtureListModel.selectedPointChanged: Accounted for mountain");
        point = newPoint;
        currentTracks.clear();
        if (exists tracks = tracksSource(newPoint)) {
            currentTracks.add(tracks);
        }
        log.trace("FixtureListModel.selectedPointChanged: Accounted for animal tracks");
        Integer newSize = size;
        log.trace("FixtureListModel.selectedPointChanged: About to notify listeners");
        fireIntervalReplaced(0..(oldSize - 1), 0..(newSize - 1));
        log.trace("End of FixtureListModel.selectedPointChanged");
    }

    shared actual TileFixture getElementAt(Integer index) {
        TileFixture[] main = fixturesSource(point).sort(comparator); // TODO: cache this?
        if (index < 0) {
            throw ArrayIndexOutOfBoundsException(index);
        } else if (exists retval = cachedTerrainList.getFromFirst(index)) {
            return retval;
        } else if (exists retval = main.getFromFirst(index - cachedTerrainList.size)) {
            return retval;
        } else if (exists retval =
                currentTracks.getFromFirst(index - cachedTerrainList.size - main.size)) {
            return retval;
        } else {
            throw ArrayIndexOutOfBoundsException(index);
        }
    }

    Integer adjustedIndex(Integer index) => index + cachedTerrainList.size;

    "Returns true if the operation is accepted (succeeded), false if it is
     rejected (failed). For now no-op operations are treated as successes."
    // TODO: Provide a way of pre-rejecting operations, so drag-and-drop failures can be warned about before the drop
    shared Boolean addFixture(TileFixture fixture) {
        if (is TileTypeFixture fixture) {
            if (exists existingTerrain = terrainSource(point)) {
                if (existingTerrain == fixture.tileType) {
                    return true;
                } else if (exists terrainSink) {
                    terrainSink(point, fixture.tileType);
                    fireContentsChanged(0..0);
                    return true;
                } else {
                    return false;
                }
            } else if (exists terrainSink) {
                terrainSink(point, fixture.tileType);
                fireIntervalAdded(0..0);
                return true;
            } else {
                return false;
            }
        } else if (is RiverFixture fixture) {
            if (nonempty existingRivers = riversSource(point).sequence()) { // TODO: syntax sugar
                if (existingRivers.containsEvery(fixture.rivers)) {
                    return true;
                } else if (exists addRivers) {
                    addRivers(point, *fixture.rivers);
                    assert (exists index =
                        cachedTerrainList.firstIndexWhere(`RiverFixture`.typeOf));
                    fireContentsChanged(index..index);
                    cachedTerrainList =
                        cachedTerrainList.patch([RiverFixture(riversSource(point))],
                            index, 1).sequence();
                    return true;
                } else {
                    return false;
                }
            } else if (exists addRivers) {
                addRivers(point, *fixture.rivers);
                Integer index = cachedTerrainList.size;
                cachedTerrainList = cachedTerrainList.withTrailing(fixture);
                fireIntervalAdded(index..index);
                return true;
            } else {
                return false;
            } // TODO: Handle roads
        } else if (is MountainFixture fixture) {
            if (mountainSource(point)) {
                return true;
            } else if (exists mountainSink) {
                Integer index = cachedTerrainList.size;
                mountainSink(point, true);
                fireIntervalAdded(index..index);
                return true;
            } else {
                return false;
            }
        } else if (exists addFixtureLambda, addFixtureLambda(point, fixture),
                exists index = fixturesSource(point).locate(fixture.equals)?.key) {
            Integer adjusted = adjustedIndex(index); // FIXME: Can this be right?
            fireIntervalAdded(adjusted..adjusted);
            return true;
        } else if (exists addFixtureLambda, exists index = fixturesSource(point).locate(fixture.equals)?.key) {
            Integer adjusted = adjustedIndex(index);
            fireContentsChanged(adjusted..adjusted);
            return true;
        } else {
            return false;
        }
    }

    "Remove the specified items from the tile and the list."
    shared Boolean removeAll(TileFixture* fixtures) {
        variable Boolean retval = true;
        for (fixture in fixtures) {
            if (is TileTypeFixture fixture) {
                if (exists currentTerrain = terrainSource(point),
                        currentTerrain == fixture.tileType) {
                    if (exists terrainSink) {
                        terrainSink(point, null);
                        fireIntervalRemoved(0..0);
                    } else {
                        retval = false;
                    }
                }
            } else if (is RiverFixture fixture) {
                if (exists removeRivers) {
                    assert (exists index = cachedTerrainList.locate(fixture.equals)?.key);
                    removeRivers(point, *fixture.rivers);
                    cachedTerrainList = cachedTerrainList.filter(not(fixture.equals))
                        .sequence();
                    fireIntervalRemoved(index..index);
                } else {
                    retval = false;
                }
            } else if (is MountainFixture fixture) {
                if (exists mountainSink) {
                    assert (exists index = cachedTerrainList.locate(fixture.equals)?.key);
                    mountainSink(point, false);
                    cachedTerrainList = cachedTerrainList.filter(not(fixture.equals))
                        .sequence();
                    fireIntervalRemoved(index..index);
                } else {
                    retval = false;
                }
            } else if (exists index = fixturesSource(point).locate(fixture.equals)?.key) {
                if (exists removeFixture) {
                    removeFixture(point, fixture);
                    Integer adjusted = adjustedIndex(index);
                    fireIntervalRemoved(adjusted..adjusted);
                } else {
                    retval = false;
                }
            } else if (is Animal fixture,
                    exists ctIndex = currentTracks.locate(fixture.equals)?.key) {
                Integer index = adjustedIndex(fixturesSource(point).size + ctIndex);
                // FIXME: Actually remove it from currentTracks
                fireIntervalRemoved(index..index);
            }
        }
        return retval;
    }
    shared actual void interactionPointChanged() {}
}
