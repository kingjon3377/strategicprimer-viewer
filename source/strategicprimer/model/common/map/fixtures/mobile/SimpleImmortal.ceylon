import strategicprimer.model.common.map {
	IFixture,
    HasMutableImage,
    HasKind
}
"A class for immortals that don't have any state other than their ID, so we only need one
 class for all of them."
shared abstract sealed class SimpleImmortal(kind, plural, dc, id) of Sphinx|Djinn|Griffin|
        Minotaur|Ogre|Phoenix|Simurgh|Troll satisfies Immortal&HasMutableImage&HasKind {
    "An ID number for the fixture."
    shared actual Integer id;
    "What kind of immortal this is, as a string."
    shared actual String kind;
    "The required Perception check result to find the immortal."
    shared actual Integer dc;
    "A short description of the fixture."
    shared actual default String shortDescription => "a ``kind``";
    "The plural of the immortal's kind."
    shared actual String plural;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    "Clone the object."
    shared actual formal SimpleImmortal copy(Boolean zero);
    shared actual String string => kind;
    "The default icon filename."
    shared actual String defaultImage => "``kind``.png";
    shared actual Boolean equals(Object obj) {
        if (is SimpleImmortal obj) {
            return obj.id == id && kind == obj.kind;
        } else {
            return false;
        }
    }
    shared actual Integer hash => id;
    "If we ignore ID, all simple immortals of a given kind are equal."
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is SimpleImmortal fixture) {
            return fixture.kind == kind;
        } else {
            return false;
        }
    }
    "A fixture is a subset iff it is equal."
    shared actual Boolean isSubset(IFixture obj, Anything(String) report) {
        if (obj.id == id) {
            if (is SimpleImmortal obj, obj.kind == kind) {
                return true;
            } else {
                report("For ID #``id``, different kinds of members");
                return false;
            }
        } else {
            report("Called with different IDs, #``id`` and ``obj.id``");
            return false;
        }
    }
}
shared class Sphinx(Integer id) extends SimpleImmortal("sphinx", "Sphinxes", 35, id) {
    shared actual Sphinx copy(Boolean zero) => Sphinx(id);
}
shared class Djinn(Integer id) extends SimpleImmortal("djinn", "Djinni", 30, id) {
    shared actual Djinn copy(Boolean zero) => Djinn(id);
}
shared class Griffin(Integer id) extends SimpleImmortal("griffin", "Griffins", 28, id) {
    shared actual Griffin copy(Boolean zero) => Griffin(id);
}
shared class Minotaur(Integer id)
        extends SimpleImmortal("minotaur", "Minotaurs", 30, id) {
    shared actual Minotaur copy(Boolean zero) => Minotaur(id);
}
shared class Ogre(Integer id) extends SimpleImmortal("ogre", "Ogres", 28, id) {
    shared actual Ogre copy(Boolean zero) => Ogre(id);
    shared actual String shortDescription => "an ogre";
}
shared class Phoenix(Integer id) extends SimpleImmortal("phoenix", "Phoenixes", 35, id) {
    shared actual Phoenix copy(Boolean zero) => Phoenix(id);
}
shared class Simurgh(Integer id) extends SimpleImmortal("simurgh", "Simurghs", 35, id) {
    shared actual Simurgh copy(Boolean zero) => Simurgh(id);
}
shared class Troll(Integer id) extends SimpleImmortal("troll", "Trolls", 28, id) {
    shared actual Troll copy(Boolean zero) => Troll(id);
}