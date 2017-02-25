import ceylon.collection {
    ArrayList,
    MutableList
}
import ceylon.math.float {
    random
}
import java.util {
    JIterator=Iterator
}
import java.lang {
    JIterable=Iterable,
    ArrayIndexOutOfBoundsException
}
import javax.swing {
    ListModel
}
"Return the elements of a list (etc.) in random order, like Java's Collections.shuffle."
by("gdejohn at https://stackoverflow.com/questions/20486670")
shared Element[] shuffle<Element>({Element*} elements, Float() randomizer = random) {
    value shuffled = ArrayList { elements = elements; };
    for (index->element in shuffled.indexed) {
        value randomIndex = (randomizer() * (index +1)).integer;
        if (randomIndex != index) {
            assert (exists randomElement = shuffled[randomIndex]);
            shuffled.set(index, randomElement);
            shuffled.set(randomIndex, element);
        }
    }
    return shuffled.size == 0 then [] else [*shuffled];
}
class ConvertingIterator<Element>(JIterator<out Anything> iter)
        satisfies Iterator<Element> {
    shared actual Element|Finished next() {
        if (iter.hasNext()) {
            assert (is Element retval = iter.next());
            return retval;
        } else {
            return finished;
        }
    }
}
shared class ConvertingIterable<Element>(JIterator<out Object>|JIterable<out Object> iter)
        satisfies Iterable<Element> {
    shared actual Iterator<Element> iterator();
    if (is JIterator<out Anything> iter) {
        iterator = () => ConvertingIterator<Element>(iter);
    } else {
        iterator = () => ConvertingIterator<Element>(iter.iterator());
    }
}
"A class to adapt a [[ListModel]] to Ceylon's [[List]] interface."
shared class ListModelWrapper<Element>(ListModel<Element> wrapped)
        satisfies List<Element> {
    shared actual Element? getFromFirst(Integer index) {
        try {
            return wrapped.getElementAt(index);
        } catch (ArrayIndexOutOfBoundsException except) {
            return null;
        }
    }
    shared actual Integer? lastIndex {
        if (wrapped.size == 0) {
            return null;
        } else {
            return wrapped.size - 1;
        }
    }
    shared actual Integer hash => wrapped.hash;
    shared actual Boolean equals(Object that) {
        if (is ListModelWrapper<out Anything> that) {
            return wrapped==that.wrapped;
        } else {
            return false;
        }
    }
}