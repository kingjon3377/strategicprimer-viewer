import ceylon.collection {
    MutableList,
    ArrayList,
    MutableMap,
    naturalOrderTreeMap,
    HashMap,
    linked,
    SortedMap
}

import strategicprimer.model.common.map {
    IFixture,
    TileFixture,
    Player,
    PlayerImpl
}
import strategicprimer.model.common.map.fixtures {
    UnitMember
}
import strategicprimer.model.common.map.fixtures.mobile {
    ProxyFor,
    IUnit,
    ProxyMember,
    IWorker
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    ProxyWorker
}

import ceylon.language {
    createMap=map
}

"A proxy for units in multiple maps, or all a player's units of one kind."
shared class ProxyUnit satisfies IUnit&ProxyFor<IUnit> {
    "If true, we are proxying parallel units in different maps; if false, multiple units
     of the same kind owned by one player."
    shared actual Boolean parallel;

    "The units we are a proxy for."
    MutableList<IUnit> proxiedList = ArrayList<IUnit>();

    variable {UnitMember*} cachedIterable = [];

    String mergeHelper(String earlier, String later) =>
        if (earlier == later) then earlier else "";

    SortedMap<Integer, String> mergeMaps(SortedMap<Integer, String>(IUnit) method) =>
        naturalOrderTreeMap(createMap(proxiedList.map(method).flatMap(identity), mergeHelper));

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

    shared actual String kind {
        if (parallel) {
            return getConsensus(IUnit.kind) else "proxied";
        } else {
            assert (is String temp = identifier);
            return temp;
        }
    }

    shared actual String shortDescription {
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

    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        log.error("ProxyUnit.equalsIgnoringID called");
        if (is ProxyUnit fixture) {
            return proxied.every((item) => fixture.proxied.any(item.equals));
        } else {
            return false;
        }
    }

    shared actual Comparison compare(TileFixture fixture) {
        log.warn("ProxyUnit.compare called");
        return super.compare(fixture);
    }

    shared actual String defaultImage {
        if (proxiedList.empty) {
            return "";
        } else if (exists img = getConsensus(IUnit.defaultImage)) {
            return img;
        } else {
            return "unit.png";
        }
    }

    shared actual String image => getConsensus(IUnit.image) else "";

    shared actual Iterator<UnitMember> iterator() {
        if (!parallel) {
            return emptyIterator;
        } // else
        if (proxiedList.empty) {
            return emptyIterator;
        } else if (!cachedIterable.empty) {
            return cachedIterable.iterator();
        } else {
            MutableMap<Integer, UnitMember&ProxyFor<UnitMember>|Animal&ProxyFor<Animal>|
                        IWorker&ProxyFor<IWorker>> map =
                    HashMap<Integer, UnitMember&ProxyFor<UnitMember>|Animal&ProxyFor<Animal>|
                        IWorker&ProxyFor<IWorker>>(linked);
            for (member in proxiedList.flatMap(identity)) {
                UnitMember&ProxyFor<UnitMember>|Animal&ProxyFor<Animal>|
                    IWorker&ProxyFor<IWorker> proxy;
                Integer memberID = member.id;
                if (exists temp = map[memberID]) {
                    proxy = temp;
                    if (is IWorker&ProxyFor<IWorker> proxy) {
                        if (is IWorker member) {
                            proxy.addProxied(member);
                        } else {
                            log.warn("ProxyWorker matched non-worker");
                        }
                    } else if (is Animal&ProxyFor<Animal> proxy) {
                        if (is Animal member) {
                            proxy.addProxied(member);
                        } else {
                            log.warn("ProxyAnimal matched non-animal");
                        }
                    } else {
                        proxy.addProxied(member);
                    }
                } else {
                    if (is IWorker member) {
                        proxy = ProxyWorker.fromWorkers(member);
                    } else if (is Animal member) {
                        proxy = ProxyAnimal(member);
                    } else {
                        proxy = ProxyMember(member);
                    }
                    map[memberID] = proxy;
                }
            }
            cachedIterable = map.items;
            return map.items.iterator();
        }
    }

    shared actual String name => getConsensus(IUnit.name) else "proxied";

    shared actual String portrait => getConsensus(IUnit.portrait) else "";

    shared actual Player owner => getConsensus(IUnit.owner) else defaultPlayer;

    shared actual Boolean isSubset(IFixture obj, Anything(String) report) {
        report("Called ProxyUnit.isSubset()");
        return super.isSubset(obj, compose(report, "In proxy unit:\t".plus));
    }

    shared actual String getOrders(Integer turn) =>
            getConsensus(shuffle(IUnit.getOrders)(turn)) else "";

    shared actual String getResults(Integer turn) =>
            getConsensus(shuffle(IUnit.getResults)(turn)) else "";

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

    shared actual {IUnit*} proxied => proxiedList.sequence();

    shared actual String string => (parallel) then "ProxyUnit for ID #``identifier``"
        else "ProxyUnit for units of kind ``identifier``";

    shared actual Boolean equals(Object obj) {
        if (is ProxyUnit obj) {
            return parallel == obj.parallel && identifier == obj.identifier &&
                proxiedList == obj.proxiedList;
        } else {
            return false;
        }
    }

    Integer hashAccumulator(Integer left, Integer right) => left.or(right);

    shared actual Integer hash =>
            proxiedList.map(Object.hash).fold(0)(hashAccumulator);

    shared actual Integer dc => getConsensus(IUnit.dc) else 10;

    "Proxy an additonal unit."
    shared actual void addProxied(IUnit item) {
        if (item === this) {
            return;
        } else if (parallel) {
            "Unit must have ID #``identifier``"
            assert (identifier is Integer, identifier == item.id);
        } else {
            "Unit must have kind ``identifier``"
            assert (identifier is String, identifier == item.kind);
        }
        cachedIterable = [];
        proxiedList.add(item);
    }
}
