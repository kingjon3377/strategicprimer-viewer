import strategicprimer.model.common.map {
    IMapNG,
    Point,
    TileType
}

import strategicprimer.model.common.idreg {
    IDRegistrar
}

import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}

import strategicprimer.model.common.map.fixtures.mobile {
    AnimalImpl
}

"A sample map-populator."
object sampleMapPopulator satisfies MapPopulator {
    "Hares won't appear in mountains, forests, or ocean."
    shared actual Boolean isSuitable(IMapNG map, Point location) {
        if (exists terrain = map.baseTerrain[location]) {
            return !map.mountainous.get(location)&& TileType.ocean != terrain &&
                !map.fixtures[location]?.narrow<Forest>()?.first exists;
        } else {
            return false;
        }
    }

    shared actual Float chance = 0.05;

    shared actual void create(Point location, IPopulatorDriverModel model, IDRegistrar idf) =>
            model.addFixture(location,
                AnimalImpl("hare", false, "wild", idf.createID()));
}

