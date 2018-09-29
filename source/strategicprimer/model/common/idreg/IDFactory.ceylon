import ceylon.interop.java {
    synchronize
}

import java.util {
    BitSet
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.common.xmlio {
    Warning
}
import strategicprimer.model.common.idreg {
    DuplicateIDException
}
import ceylon.collection {
    MutableSet,
    HashSet
}
"A class to register IDs with and produce not-yet-used IDs. Performance is likely to be
 poor, but we don't want to go to random IDs because we want them to be as low as
 possible."
shared native class IDFactory() satisfies IDRegistrar {
    MutableSet<Integer> commonUsedIDs = HashSet<Integer>();
    "Whether the given ID is unused."
    shared actual native Boolean isIDUnused(Integer id) =>
            id >= 0 && !(id in commonUsedIDs);
    "Register, and return, the given ID, using the given Warning instance to report if it
     has already been registered."
    shared actual native Integer register(Integer id, Warning warning,
            "The location ([row, column]) in some XML that this is coming from. Null if
             caller isn't an XML reader."
            [Integer, Integer]? location) {
        if (id >= 0) {
            if (id in commonUsedIDs) {
                if (exists location) {
                    warning.handle(DuplicateIDException.atLocation(id,
                        location.first, location.rest.first));
                } else {
                    warning.handle(DuplicateIDException(id));
                }
            }
            commonUsedIDs.add(id);
        }
        return id;
    }
    "Generate and register an ID that hasn't been previously registered. Note that this
     method is only thread-safe, and only performant, on the JVM."
    shared actual native Integer createID() {
        for (i in 0:runtime.maxArraySize) {
            if (!(i in commonUsedIDs)) {
                return register(i);
            }
        }
        throw AssertionError("Ran out of possible IDs");
    }
    """Create a copy of this factory for testing purposes. (So that we don't "register"
       IDs that don't end up getting used.)"""
    todo("Tests should cover this method.")
    shared native IDRegistrar copy() {
        IDFactory retval = IDFactory();
        retval.commonUsedIDs.addAll(commonUsedIDs);
        return retval;
    }
}

native("jvm")
shared class IDFactory() satisfies IDRegistrar {
    "The set of IDs used already."
    todo("If the Ceylon SDK ever gets an equivalent, use it instead.")
    BitSet usedIDs = BitSet();
    "Whether the given ID is unused."
    native("jvm")
    shared actual Boolean isIDUnused(Integer id) => id >= 0 && !usedIDs.get(id);
    "Register, and return, the given ID, using the given Warning instance to report if it
     has already been registered."
    native("jvm")
    shared actual Integer register(Integer id, Warning warning,
            "The location ([row, column]) in some XML that this is coming from. Null if
             caller isn't an XML reader."
            [Integer, Integer]? location) {
        if (id >= 0) {
            if (usedIDs.get(id)) {
                if (exists location) {
                    warning.handle(DuplicateIDException.atLocation(id,
                        location.first, location.rest.first));
                } else {
                    warning.handle(DuplicateIDException(id));
                }
            }
            usedIDs.set(id);
        }
        return id;
    }
    "Generate and register an ID that hasn't been previously registered."
    native("jvm")
    shared actual Integer createID() {
        variable Integer retval = -1;
        synchronize(usedIDs, () {
            assert (usedIDs.cardinality() < runtime.maxArraySize);
            retval = register(usedIDs.nextClearBit(0));
        });
        assert (retval >= 0);
        return retval;
    }
    """Create a copy of this factory for testing purposes. (So that we don't "register"
       IDs that don't end up getting used.)"""
    todo("Tests should cover this method.")
    native("jvm")
    shared IDRegistrar copy() {
        IDFactory retval = IDFactory();
        retval.usedIDs.or(usedIDs);
        return retval;
    }
}
