import ceylon.collection {
    MutableList,
    ArrayList,
    MutableMap,
    naturalOrderTreeMap,
    SortedMap,
    TreeMap
}
import ceylon.logging {
    Logger,
    logger
}

import java.lang {
    IllegalArgumentException,
    IllegalStateException
}
import java.util {
    Formatter
}

import lovelace.util.common {
    todo
}

import model.map {
    IFixture,
    TileFixture,
    Player,
    PlayerImpl
}
import strategicprimer.viewer.model.map.fixtures {
    UnitMember
}

import strategicprimer.viewer.model.map {
    HasMutableName,
    HasMutableKind,
    HasMutableOwner,
    HasMutableImage
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    ProxyFor,
    IUnit
}
import strategicprimer.viewer.model.map.fixtures.mobile.worker {
    ProxyWorker
}
Logger log = logger(`module strategicprimer.viewer`);
"A proxy for units in multiple maps, or all a player's units of one kind."
shared class ProxyUnit satisfies IUnit&ProxyFor<IUnit>&HasMutableKind&HasMutableImage
        &HasMutableName&HasMutableOwner {
    "If true, we are proxying parallel units in different maps; if false, multiple units
     of the same kind owned by one player."
    shared actual Boolean parallel;
    "The units we are a proxy for."
    MutableList<IUnit> proxiedList = ArrayList<IUnit>();
    SortedMap<Integer, String> mergeMaps(SortedMap<Integer, String>(IUnit) method) {
        MutableMap<Integer,String>&SortedMap<Integer, String> retval =
                TreeMap<Integer, String>((x, y) => x <=> y, {});
        for (map in proxiedList.map(method)) {
            for (key-> item in map) {
                if (exists existing = retval.get(key)) {
                    if (item != existing) {
                        retval.put(key, "");
                    }
                } else {
                    retval.put(key, item);
                }
            }
        }
        return retval;
    }
    "Call a function on every proxied member, and return the value returned if it was
     always the same, or else a provided value."
    Result getCommonValue<Result>(Result(IUnit) method, Result ifEmpty, Result ifDiffer)
            given Result satisfies Object {
        variable Result? retval = null;
        for (unit in proxiedList) {
            Result current = method(unit);
            if (exists temp = retval) {
                if (temp != current) {
                    return ifDiffer;
                }
            } else {
                retval = current;
            }
        }
        if (exists temp = retval) {
            return temp;
        } else {
            return ifEmpty;
        }
    }
    "If we're proxying parallel units, their ID; if we're proxying all units of a given
     kind, their kind."
    variable Integer|String identifier;
    shared new fromParallelMaps(Integer idNum) {
        identifier = idNum;
        parallel = true;
    }
    shared new forKind(String unitKind) {
        identifier = unitKind;
        parallel = false;
    }
    "All orders shared by all the proxied units."
    shared actual SortedMap<Integer, String> allOrders => mergeMaps(IUnit.allOrders);
    "All results shared by all the proxied units."
    shared actual SortedMap<Integer, String> allResults => mergeMaps(IUnit.allResults);
    shared actual IUnit copy(Boolean zero) {
        ProxyUnit retval;
        if (parallel) {
            assert (is Integer temp = identifier);
            retval = ProxyUnit.fromParallelMaps(temp);
        } else {
            assert (is String temp = identifier);
            retval = ProxyUnit.forKind(temp);
        }
        for (unit in proxiedList) {
            retval.addProxied(unit.copy(zero));
        }
        return retval;
    }
    Player defaultPlayer = PlayerImpl(-1, "proxied");
    shared actual Player owner => getCommonValue(IUnit.owner, defaultPlayer, defaultPlayer);
    shared actual String kind {
        if (parallel) {
            return getCommonValue(IUnit.kind, "proxied", "proxied");
        } else {
            assert (is String temp = identifier);
            return temp;
        }
    }
    shared actual String shortDesc() {
        if (parallel || proxiedList.size == 1) {
            if (owner.current) {
                return "a(n) ``kind`` unit belonging to you";
            } else if (owner.independent) {
                return "an independent ``kind`` unit";
            } else {
                return "a(n) ``kind`` unit belonging to ``owner``";
            }
        } else {
            return "Multiple units of kind ``kind``";
        }
    }
    shared actual Integer id {
        if (is Integer temp = identifier) {
            return temp;
        } else {
            return -1;
        }
    }
    todo("Implement") // FIXME
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        log.warn("ProxyUnit.equalsIgnoringID called");
        throw IllegalStateException("FIXME: implement equalsIgnoringID");
    }
    shared actual Integer compareTo(TileFixture fixture) {
        log.warn("ProxyUnit.compareTo called");
        return super.compareTo(fixture);
    }
    shared actual String defaultImage => getCommonValue(IUnit.defaultImage, "", "unit.png");
    shared actual String image => getCommonValue(IUnit.image, "", "");
    assign image {
        log.warn("ProxyUnit.image setter called");
        for (unit in proxiedList) {
            if (is HasMutableImage unit) {
                unit.image = image;
            } else {
                log.warn("image setter skipped unit with immutable image");
            }
        }
    }
    assign kind {
        // TODO: Should the second condition here be negated too?
        if (!parallel || identifier is Integer) {
            identifier = kind;
        }
        for (unit in proxiedList) {
            if (is HasMutableKind unit) {
                unit.kind = kind;
            } else {
                log.error("ProxyUnit.kind setter skipped unit with immutable kind");
            }
        }
    }
    shared actual Iterator<UnitMember> iterator() {
        if (!parallel) {
            return {}.iterator();
        } // else
        MutableMap<Integer, UnitMember&ProxyFor<UnitMember>|IWorker&ProxyFor<IWorker>> map =
                naturalOrderTreeMap<Integer, UnitMember&ProxyFor<UnitMember>|IWorker&ProxyFor<IWorker>>({});
        for (unit in proxiedList) {
            for (member in unit) {
                UnitMember&ProxyFor<UnitMember>|IWorker&ProxyFor<IWorker> proxy;
                Integer memberID = member.id;
                if (exists temp = map.get(memberID)) {
                    proxy = temp;
                    if (is IWorker&ProxyFor<IWorker> proxy) {
                        if (is IWorker member) {
                            proxy.addProxied(member);
                        } else {
                            log.warn("ProxyWorker matched non-worker");
                        }
                    } else {
                        proxy.addProxied(member);
                    }
                } else {
                    if (is IWorker member) {
                        proxy = ProxyWorker.fromWorkers(member);
                    } else {
                        proxy = ProxyMember(member);
                    }
                    map.put(memberID, proxy);
                }
            }
        }
        return map.items.iterator();
    }
    shared actual String name => getCommonValue(IUnit.name, "proxied", "proxied");
    assign name {
        for (unit in proxiedList) {
            if (is HasMutableName unit) {
                unit.name = name;
            } else {
                log.error("ProxyUnit.name setter skipped unit with immutable name");
            }
        }
    }
    assign owner {
        for (unit in proxiedList) {
            if (is HasMutableOwner unit) {
                unit.owner = owner;
            } else {
                log.error("ProxyUnit.owner setter skipped unit with immutable owner");
            }
        }
    }
    shared actual Boolean isSubset(IFixture obj, Formatter ostream, String context) {
        ostream.format("%sCalled ProxyUnit.isSubset()%n", context);
        return super.isSubset(obj, ostream, "``context``\tIn proxy unit:");
    }
    shared actual String getOrders(Integer turn) =>
            getCommonValue((unit) => unit.getOrders(turn), "", "");
    shared actual String getResults(Integer turn) =>
            getCommonValue((unit) => unit.getResults(turn), "", "");
    shared actual void setOrders(Integer turn, String newOrders) {
        for (unit in proxiedList) {
            unit.setOrders(turn, newOrders);
        }
    }
    shared actual void setResults(Integer turn, String newResults) {
        for (unit in proxiedList) {
            unit.setResults(turn, newResults);
        }
    }
    shared actual String verbose {
        if (parallel) {
            if (exists first = proxiedList.first) {
                return "A proxy for units in several maps, such as the following:
                        ``first.verbose``";
            } else {
                return "A proxy for units in several maps, but no units yet.";
            }
        } else {
            assert (is String temp = identifier);
            return "A proxy for units of kind ``temp``";
        }
    }
    shared actual void addMember(UnitMember member) {
        if (parallel) {
            for (unit in proxiedList) {
                if (!unit.any(member.equals)) {
                    unit.addMember(member.copy(false));
                }
            }
        } else {
            log.error("addMember() called on proxy for all units of one kind");
        }
    }
    todo("FIXME: is this really right?")
    shared actual void removeMember(UnitMember member) {
        if (parallel) {
            for (unit in proxiedList) {
                if (exists found = unit.find(member.equals)) {
                    unit.removeMember(found);
                }
            }
        } else {
            log.error("RemoveMember() called on proxy for all units of one kind");
        }
    }
    shared actual Iterable<IUnit> proxied => {*proxiedList};
    shared actual String string => (parallel) then "ProxyUnit for ID #``identifier``"
        else "ProxyUnit for units of kind ``identifier``";
    shared actual Boolean equals(Object obj) {
        if (is ProxyUnit obj) {
            return parallel == obj.parallel && identifier == obj.identifier &&
                proxied == obj.proxied;
        } else {
            return false;
        }
    }
    shared actual Integer hash =>
            proxiedList.map(Object.hash).fold(0)((left, right) => left.or(right));
    shared actual Integer dc => getCommonValue(IUnit.dc, 10, 10);
    "Proxy an additonal unit."
    shared actual void addProxied(IUnit item) {
        if (is Identifiable item, item === this) {
            return;
        } else if (parallel, identifier is Integer, identifier != item.id) {
            throw IllegalArgumentException("Expected unit with ID #``identifier``");
        }  else if (!parallel, identifier is String, identifier != item.kind) {
            throw IllegalArgumentException("Expected unit of kind ``identifier``");
        } else {
            proxiedList.add(item);
        }
    }
}