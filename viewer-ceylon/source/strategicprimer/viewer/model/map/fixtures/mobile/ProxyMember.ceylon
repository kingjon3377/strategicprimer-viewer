import ceylon.collection {
    MutableList,
    ArrayList
}

import java.util {
    Formatter
}

import lovelace.util.common {
    todo
}

import model.map {
    IFixture
}
import strategicprimer.viewer.model.map.fixtures {
    UnitMember
}
import strategicprimer.viewer.model.map.fixtures.mobile {
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
    "Since the only user of this class proxied members that should all have the same ID
     number, we just get the ID of the first proxied member."
    todo("Remove that assumption")
    shared actual Integer id {
        if (exists first = proxiedMembers.first) {
            return first.id;
        } else {
            return -1;
        }
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
}