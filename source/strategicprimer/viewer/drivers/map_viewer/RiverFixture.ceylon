import strategicprimer.model.common.map {
    River,
    FakeFixture
}
import lovelace.util.common {
    simpleMap,
    simpleSet
}
"""A fake "TileFixture" to represent the rivers on a tile, so they can appear in the list
   of the tile's contents."""
class RiverFixture satisfies FakeFixture {

    "A mapping from river-sets to filenames." // TODO: Find some way to share this between this class and Ver2TileDrawHelper
    static Map<Set<River>, String> riverFiles = simpleMap(
        emptySet->"riv00.png", simpleSet(River.north)->"riv01.png",
        simpleSet(River.east)->"riv02.png", simpleSet(River.south)->"riv03.png",
        simpleSet(River.west)->"riv04.png", simpleSet(River.lake)->"riv05.png",
        simpleSet(River.north, River.east)->"riv06.png",
        simpleSet(River.north,River.south)->"riv07.png",
        simpleSet(River.north,River.west)->"riv08.png",
        simpleSet(River.north,River.lake)->"riv09.png",
        simpleSet(River.east,River.south)->"riv10.png",
        simpleSet(River.east,River.west)->"riv11.png",
        simpleSet(River.east,River.lake)->"riv12.png",
        simpleSet(River.south,River.west)->"riv13.png",
        simpleSet(River.south,River.lake)->"riv14.png",
        simpleSet(River.west,River.lake)->"riv15.png",
        simpleSet(River.north,River.east,River.south)->"riv16.png",
        simpleSet(River.north,River.east,River.west)->"riv17.png",
        simpleSet(River.north,River.east,River.lake)->"riv18.png",
        simpleSet(River.north,River.south,River.west)->"riv19.png",
        simpleSet(River.north,River.south,River.lake)->"riv20.png",
        simpleSet(River.north,River.west,River.lake)->"riv21.png",
        simpleSet(River.east,River.south,River.west)->"riv22.png",
        simpleSet(River.east,River.south,River.lake)->"riv23.png",
        simpleSet(River.east,River.west,River.lake)->"riv24.png",
        simpleSet(River.south,River.west,River.lake)->"riv25.png",
        simpleSet(River.north,River.east,River.south,River.west)->"riv26.png",
        simpleSet(River.north,River.south,River.west,River.lake)->"riv27.png",
        simpleSet(River.north,River.east,River.west,River.lake)->"riv28.png",
        simpleSet(River.north,River.east,River.south,River.lake)->"riv29.png",
        simpleSet(River.east,River.south,River.west,River.lake)->"riv30.png",
        simpleSet(River.north,River.east,River.south,River.west,River.lake)->"riv31.png"
    );

    shared actual String defaultImage;

    shared {River*} rivers;
    shared new (River* rivers) {
        this.rivers = rivers;
        assert (exists img = riverFiles[simpleSet<River>(*rivers)]);
        defaultImage = img;
    }

    "Clone the object."
    deprecated("This class should only ever be used in a FixtureListModel, and copying
                a tile's rivers should be handled specially anyway, so this method
                should never be called.")
    shared actual RiverFixture copy(Boolean zero) {
        log.warn("TileTypeFixture.copy called", Exception("dummy"));
        return RiverFixture(*rivers);
    }

    shared actual Boolean equals(Object obj) {
        if (is RiverFixture obj) {
            return rivers.containsEvery(obj.rivers) && obj.rivers.containsEvery(rivers);
        } else {
            return false;
        }
    }

    shared actual Integer hash => rivers.hash;

    shared actual String string => "Rivers: " + ", ".join(rivers).lowercased;

    shared actual String shortDescription => string;

    "The required Perception check for an explorer to find the fixture."
    shared actual Integer dc = 0;
}
