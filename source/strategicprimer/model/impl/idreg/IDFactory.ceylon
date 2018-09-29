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
"A class to register IDs with and produce not-yet-used IDs. Performance is likely to be
 poor, but we don't want to go to random IDs because we want them to be as low as
 possible."
shared class IDFactory() satisfies IDRegistrar {
    "The set of IDs used already."
    todo("If the Ceylon SDK ever gets an equivalent, use it instead.")
    BitSet usedIDs = BitSet();
    "Whether the given ID is unused."
    shared actual Boolean isIDUnused(Integer id) => id >= 0 && !usedIDs.get(id);
    "Register, and return, the given ID, using the given Warning instance to report if it
     has already been registered."
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
    shared IDRegistrar copy() {
        IDFactory retval = IDFactory();
        retval.usedIDs.or(usedIDs);
        return retval;
    }
}
