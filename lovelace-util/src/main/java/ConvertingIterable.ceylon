import java.util {
    JIterator=Iterator
}

import java.lang {
    JIterable=Iterable
}

"Wrapper around a [[Java Iterator|JIterator]] that meets [[the Ceylon Iterator
 interface|Iterator]] and asserts that each element is of the specified type."
see(`class ConvertingIterable`)
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

"A wrapper around a Java [[Iterable|JIterable]] or [[Iterator|JIterator]] that meets 
 [[the Ceylon Iterable interface|Iterable]] and asserts that each element is of the
 specified type."
shared class ConvertingIterable<Element>(JIterator<out Object>|JIterable<out Object> iter)
        satisfies {Element*} given Element satisfies Object {
    shared actual Iterator<Element> iterator();
    if (is JIterator<out Anything> iter) {
        iterator = () => ConvertingIterator<Element>(iter);
    } else {
        iterator = () => ConvertingIterator<Element>(iter.iterator());
    }
}
