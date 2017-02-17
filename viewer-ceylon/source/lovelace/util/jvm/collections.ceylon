import ceylon.collection {
    ArrayList
}
import ceylon.math.float {
    random
}
import java.util {
    JIterator=Iterator
}
import java.lang {
    JIterable=Iterable
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