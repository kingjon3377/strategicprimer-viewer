import strategicprimer.model.common.map {
    River,
    FakeFixture
}

"""A fake "TileFixture" to represent the rivers on a tile, so they can appear in the list
   of the tile's contents."""
class RiverFixture satisfies FakeFixture {
    shared actual String defaultImage = "river.png";

    shared {River*} rivers;
    shared new ({River*} rivers) {
        this.rivers = rivers;
    }

    "Clone the object."
    deprecated("This class should only ever be used in a FixtureListModel, and copying
                a tile's rivers should be handled specially anyway, so this method
                should never be called.")
    shared actual RiverFixture copy(Boolean zero) {
        log.warn("TileTypeFixture.copy called", Exception("dummy"));
        return RiverFixture(rivers);
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
