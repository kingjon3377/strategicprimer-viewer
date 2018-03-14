import ceylon.collection {
    ArrayList,
    MutableList
}

import javax.swing {
    DefaultListModel,
	SwingUtilities
}

import lovelace.util.common {
    todo,
    anythingEqual
}

import strategicprimer.model.map {
    Point,
    TileFixture,
    IMutableMapNG,
    invalidPoint
}
import strategicprimer.model.map.fixtures.mobile {
    Animal
}
import strategicprimer.drivers.common {
    SelectionChangeListener
}
"A model for the list-based representation of the contents of a tile."
todo("Tests")
shared class FixtureListModel(IMutableMapNG map,
        "Whether to keep animal tracks out of the map."
        Boolean filterTracks) extends DefaultListModel<TileFixture>()
        satisfies SelectionChangeListener {
    "The currently selected point."
    variable Point point = invalidPoint;
    """Any animal tracks that have been "added" to the current tile but kept out of the
       map."""
    MutableList<Animal> currentTracks = ArrayList<Animal>();
    shared actual void selectedPointChanged(Point? old, Point newPoint) {
        clear();
        currentTracks.clear();
        point = newPoint;
        if (exists base = map.baseTerrain[newPoint]) {
            addElement(TileTypeFixture(base));
        }
//        for (fixture in map.fixtures[newPoint]) {
        for (fixture in map.fixtures.get(newPoint)) {
            addElement(fixture);
        }
    }
    "Add a tile fixture to the current tile. Note that this modifies the map, not
     just the list."
    shared void addFixture(TileFixture fixture) {
        if (is TileTypeFixture fixture) {
            if (!anythingEqual(map.baseTerrain[point], fixture.tileType)) {
                map.baseTerrain[point] = fixture.tileType;
                SwingUtilities.invokeLater(() => selectedPointChanged(null, point));
            }
        } else if (filterTracks, is Animal fixture, fixture.traces) {
            currentTracks.add(fixture);
            addElement(fixture);
        } else if (map.addFixture(point, fixture)) {
            addElement(fixture);
        }
    }
    "Remove the specified items from the tile and the list."
    shared void removeAll(TileFixture* fixtures) {
        for (fixture in fixtures) {
            if (is TileTypeFixture fixture) {
                if (removeElement(fixture)) { // no-op if it wasn't *our* terrain
                    map.baseTerrain[point] = null;
                }
            } else if (filterTracks, is Animal fixture, currentTracks.contains(fixture)) {
                if (removeElement(fixture)) {
                    currentTracks.remove(fixture);
                }
            } else if (removeElement(fixture)) {
                map.removeFixture(point, fixture);
            }
        }
    }
    shared actual Boolean equals(Object that) {
        if (is FixtureListModel that, that.map == map, that.point == point,
            that.filterTracks == filterTracks, that.currentTracks == currentTracks) {
            return true;
        } else {
            return false;
        }
    }
    shared actual Integer hash => map.hash.or(point.hash);
}
