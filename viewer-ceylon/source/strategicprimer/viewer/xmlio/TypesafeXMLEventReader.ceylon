import java.io {
    JReader=Reader
}

import javax.xml.stream {
    XMLEventReader,
    XMLInputFactory,
    XMLStreamException
}
import javax.xml.stream.events {
    XMLEvent
}
"A wrapper around [[XMLEventReader]] that makes the Iterator declaration take a type
 argument. Also contains factory methods so callers don't need to deal *at all* with the
 object this wraps."
shared class TypesafeXMLEventReader satisfies Iterator<XMLEvent> {
    XMLEventReader wrapped;
    shared new (XMLEventReader|JReader reader) {
        if (is XMLEventReader reader) {
            wrapped = reader;
        } else {
            wrapped = XMLInputFactory.newInstance().createXMLEventReader(reader);
        }
    }
    throws(`class XMLStreamException`, "on malformed XML")
    shared actual XMLEvent|Finished next() {
        if (exists retval = wrapped.nextEvent()) {
            return retval;
        } else {
            return finished;
        }
    }
}