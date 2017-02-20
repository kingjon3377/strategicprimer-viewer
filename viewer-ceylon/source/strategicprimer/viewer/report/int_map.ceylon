import ceylon.collection {
    HashMap,
    MutableMap,
    ArrayList,
    MutableList,
    HashSet
}
import util {
    PatientMap
}
import ceylon.interop.java {
    synchronize,
    CeylonMap,
    JavaSet,
    JavaCollection,
    JavaMap
}
import java.util {
    JMap=Map, JSet=Set, JCollection=Collection
}
"A simplified Map using Integers as keys and delaying removal of items remove()d until a
 subsequent method is called."
class IntMap<Value>() satisfies PatientMap<Integer, Value> given Value satisfies Object {
    "The map that we use as a backing store."
    MutableMap<Integer, Value> backing = HashMap<Integer, Value>();
    "The list of items that have nominally been removed since the last [[coalesce]]."
    MutableList<Integer> toRemove = ArrayList<Integer>();
    "The number of correspondences in the map."
    shared actual Integer size() => backing.size;
    "Whether the map is empty."
    shared actual Boolean empty => backing.empty;
    "Whether the map contains the given key."
    shared actual Boolean containsKey(Object? key) {
        if (exists key) {
            return backing.defines(key);
        } else {
            return false;
        }
    }
    "Whether the map contains the given value."
    shared actual Boolean containsValue(Object? val) {
        if (exists val) {
            for (key->item in backing) {
                if (val == item) {
                    return true;
                }
            }
        }
        return false;
    }
    "Get the value for the given key."
    shared actual Value? get(Object key) => backing.get(key);
    "Insert the given value in the map for the given key."
    shared actual Value? put(Integer key, Value? val) {
        if (exists val) {
            return backing.put(key, val);
        } else {
            return backing.remove(key);
        }
    }
    "Schedule the given key to be removed. Doesn't actually perform the removal yet!"
    shared actual Value? remove(Object? key) {
        if (is Integer key) {
            toRemove.add(key);
        }
        if (exists key) {
            return backing.get(key);
        } else {
            return null;
        }
    }
    "Apply all scheduled removals."
    shared actual void coalesce() {
        synchronize(toRemove, () {
            for (item in toRemove) {
                backing.remove(item);
            }
            toRemove.clear();
        });
    }
    "Put all members of another map into the map."
    shared actual void putAll(JMap<out Integer, out Value> map) {
        backing.putAll(CeylonMap(map));
    }
    "Schedule all keys for removal."
    shared actual void clear() => toRemove.addAll(backing.keys);
    "The key set."
    shared actual JSet<Integer> keySet() => JavaSet<Integer>(HashSet { *backing.keys });
    "The collection of values."
    shared actual JCollection<Value> values() => JavaCollection<Value>(backing.items);
    "The entry set."
    shared actual JSet<JMap.Entry<Integer, Value>> entrySet() =>
            JavaMap<Integer, Value>(backing).entrySet();
    "The String representation of the map."
    shared actual String string => backing.string;
    "A hash value."
    shared actual Integer hash => backing.hash;
    "Whether another object is equal."
    shared actual Boolean equals(Object that) {
        if (is IntMap<Value> that) {
            return that.backing == backing;
        } else {
            return false;
        }
    }
}