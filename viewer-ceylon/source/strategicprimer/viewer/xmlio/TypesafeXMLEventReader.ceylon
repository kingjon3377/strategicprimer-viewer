import java.io {
    JReader=Reader
}
import java.util {
    JIterator=Iterator,
    NoSuchElementException
}

import javax.xml.stream {
    XMLEventReader,
    XMLInputFactory,
    XMLStreamException
}
import javax.xml.stream.events {
    XMLEvent
}

import lovelace.util.common {
    todo
}
"A wrapper around [[XMLEventReader]] that makes the Iterator declaration take a type
 argument. Also contains factory methods so callers don't need to deal *at all* with the
 object this wraps."
todo("Satisfy Ceylon Iterator instead")
shared class TypesafeXMLEventReader satisfies JIterator<XMLEvent> {
    XMLEventReader wrapped;
    shared new (XMLEventReader|JReader reader) {
        if (is XMLEventReader reader) {
            wrapped = reader;
        } else {
            wrapped = XMLInputFactory.newInstance().createXMLEventReader(reader);
        }
    }
    shared actual Boolean hasNext() => wrapped.hasNext();
    shared actual XMLEvent next() {
        try {
            if (exists retval = wrapped.nextEvent()) {
                return retval;
            } else {
                throw NoSuchElementException("next event was null");
            }
        } catch (XMLStreamException except) {
            throw NoSuchElementBecauseException("Malformed XML", except);
        }
    }
    shared actual void remove() => wrapped.remove();
}