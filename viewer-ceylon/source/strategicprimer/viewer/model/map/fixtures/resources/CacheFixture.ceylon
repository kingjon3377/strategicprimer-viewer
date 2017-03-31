import lovelace.util.common {
    todo
}
import strategicprimer.viewer.model.map {
    IFixture
}
import model.map {
    HasKind
}
"A cache (of vegetables, or a hidden treasure, or ...) on a tile."
shared class CacheFixture(kind, contents, id) satisfies HarvestableFixture&HasKind {
    "What kind of things this is a cache of."
    todo("Should perhaps be enumerated, so we can make images more granular.")
    shared actual String kind;
    "The contents of this cache."
    todo("TODO: Should be turned into objects (serialized as children) as part of the
          general Resource framework.")
    shared String contents;
    "ID number"
    shared actual Integer id;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    "Clone the object."
    shared actual CacheFixture copy(Boolean zero) {
        CacheFixture retval = CacheFixture(kind, contents, id);
        retval.image = image;
        return retval;
    }
    "The filename of the image to use as the default icon for caches."
    todo("Should be more granular")
    shared actual String defaultImage = "cache.png";
    shared actual String string => "a cache of ``kind`` containing ``contents``";
    shared actual Boolean equals(Object obj) {
        if (is CacheFixture obj) {
            return obj.id == id && kind == obj.kind && contents == obj.contents;
        } else {
            return false;
        }
    }
    shared actual Integer hash => id;
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is CacheFixture fixture) {
            return fixture.kind == kind && fixture.contents == contents;
        } else {
            return false;
        }
    }
    shared actual String plural = "Caches";
    shared actual String shortDescription => "a cache of ``kind``";
    todo("Make variable (loaded from XML) or otherwise more granular?")
    shared actual Integer dc = 25;
}