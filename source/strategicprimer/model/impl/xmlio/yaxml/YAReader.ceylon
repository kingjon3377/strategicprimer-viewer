import strategicprimer.model.common.xmlio {
    SPFormatException
}

import java.io {
    IOException
}

import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent
}

"""An interface for XML readers that can read multiple related types, in the sixth
   generation of SP XML I/O ("yet another SP XML reader")."""
interface YAReader<out Item, in Value=Item> given Item satisfies Object {
    "Read an object from XML."
    throws(`class SPFormatException`, "on SP format errors")
    shared formal Item read(
            "The element (XML tag) being parsed"
            StartElement element,
            "The parent tag"
            QName parent,
            "The stream of XML events to read more from"
            {XMLEvent*} stream);
    "Write an object to the stream."
    throws(`class IOException`, "on I/O error in writing")
    shared formal void write(
            "The stream to write to"
            Anything(String) ostream,
            "The object to write"
            Value obj,
            "The current indentation level"
            Integer indentation);
    "Whether we can read the given tag."
    shared formal Boolean isSupportedTag(String tag);
    "Whether we can write the given object."
    shared default Boolean canWrite(Object obj) => obj is Item;
    "Write the given object, when the caller knows the object is the right type but the
     typechecker doesn't. This will probably crash the program if the types don't in fact
     match."
    shared default void writeRaw(Anything(String) ostream, Object obj,
            Integer indentation) {
        assert (is Value obj);
        write(ostream, obj, indentation);
    }
}
