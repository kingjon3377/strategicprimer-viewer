import lovelace.util.common {
    todo
}

import strategicprimer.model.map {
    HasMutableImage,
    IFixture,
    HasKind
}
"Kinds of simple immortals."
shared class SimpleImmortalKind of sphinx|djinn|griffin|minotaur|ogre|phoenix|
        simurgh|troll {
    "Get the value representing the given XML tag."
    shared static SimpleImmortalKind|ParseException parse(String tag) =>
            parseSimpleImmortalKind(tag);
    "The word to use for both default image and XML tag."
    shared String tag;
    "The plural of this kind."
    shared String plural;
    "The DC to use for this discovering kind."
    shared Integer dc;
    abstract new delegate(String tagParam, String pluralString, Integer dcNum) {
        tag = tagParam;
        plural = pluralString;
        dc = dcNum;
    }
    shared new sphinx extends delegate("sphinx", "Sphinxes", 35) {}
    shared new djinn extends delegate("djinn", "Djinni", 30) {}
    shared new griffin extends delegate("griffin", "Griffins", 28) {}
    shared new minotaur extends delegate("minotaur", "Minotaurs", 30) {}
    shared new ogre extends delegate("ogre", "Ogres", 28) {}
    shared new phoenix extends delegate("phoenix", "Phoenixes", 35) {}
    shared new simurgh extends delegate("simurgh", "Simurghs", 35) {}
    shared new troll extends delegate("troll", "Trolls", 28) {}
}
"A class for immortals that don't have any state other than their ID, so we only need one
 class for all of them."
todo("Split back out, since Ceylon means less boilerplate?")
shared class SimpleImmortal satisfies Immortal&HasMutableImage&HasKind {
    "What kind of immortal this is, as an enumerated object."
    shared SimpleImmortalKind immortalKind;
    "An ID number for the fixture."
    shared actual Integer id;
    shared new (SimpleImmortalKind kind, Integer idNum) {
        immortalKind = kind;
        id = idNum;
    }
    "What kind of immortal this is, as a string."
    shared actual String kind => immortalKind.tag;
    "The required Perception check result to find the immortal."
    shared actual Integer dc => immortalKind.dc;
    "A short description of the fixture."
    shared actual String shortDescription =>
            (immortalKind == SimpleImmortalKind.ogre) then "an ogre" else
            "a ``immortalKind.tag``";
    "The plural of the immortal's kind."
    shared actual String plural = immortalKind.plural;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    "Clone the object."
    shared actual SimpleImmortal copy(Boolean zero) {
        SimpleImmortal retval = SimpleImmortal(immortalKind, id);
        retval.image = image;
        return retval;
    }
    shared actual String string => immortalKind.tag;
    "The default icon filename."
    shared actual String defaultImage => "``immortalKind.tag``.png";
    shared actual Boolean equals(Object obj) {
        if (is SimpleImmortal obj) {
            return obj.id == id && immortalKind == obj.immortalKind;
        } else {
            return false;
        }
    }
    shared actual Integer hash => id;
    "If we ignore ID, all simple immortals of a given kind are equal."
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is SimpleImmortal fixture) {
            return fixture.immortalKind == immortalKind;
        } else {
            return false;
        }
    }
    "A fixture is a subset iff it is equal."
    shared actual Boolean isSubset(IFixture obj, Anything(String) report) {
        if (obj.id == id) {
            if (is SimpleImmortal obj, obj.immortalKind == immortalKind) {
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
SimpleImmortalKind|ParseException parseSimpleImmortalKind(String tag) {
    for (kind in `SimpleImmortalKind`.caseValues) {
        if (tag == kind.tag) {
            return kind;
        }
    } else {
        return ParseException("Can't parse a simple immortal kind from '``tag``'");
    }
}
