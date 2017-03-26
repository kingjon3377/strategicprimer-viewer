import ceylon.collection {
    SortedMap
}
import ceylon.interop.java {
    CeylonIterable
}

import java.util {
    Formatter
}

import lovelace.util.common {
    todo
}

import model.map {
    HasImage,
    HasKind,
    HasName,
    FixtureIterable,
    HasOwner,
    IFixture
}
import model.map.fixtures {
    UnitMember,
    FortressMember
}
import model.map.fixtures.mobile {
    MobileFixture
}
"An interface for units."
shared interface IUnit satisfies MobileFixture&HasImage&HasKind&HasName&
        FixtureIterable<UnitMember>&FortressMember&HasOwner {
    "The unit's orders history, a mapping from turns to the orders for those turns."
    shared formal SortedMap<Integer, String> allOrders;
    "Get the unit's orders for the given turn."
    shared formal String getOrders(Integer turn);
    "Set the unit's orders for a turn."
    todo("Move to a 'mutable' interface?")
    shared formal void setOrders(Integer turn, String newOrders);
    "The unit's results for the given turn."
    shared formal String getResults(Integer turn);
    "The unit's results history, a mapping from turns to the results for those turns."
    shared formal SortedMap<Integer, String> allResults;
    "Set the unit's results for a turn."
    todo("Move to a 'mutable' interface?")
    shared formal void setResults(Integer turn, String newResults);
    "The unit's latest orders as of the given turn."
    shared default String getLatestOrders(Integer turn) {
        SortedMap<Integer, String> orders = allOrders;
        for (i in turn..-1) {
            if (exists temp = orders.get(i)) {
                String turnOrders = temp.trimmed;
                if (!turnOrders.empty) {
                    return turnOrders;
                }
            }
        }
        return "";
    }
    "The unit's latest results as of the given turn."
    shared default String getLatestResults(Integer turn) {
        SortedMap<Integer, String> results = allResults;
        for (i in turn..-1) {
            if (exists temp = results.get(i)) {
                String turnResults = temp.trimmed;
                if (!turnResults.empty) {
                    return turnResults;
                }
            }
        }
        return "";
    }
    "Get the latest turn that the given orders were the current orders."
    shared default Integer getOrdersTurn(String orders) {
        variable Integer retval = -1;
        for (key->item in allOrders) {
            if (item == orders, key > retval) {
                retval = key;
            }
        }
        return retval;
    }
    "A verbose description of the unit."
    shared formal String verbose;
    "Add a member."
    todo("Move to a 'mutable' interface?")
    shared formal void addMember(UnitMember member);
    "Remove a member"
    todo("Move to a 'mutable' interface?")
    shared formal void removeMember(UnitMember member);
    "Clone the unit."
    shared formal actual IUnit copy(Boolean zero);
    "The plural of Unit is Units"
    shared default actual String plural() => "Units";
    "A fixture is a subset if it is a unit with the same ID and no extra members, and all
     corresponding (by ID, presumably) members are either equal or themselves subsets."
    shared default actual Boolean isSubset(IFixture obj, Formatter ostream, String context) {
        if (obj.id == id) {
            if (is IUnit obj) {
                if (owner.playerId != obj.owner.playerId) {
                    ostream.format("%s In Unit of ID #%d:\tOwners differ%n", context, id);
                    return false;
                } else if (name != obj.name) {
                    ostream.format("%s In unit of ID #%d:\tNames differ%n", context,
                        id);
                    return false;
                } else if (kind != obj.kind) {
                    ostream.format("%s In unit of ID #%d:\tKinds differ%n",
                        context, id);
                    return false;
                }
                Map<Integer, UnitMember> ours = map { *CeylonIterable(this).map((member) => member.id->member) };
                variable Boolean retval = true;
                for (member in obj) {
                    if (exists ourMember = ours.get(member.id)) {
                        if (!ourMember.isSubset(member, ostream,
                                "``context`` In unit of kind ``kind`` named ``name`` (ID #``id``):")) {
                            retval = false;
                        }
                    } else {
                        ostream.format(
                            "%s In unit of kind %s named %s (ID %d): Extra member:\t%s, ID #%d%n",
                            context, kind, name, id, member.string, member.id);
                        retval = false;
                    }
                }
                if (retval) {
                    if ({name, kind}.contains("unassigned"), !CeylonIterable(this).empty,
                            CeylonIterable(obj).empty) {
                        ostream.format(
                            "%s In unit of kind %s named %s (ID #%d): Non empty 'unassigned' when submap has it empty%n",
                            context, kind, name, id);
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                ostream.format("%s\tDifferent kinds of fixtures for ID #%d%n", context, id);
                return false;
            }
        } else {
            ostream.format("%s\tFixtures have different IDs%n", context);
            return false;
        }
    }
}