import ceylon.collection {
    ArrayList,
    HashMap,
    MutableList,
    MutableMap
}

import strategicprimer.model.common.map {
    IFixture
}

import lovelace.util.common {
    NonNullCorrespondence
}

shared class CoalescedHolder<Type,Key>(Key(Type) extractor, shared Type({Type+}) combiner)
        satisfies NonNullCorrespondence<Type, MutableList<Type>>&{List<Type>*}
        given Type satisfies IFixture given Key satisfies Object {
    MutableMap<Key, MutableList<Type>> map = HashMap<Key, MutableList<Type>>();

    shared actual Boolean defines(Type key) => true;

    shared variable String plural = "unknown";

    shared actual MutableList<Type> get(Type item) {
        Key key = extractor(item);
        plural = item.plural;
        if (exists retval = map[key]) {
            return retval;
        } else {
            MutableList<Type> retval = ArrayList<Type>();
            map[key] = retval;
            return retval;
        }
    }

    shared actual Iterator<List<Type>> iterator() => map.items.iterator();

    shared void addIfType(Anything item) {
        if (is Type item) {
            get(item).add(item);
        }
    }

    shared Type combineRaw({IFixture+} list) {
        assert (is {Type+} list);
        return combiner(list);
    }
}
