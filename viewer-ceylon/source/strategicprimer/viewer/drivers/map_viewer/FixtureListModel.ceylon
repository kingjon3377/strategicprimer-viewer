import ceylon.collection {
    ArrayList,
    MutableList
}

import javax.swing {
    DefaultListModel
}

import lovelace.util.common {
    todo
}

import model.listeners {
    SelectionChangeListener
}
import model.map {
    Point
}

import strategicprimer.viewer.model.map {
    River,
    TileFixture,
    TileType,
    IMutableMapNG,
    invalidPoint
}
import strategicprimer.viewer.model.map.fixtures {
    RiverFixture,
    Ground
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    Animal
}
import strategicprimer.viewer.model.map.fixtures.terrain {
    Forest
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
        TileType base = map.getBaseTerrain(newPoint);
        if (TileType.notVisible != base) {
            addElement(TileTypeFixture(base));
        }
        {River*} rivers = map.getRivers(newPoint);
        if (!rivers.empty) {
            if (is TileFixture rivers) {
                addElement(rivers);
            } else {
                addElement(RiverFixture(*rivers));
            }
        }
        Ground? ground = map.getGround(newPoint);
        Forest? forest = map.getForest(newPoint);
        for (fixture in {ground, forest,
            *map.getOtherFixtures(newPoint)}.coalesced) {
            addElement(fixture);
        }
    }
    "Add a tile fixture to the current tile. Note that this modifies the map, not
     just the list."
    shared void addFixture(TileFixture fixture) {
        if (is Ground fixture, !map.getGround(point) exists) {
            map.setGround(point, fixture);
            selectedPointChanged(null, point);
        } else if (is Forest fixture, !map.getForest(point) exists) {
            map.setForest(point, fixture);
            selectedPointChanged(null, point);
        } else if (is TileTypeFixture fixture) {
            if (map.getBaseTerrain(point) != fixture.tileType) {
                map.setBaseTerrain(point, fixture.tileType);
                selectedPointChanged(null, point);
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
                    map.setBaseTerrain(point, TileType.notVisible);
                }
            } else if (is Ground fixture, exists ground = map.getGround(point),
                fixture == ground) {
                if (removeElement(fixture)) {
                    map.setGround(point, null);
                }
            } else if (is Forest fixture, exists forest = map.getForest(point),
                fixture == forest) {
                if (removeElement(fixture)) {
                    map.setForest(point, null);
                }
            } else if (is RiverFixture fixture) {
                if (removeElement(fixture)) {
                    map.removeRivers(point, *fixture);
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
