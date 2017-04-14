import lovelace.util.common {
    todo
}
import ceylon.random {
    DefaultRandom
}
"Possible status of fields (and meadows, and orchards ...) Fields should rotate between
 these, at a rate determined by the kind of field."
todo("Implement that")
shared class FieldStatus of fallow|seeding|growing|bearing
        satisfies Comparable<FieldStatus> {
    shared static FieldStatus|ParseException parse(String status) =>
            parseFieldStatus(status);
    shared static FieldStatus random(Integer seed) => randomStatus(seed);
    shared actual String string;
    shared Integer ordinal;
    "Fallow: waiting to be planted, or waiting to start growing."
    shared new fallow {
        string = "fallow";
        ordinal = 0;
    }
    "Seeding: being planted, by human or animal activity."
    shared new seeding {
        string = "seeding";
        ordinal = 1;
    }
    "Growing."
    shared new growing {
        string = "growing";
        ordinal = 2;
    }
    "Bearing: ready to be harvested."
    shared new bearing {
        string = "bearing";
        ordinal = 3;
    }
    shared actual Comparison compare(FieldStatus other) => ordinal.compare(other.ordinal);
}
FieldStatus|ParseException parseFieldStatus(String status) {
    for (item in `FieldStatus`.caseValues) {
        if (item.string == status) {
            return item;
        }
    }
    return ParseException("Failed to parse FieldStatus from '``status``'");
}
FieldStatus randomStatus(Integer seed) {
    assert (exists retval = DefaultRandom(seed).nextElement(sort<FieldStatus>(`FieldStatus`.caseValues)));
    return retval;
}