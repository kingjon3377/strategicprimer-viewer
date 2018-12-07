import strategicprimer.model.common.map {
    TileFixture,
    Point,
    IMutableMapNG
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
    AnimalTracks
}
import javax.swing.event {
    ListDataListener,
    ListDataEvent
}
import java.lang {
    ArrayIndexOutOfBoundsException
}

"A model for the list-based representation of the contents of a tile."
shared class FixtureListModel(IMutableMapNG map, AnimalTracks?(Point) tracksSource)
        satisfies ListModel<TileFixture>&SelectionChangeListener {
    "The currently selected point."
    variable Point point = Point.invalidPoint;
    variable TileFixture[] cachedTerrainList = [];

    """Any animal tracks that have been "added" to the current tile but kept out of the
       map."""
    MutableList<AnimalTracks> currentTracks = ArrayList<AnimalTracks>();

    MutableList<ListDataListener> listDataListeners = ArrayList<ListDataListener>();
    shared actual void addListDataListener(ListDataListener listener) =>
            listDataListeners.add(listener);
    shared actual void removeListDataListener(ListDataListener listener) =>
            listDataListeners.remove(listener);

    shared actual Integer size { // TODO: => once syntax sugar in place
        //return map.fixtures[point].size + currentTracks.size + cachedTerrainList.size; // TODO: syntax sugar
        return map.fixtures.get(point).size + currentTracks.size + cachedTerrainList.size;
    }

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
        Integer oldSize = size;
        cachedTerrainList = [];
        if (exists terrain = map.baseTerrain[newPoint]) {
            cachedTerrainList = [TileTypeFixture(terrain)];
        }
        if (nonempty rivers = map.rivers.get(newPoint).sequence()) { // TODO: syntax sugar
            cachedTerrainList = cachedTerrainList.withTrailing(RiverFixture(*rivers));
        }
//        if (map.mountainous[newPoint]) { // TODO: syntax sugar
        if (map.mountainous.get(newPoint)) {
            cachedTerrainList = cachedTerrainList.withTrailing(MountainFixture());
        }
        point = newPoint;
        currentTracks.clear();
        if (exists tracks = tracksSource(newPoint)) {
            currentTracks.add(tracks);
        }
        Integer newSize = size;
        fireIntervalReplaced(0..(oldSize - 1), 0..(newSize - 1));
    }

    shared actual TileFixture getElementAt(Integer index) {
        //TileFixture[] main = map.fixtures[point].sequence(); // TODO: syntax sugar
        TileFixture[] main = map.fixtures.get(point).sequence();
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

    Integer adjustedIndex(Integer index) {
        return index + cachedTerrainList.size;
    }

    shared void addFixture(TileFixture fixture) {
        if (is TileTypeFixture fixture) {
            if (exists existingTerrain = map.baseTerrain[point]) {
                if (existingTerrain == fixture.tileType) {
                    return;
                } else {
                    map.baseTerrain[point] = fixture.tileType;
                    fireContentsChanged(0..0);
                }
            } else {
                map.baseTerrain[point] = fixture.tileType;
                fireIntervalAdded(0..0);
            }
        } else if (is RiverFixture fixture) {
            if (nonempty existingRivers = map.rivers.get(point).sequence()) { // TODO: syntax sugar
                if (existingRivers.containsEvery(fixture.rivers)) {
                    return;
                } else {
                    map.addRivers(point, *fixture.rivers);
                    assert (exists index =
                        cachedTerrainList.firstIndexWhere(`RiverFixture`.typeOf));
                    fireContentsChanged(index..index);
                    cachedTerrainList =
                        cachedTerrainList.patch([RiverFixture(*map.rivers.get(point))],
                            index, 1).sequence();
                }
            } else {
                map.addRivers(point, *fixture.rivers);
                Integer index = cachedTerrainList.size;
                cachedTerrainList = cachedTerrainList.withTrailing(fixture);
                fireIntervalAdded(index..index);
            }
        } else if (is MountainFixture fixture) {
//        if (map.mountainous[point]) { // TODO: syntax sugar
            if (map.mountainous.get(point)) {
                return;
            } else {
                Integer index = cachedTerrainList.size;
                map.mountainous[point] = true;
                fireIntervalAdded(index..index);
            }
        } else if (map.addFixture(point, fixture),
                exists index = map.fixtures[point]?.locate(fixture.equals)?.key) {
            Integer adjusted = adjustedIndex(index);
            fireIntervalAdded(adjusted..adjusted);
        } else if (exists index = map.fixtures[point]?.locate(fixture.equals)?.key) {
            Integer adjusted = adjustedIndex(index);
            fireContentsChanged(adjusted..adjusted);
        }
    }

    "Remove the specified items from the tile and the list."
    shared void removeAll(TileFixture* fixtures) {
        for (fixture in fixtures) {
            if (is TileTypeFixture fixture) {
                if (exists currentTerrain = map.baseTerrain[point],
                    currentTerrain == fixture.tileType) { // TODO: fix indentation
                    map.baseTerrain[point] = null;
                    fireIntervalRemoved(0..0);
                }
            } else if (is RiverFixture fixture) {
                assert (exists index = cachedTerrainList.locate(fixture.equals)?.key); // TODO: Put in loop condition
                map.removeRivers(point, *fixture.rivers);
                cachedTerrainList = cachedTerrainList.filter(not(fixture.equals))
                    .sequence();
                fireIntervalRemoved(index..index);
            } else if (is MountainFixture fixture) {
                assert (exists index = cachedTerrainList.locate(fixture.equals)?.key);
                map.mountainous[point] = false;
                cachedTerrainList = cachedTerrainList.filter(not(fixture.equals))
                    .sequence();
                fireIntervalRemoved(index..index);
                return;
            } else if (exists index = map.fixtures[point]?.locate(fixture.equals)?.key) {
                map.removeFixture(point, fixture);
                Integer adjusted = adjustedIndex(index);
                fireIntervalRemoved(adjusted..adjusted);
            } else if (is Animal fixture,
                    exists ctIndex = currentTracks.locate(fixture.equals)?.key) {
                //Integer index = adjustedIndex(map.fixtures[point].size + ctIndex); // TODO: syntax sugar
                Integer index = adjustedIndex(map.fixtures.get(point).size + ctIndex);
                fireIntervalRemoved(index..index);
            }
        }
    }
}
