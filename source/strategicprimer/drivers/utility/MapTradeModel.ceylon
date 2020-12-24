import strategicprimer.drivers.common {
    IDriverModel,
    SimpleMultiMapModel
}

import lovelace.util.common {
    matchingValue
}

import strategicprimer.model.common.map {
    IMutableMapNG,
    Point,
    TileFixture
}

shared class MapTradeModel extends SimpleMultiMapModel {
    shared new (IMutableMapNG map) extends SimpleMultiMapModel(map) { }
    shared new copyConstructor(IDriverModel model)
            extends SimpleMultiMapModel.copyConstructor(model) {}

    variable Boolean globalModifiedFlag = false;

    void setGlobalModifiedFlag() {
        if (!globalModifiedFlag) {
            for (second in restrictedSubordinateMaps) {
                second.modified = true;
            }
            globalModifiedFlag = true;
        }
    }

    shared void copyPlayers() {
        for (second in restrictedSubordinateMaps) {
            map.players.each(second.addPlayer);
        }
        setGlobalModifiedFlag();
    }

    shared void copyBaseTerrainAt(Point location) {
        for (second in restrictedSubordinateMaps) {
            if (!second.baseTerrain[location] exists, exists terrain =
                    map.baseTerrain[location]) {
                second.baseTerrain[location] = terrain;
                setGlobalModifiedFlag();
            }
        }
    }

    shared void maybeCopyFixturesAt(Point location, Boolean(TileFixture) condition, Boolean zeroFixtures) {
        for (second in restrictedSubordinateMaps) {
            //for (fixture in map.fixtures[location].filter(condition)) { // TODO: syntax sugar
            for (fixture in map.fixtures.get(location).filter(condition)) {
                //if (!second.fixtures[location] // TODO: syntax sugar
                if (fixture.id >= 0, !second.fixtures.get(location)
                        .any(matchingValue(fixture.id, TileFixture.id))) {
                    second.addFixture(location, fixture.copy(zeroFixtures));
                    setGlobalModifiedFlag();
                }
            }
        }
    }

    shared void copyRiversAt(Point location) {
        for (second in restrictedSubordinateMaps) {
                //second.addRivers(location, *map.rivers[location]); // TODO: syntax sugar
                second.addRivers(location, *map.rivers.get(location));
                setGlobalModifiedFlag();
        }
    }

    shared void copyRoadsAt(Point location) {
        if (exists roads = map.roads[location]) {
            for (second in restrictedSubordinateMaps) {
                value existingRoads = second.roads[location];
                for (direction->quality in roads) {
                    value existingRoad = existingRoads?.get(direction);
                    if (exists existingRoad, existingRoad >= quality) {
                        continue;
                    } else {
                        second.setRoadLevel(location, direction, quality);
                        setGlobalModifiedFlag();
                    }
                }
            }
        }
    }
}
