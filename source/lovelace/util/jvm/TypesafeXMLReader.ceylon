import java.io {
    JReader=Reader,
	JCloseable=Closeable
}

import javax.xml.stream {
    XMLEventReader,
    XMLStreamException,
    XMLInputFactory
}
import javax.xml.stream.events {
    XMLEvent
}
"A wrapper around [[XMLEventReader]] that makes the Iterator declaration take a type
 argument. Also contains factory methods so callers don't need to deal *at all* with the
 object this wraps. If the provided reader is [[Closeable|JCloseable]], we call its `close`
 method before returning [[finished]], to help mitigate the resource leak in IncludingIterator."
shared class TypesafeXMLEventReader satisfies Iterator<XMLEvent> {
    XMLEventReader wrapped;
    JCloseable? handle;
    variable Boolean closed = false;
    shared new (XMLEventReader|JReader reader) {
        if (is XMLEventReader reader) {
            wrapped = reader;
            if (is JCloseable reader) {
                handle = reader;
            } else {
                handle = null; // TODO: allow caller to pass in one (or more?) handles to close when we finish
            }
        } else {
            wrapped = XMLInputFactory.newInstance().createXMLEventReader(reader);
            handle = reader;
        }
    }
    throws(`class XMLStreamException`, "on malformed XML")
    shared actual XMLEvent|Finished next() {
        if (wrapped.hasNext(), exists retval = wrapped.nextEvent()) {
            return retval;
        } else {
            if (!closed) {
	            wrapped.close();
	            if (exists handle) {
	                handle.close();
	            }
	            closed = true;
	        }
            return finished;
        }
    }
}
