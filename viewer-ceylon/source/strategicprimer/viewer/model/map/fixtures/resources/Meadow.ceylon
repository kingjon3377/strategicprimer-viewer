import lovelace.util.common {
    todo
}

import model.map {
    HasKind,
    IFixture
}
"A field or meadow. If in forest, should increase a unit's vision slightly when the unit
 is on it."
todo("Implement that effect")
shared class Meadow(kind, field, cultivated, id, status)
        satisfies HarvestableFixture&HasKind {
    "The kind of grain or grass growing in this field or meadow."
    shared actual String kind;
    "If true, this is a field; if false, a meadow."
    todo("Use constructors instead of exposing this as a field?")
    shared Boolean field;
    "Whether this field or meadow is under cultivation."
    shared Boolean cultivated;
    "An ID number to identify the field or meadow."
    shared actual Integer id;
    "Which season the field is in."
    todo("Make mutable?")
    shared FieldStatus status;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    shared actual Meadow copy(Boolean zero) {
        Meadow retval = Meadow(kind, field, cultivated, id, status);
        retval.image = image;
        return retval;
    }
    "The name of an image to use as an icon by default."
    todo("Make more granular based on [[kind]]")
    shared actual String defaultImage = (field) then "field.png" else "meadow.png";
    shared actual String shortDesc() {
        if (field) {
            return (cultivated) then "Wild or abandoned ``kind`` field"
                else "``kind`` field";
        } else {
            return "``kind`` meadow";
        }
    }
    shared actual String string = shortDesc();
    shared actual Boolean equals(Object obj) {
        if (is Meadow obj) {
            return kind == obj.kind && field == obj.field && status == obj.status &&
                cultivated == obj.cultivated && id == obj.id;
        } else {
            return false;
        }
    }
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is Meadow fixture) {
            return kind == fixture.kind && field == fixture.field &&
                status == fixture.status && cultivated == fixture.cultivated;
        } else {
            return false;
        }
    }
    shared actual String plural() => "Fields and meadows";
    "The required Perception check to find the fixture."
    shared actual Integer dc = 18;
}