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
import ceylon.collection {
	Queue,
	LinkedList
}
"A wrapper around [[XMLEventReader]] that makes the Iterator declaration take a type
 argument. Also contains factory methods so callers don't need to deal *at all* with the
 object this wraps. If the provided reader is [[Closeable|JCloseable]], we call its `close`
 method before returning [[finished]], to help mitigate the resource leak in IncludingIterator."
shared class TypesafeXMLEventReader satisfies Iterator<XMLEvent>&Destroyable {
    XMLEventReader wrapped;
    Queue<Anything()> closeHandles = LinkedList<Anything()>();
    variable Boolean closed = false;
    shared new (XMLEventReader|JReader reader, {Anything()*} closeMethods = []) {
        if (is XMLEventReader reader) {
            wrapped = reader;
            if (is JCloseable reader) {
                closeHandles.offer((reader of JCloseable).close);
            }
        } else {
            wrapped = XMLInputFactory.newInstance().createXMLEventReader(reader);
            closeHandles.offer(reader.close);
        }
        for (method in closeMethods) {
            closeHandles.offer(method);
        }
    }
    "Close the wrapped stream and prevent reading any further data."
    shared actual void destroy(Throwable? error) {
        wrapped.close();
        while (exists handle = closeHandles.accept()) {
            handle();
        }
        closed = true;
        if (exists error) {
            throw error;
        }
    }
    throws(`class XMLStreamException`, "on malformed XML")
    shared actual XMLEvent|Finished next() {
        if (closed) {
            return finished;
        } else {
	        if (wrapped.hasNext(), exists retval = wrapped.nextEvent()) {
	            return retval;
	        } else {
	            destroy(null);
	            return finished;
	        }
        }
    }
}
