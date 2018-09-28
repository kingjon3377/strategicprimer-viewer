import ceylon.collection {
    SortedMap
}
import ceylon.language {
    createMap=map
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.impl.map {
    HasImage,
    IFixture,
    HasOwner,
    HasKind,
    HasName
}
import strategicprimer.model.impl.map.fixtures {
    UnitMember,
    FortressMember
}
"An interface for units."
shared interface IUnit satisfies MobileFixture&HasImage&HasKind&HasName&
        {UnitMember*}&FortressMember&HasOwner {
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
    shared default String getLatestOrders(Integer turn) =>
            (turn..-1).map(allOrders.get).coalesced.map(String.trimmed)
                .find(not(String.empty)) else "";
    "The unit's latest results as of the given turn."
    shared default String getLatestResults(Integer turn) =>
            (turn..-1).map(allResults.get).coalesced.map(String.trimmed)
                .find(not(String.empty)) else "";
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
    shared default actual String plural => "Units";
    "A fixture is a subset if it is a unit with the same ID and no extra members, and all
     corresponding (by ID, presumably) members are either equal or themselves subsets."
    shared default actual Boolean isSubset(IFixture obj, Anything(String) report) {
        if (obj.id == id) {
            if (is IUnit obj) {
	            Anything(String) localSimpleReport =
			            compose(report, "In Unit of ID #``id``:\t".plus);
                if (owner.playerId != obj.owner.playerId) {
                    localSimpleReport("Owners differ");
                    return false;
                } else if (name != obj.name) {
                    localSimpleReport("Names differ");
                    return false;
                } else if (kind != obj.kind) {
                    localSimpleReport("Kinds differ");
                    return false;
                }
                Map<Integer, UnitMember> ours =
                        createMap(map((member) => member.id->member));
                variable Boolean retval = true;
                Anything(String) localReport =
                        compose(report,
                            "In unit of ``name`` (``kind``) (ID # ``id``):\t".plus);
                for (member in obj) {
                    if (exists ourMember = ours[member.id]) {
                        if (!ourMember.isSubset(member, localReport)) {
                            retval = false;
                        }
                    } else {
                        localReport("Extra member: ``member``, ID #``member.id``");
                        retval = false;
                    }
                }
                if (retval) {
                    if ([name, kind].contains("unassigned"), !empty,
                            obj.empty) {
                        localReport(
                            """Non-empty "unassigned" when submap has it empty""");
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                report("Different kinds of fixtures for ID #``id``");
                return false;
            }
        } else {
            report("Fixtures have different IDs");
            return false;
        }
    }
}
