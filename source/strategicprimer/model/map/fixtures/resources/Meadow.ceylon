import lovelace.util.common {
    todo
}
import strategicprimer.model.map {
    IFixture
}
import strategicprimer.model.map.fixtures {
	SPNumber,
	numberComparator
}
"A field or meadow. If in forest, should increase a unit's vision slightly when the unit
 is on it."
todo("Implement that effect")
shared class Meadow(kind, field, cultivated, id, status, acres = -1)
        satisfies HarvestableFixture {
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
    "The size of the field or meadow, in acres. (Or a negative number if unknown.)"
    shared SPNumber acres; // FIXME: Make Subsettable so we can compare this properly
    shared actual Meadow copy(Boolean zero) {
        Meadow retval = Meadow(kind, field, cultivated, id, status, (zero) then -1 else acres);
        retval.image = image;
        return retval;
    }
    "The name of an image to use as an icon by default."
    todo("Make more granular based on [[kind]]")
    shared actual String defaultImage = (field) then "field.png" else "meadow.png";
    shared actual String shortDescription {
        String acreage;
        if (numberComparator.compare(acres, 0) == smaller) {
            acreage = "";
        } else {
            acreage = "``acres``-acre ";
        }
        if (field) {
            return (cultivated) then "``acreage````kind`` field"
                else "Wild or abandoned ``acreage````kind`` field";
        } else {
            return "``acreage````kind`` meadow";
        }
    }
    shared actual String string = shortDescription;
    shared actual Boolean equals(Object obj) {
        if (is Meadow obj) {
            return kind == obj.kind && field == obj.field && status == obj.status &&
                cultivated == obj.cultivated && id == obj.id && acres == obj.acres;
        } else {
            return false;
        }
    }
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is Meadow fixture) {
            return kind == fixture.kind && field == fixture.field &&
                status == fixture.status && cultivated == fixture.cultivated &&
                acres == fixture.acres;
        } else {
            return false;
        }
    }
    shared actual String plural = "Fields and meadows";
    "The required Perception check to find the fixture."
    shared actual Integer dc = 18; // TODO: reflect size
}
