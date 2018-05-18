import lovelace.util.common {
    todo
}
import strategicprimer.model.map {
    IFixture,
	HasExtent
}
import strategicprimer.model.map.fixtures {
	numberComparator
}
"A field or meadow. If in forest, should increase a unit's vision slightly when the unit
 is on it."
todo("Implement that effect")
shared class Meadow(kind, field, cultivated, id, status, acres = -1)
        satisfies HarvestableFixture&HasExtent {
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
    shared actual Number<out Anything> acres;
    shared actual Meadow copy(Boolean zero) {
        Meadow retval = Meadow(kind, field, cultivated, id, status,
            (zero) then -1 else acres);
        retval.image = image;
        return retval;
    }
    "The name of an image to use as an icon by default."
    todo("Make more granular based on [[kind]]")
    shared actual String defaultImage = (field) then "field.png" else "meadow.png";
    shared actual String shortDescription {
        String acreage;
        if (!acres.positive) {
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
    shared actual Boolean isSubset(IFixture other, Anything(String) report) {
        if (other.id != id) {
            report("IDs differ");
            return false;
        } else if (is Meadow other) {
            if (other.field != field) {
                report("One field, one meadow for ID #``id``");
                return false;
            } else if (kind != other.kind) {
                report("In ``(field) then "field" else "meadow"`` with ID #``id``:\tKinds differ");
                return false;
            }
            Anything(String) localReport;
            if (field) { // TODO: These can be made method-reference logic
                localReport = (String str) => report("In ``kind`` field (ID #``id``):\t``str``");
            } else {
                localReport = (String str) => report("In ``kind`` meadow (ID #``id``):\t``str``");
            }
            variable Boolean retval = true;
            if (status != other.status) {
                localReport("Field status differs");
                retval = false;
            }
            if (cultivated != other.cultivated) {
                localReport("Cultivation status differs");
                retval = false;
            }
            if (numberComparator.compare(acres, other.acres) == smaller) {
                localReport("Has larger extent");
                retval = false;
            }
            return retval;
        } else {
            report("Different kinds of fixtures for ID #``id``");
            return false;
        }
    }
    shared actual String plural = "Fields and meadows";
    "The required Perception check to find the fixture."
    shared actual Integer dc = 18; // TODO: reflect size
    shared actual Integer hash => id;
}
