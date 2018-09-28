import ceylon.collection {
    MutableList,
    ArrayList
}

import strategicprimer.model.impl.map {
    IFixture
}
import strategicprimer.model.impl.map.fixtures {
    UnitMember
}
import strategicprimer.model.impl.map.fixtures.mobile {
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
    shared actual {UnitMember*} proxied => proxiedMembers.sequence();
    shared actual Boolean parallel = true;
    shared actual String string {
        if (proxiedMembers.empty) {
            return "a proxy for no unit members";
        } else if (exists retval = getConsensus(UnitMember.string)) {
            return retval;
        } else {
            return "a proxy for a variety of unit members";
        }
    }
    "Returns the ID number shared by all the proxied members, or -1 if either there are no
     proxied members or some have a different ID."
    shared actual Integer id => getConsensus(UnitMember.id) else -1;
    shared actual String plural =>
            getConsensus(UnitMember.plural) else "Various Unit Members";
}
