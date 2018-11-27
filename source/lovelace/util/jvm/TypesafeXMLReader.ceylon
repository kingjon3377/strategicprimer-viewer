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
import lovelace.util.common {
    MalformedXMLException
}

"""A wrapper around [[XMLEventReader]] that hides its "raw type" from callers,
   additionally satisfying [[the Ceylon Iterable interface|Iterable]] instead
   of [[the Java Iterable interface|java.lang::Iterable]].  Also contains
   factory methods so callers don't need to deal *at all* with the object this
   wraps. If the provided reader is [[Closeable|JCloseable]], we call its
   `close` method before returning [[finished]], to help mitigate the resource
   leak in IncludingIterator. Callers can also pass in additional methods
   to call before returning [[finished]]."""
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
            try {
                wrapped = XMLInputFactory.newInstance().createXMLEventReader(reader);
            } catch (XMLStreamException except) {
                throw MalformedXMLException(except);
            }
            closeHandles.offer(reader.close);
        }
        closeMethods.each(closeHandles.offer);
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
    throws(`class MalformedXMLException`, "on malformed XML")
    shared actual XMLEvent|Finished next() {
        if (closed) {
            return finished;
        } else {
            try {
                if (wrapped.hasNext(), exists retval = wrapped.nextEvent()) {
                    return retval;
                } else {
                    destroy(null);
                    return finished;
                }
            } catch (XMLStreamException exception) {
                throw MalformedXMLException(exception);
            }
        }
    }
}
