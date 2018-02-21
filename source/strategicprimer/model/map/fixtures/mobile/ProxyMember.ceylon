import ceylon.collection {
    MutableList,
    ArrayList
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.map {
    IFixture
}
import strategicprimer.model.map.fixtures {
    UnitMember
}
import strategicprimer.model.map.fixtures.mobile {
    ProxyFor
}
"A proxy for non-worker unit members."
class ProxyMember satisfies UnitMember&ProxyFor<UnitMember> {
    MutableList<UnitMember> proxiedMembers = ArrayList<UnitMember>();
    new noop() {}
    shared new (UnitMember member) { proxiedMembers.add(member); }
    shared actual void addProxied(UnitMember item) => proxiedMembers.add(item);
    shared actual ProxyMember copy(Boolean zero) {
        ProxyMember retval = ProxyMember.noop();
        for (member in proxiedMembers) {
            retval.addProxied(member.copy(zero));
        }
        return retval;
    }
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        log.warn("ProxyMember.equalsIgnoringID() called");
        if (is ProxyMember fixture) {
            return fixture.proxiedMembers == proxiedMembers;
        } else {
            return false;
        }
    }
    shared actual Boolean isSubset(IFixture fixture, Anything(String) report) {
        report("isSubset called on ProxyMember");
        return false;
    }
    shared actual Iterable<UnitMember> proxied => {*proxiedMembers};
    todo("Implement properly")
    shared actual String string {
        if (exists first = proxiedMembers.first) {
            return first.string;
        } else {
            return "a proxy for no unit members";
        }
    }
    shared actual Boolean parallel = true;
    "Returns the ID number shared by all the proxied members, or -1 if either there are no
     proxied members or some have a different ID."
    shared actual Integer id => getConsensus(UnitMember.id) else -1;
}
