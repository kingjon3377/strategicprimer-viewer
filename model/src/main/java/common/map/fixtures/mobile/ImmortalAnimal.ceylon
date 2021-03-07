import strategicprimer.model.common.map {
    HasKind,
    HasMutableImage,
    IFixture
}
"An immortal that used to be represented as an [[animal|Animal]] in the past. This class
 is provided, instead of making them additional subclasses of [[SimpleImmortal]], to
 ease the handling of old map files by XML-reading code."
shared abstract class ImmortalAnimal
        of Snowbird|Thunderbird|Pegasus|Unicorn|Kraken
        satisfies Immortal&HasMutableImage&HasKind {
    "Get an immortal constructor for the given kind."
    shared static ImmortalAnimal(Integer) parse(String kind) {
        switch (kind)
        case ("snowbird") { return Snowbird; }
        case ("thunderbird") { return Thunderbird; }
        case ("pegasus") { return Pegasus; }
        case ("unicorn") { return Unicorn; }
        case ("kraken") { return Kraken; }
        else {
            throw ParseException("Unknown immortal-animal kind " + kind);
        }
    }
    "An ID number for the fixture."
    shared actual Integer id;

    "What kind of immortal this is, as a string."
    shared actual String kind;

    "The required Perception check result to find the immortal."
    shared actual Integer dc;

    "The plural of the immortal's kind."
    shared actual String plural;

    shared new (String kind, String plural, Integer dc, Integer id) {
        this.kind = kind;
        this.plural = plural;
        this.dc = dc;
        this.id = id;
    }

    "A short description of the fixture."
    shared actual default String shortDescription => "a ``kind``";

    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";

    "Clone the object."
    shared actual formal ImmortalAnimal copy(Boolean zero);

    shared actual String string => kind;

    "The default icon filename."
    shared actual String defaultImage => "``kind``.png";

    shared actual Boolean equals(Object obj) {
        if (is ImmortalAnimal obj) {
            return obj.id == id && kind == obj.kind;
        } else {
            return false;
        }
    }

    shared actual Integer hash => id;

    "If we ignore ID, all simple immortals of a given kind are equal."
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is ImmortalAnimal fixture) {
            return fixture.kind == kind;
        } else {
            return false;
        }
    }

    "A fixture is a subset iff it is equal."
    shared actual Boolean isSubset(IFixture obj, Anything(String) report) {
        if (obj.id == id) {
            if (is ImmortalAnimal obj, obj.kind == kind) {
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

shared final class Snowbird(Integer id)
        extends ImmortalAnimal("snowbird", "Snowbirds", 29, id) {
    shared actual Snowbird copy(Boolean zero) => Snowbird(id);
}

shared final class Thunderbird(Integer id)
        extends ImmortalAnimal("thunderbird", "Thunderbirds", 29, id) {
    shared actual Thunderbird copy(Boolean zero) => Thunderbird(id);
}

shared final class Pegasus(Integer id)
        extends ImmortalAnimal("pegasus", "Pegasi", 29, id) {
    shared actual Pegasus copy(Boolean zero) => Pegasus(id);
}

shared final class Unicorn(Integer id)
        extends ImmortalAnimal("unicorn", "Unicorns", 29, id) {
    shared actual Unicorn copy(Boolean zero) => Unicorn(id);
}

shared final class Kraken(Integer id)
        extends ImmortalAnimal("kraken", "Krakens", 30, id) {
    shared actual Kraken copy(Boolean zero) => Kraken(id);
}
