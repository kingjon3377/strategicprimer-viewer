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
class ConvertingIterator<Element>(JIterator<out Anything> iter)
        satisfies Iterator<Element> given Element satisfies Object {
    shared actual Element|Finished next() {
        if (iter.hasNext()) {
            assert (is Element retval = iter.next());
            return retval;
        } else {
            return finished;
        }
    }
}
"A wrapper around a Java Iterable or Iterator that meets the Ceylon Iterable interface and
 asserts that each element is of the specified type."
shared class ConvertingIterable<Element>(JIterator<out Object>|JIterable<out Object> iter)
        satisfies Iterable<Element> given Element satisfies Object {
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
    shared actual Integer? lastIndex =>
            if (wrapped.size == 0) then null else wrapped.size - 1;
    shared actual Integer hash => wrapped.hash;
    shared actual Boolean equals(Object that) =>
            if (is ListModelWrapper<out Anything> that) then wrapped==that.wrapped
            else false;
}
