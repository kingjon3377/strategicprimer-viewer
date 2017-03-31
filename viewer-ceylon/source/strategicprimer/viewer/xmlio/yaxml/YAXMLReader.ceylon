import java.io {
    JReader=Reader,
    IOException
}
import java.lang {
    IllegalStateException
}
import java.nio.file {
    JPath=Path,
    JFiles=Files
}

import javax.xml.namespace {
    QName
}
import javax.xml.stream {
    XMLStreamException
}
import javax.xml.stream.events {
    XMLEvent,
    StartElement
}

import lovelace.util.common {
    IteratorWrapper
}

import strategicprimer.viewer.model {
    IDFactory,
    IDRegistrar
}
import strategicprimer.viewer.model.map {
    IMutableMapNG
}
import strategicprimer.viewer.xmlio {
    Warning,
    ISPReader,
    IncludingIterator,
    TypesafeXMLEventReader,
    IMapReader,
    SPFormatException
}
"Sixth-generation SP XML reader."
shared object yaXMLReader satisfies IMapReader&ISPReader {
    "Read an object from XML."
    throws(`class XMLStreamException`, "if the XML isn't well-formed")
    throws(`class SPFormatException`, "on SP XML format error")
    throws(`class IllegalStateException`,
        "if a reader produces a different type than requested")
    shared actual Element readXML<Element>(
            "The file we're reading from" JPath file,
            "The stream to read from" JReader istream,
            "The Warning instance to use for warnings" Warning warner)
            given Element satisfies Object {
        Iterator<XMLEvent> reader = TypesafeXMLEventReader(istream);
        {XMLEvent*} eventReader = IteratorWrapper(IncludingIterator(file, reader));
        IDRegistrar idFactory = IDFactory();
        if (exists event = eventReader.narrow<StartElement>().first) {
            Object retval = YAReaderAdapter(warner, idFactory).parse(event, QName("root"),
                eventReader);
            if (is Element retval) {
                return retval;
            } else {
                throw IllegalStateException(
                    "Reader produced different type than we expected");
            }
        } else {
            throw XMLStreamException("XML stream didn't contain a start element");
        }
    }
    "Read a map from a stream."
    throws(`class XMLStreamException`, "on malformed XML")
    throws(`class SPFormatException`, "on SP format problems")
    shared actual IMutableMapNG readMapFromStream("The file we're reading from" JPath file,
            "The stream to read from" JReader istream,
            "The Warning instance to use for warnings" Warning warner) =>
                readXML(file, istream, warner);
    "Read a map from XML."
    throws(`class IOException`, "on I/O error")
    throws(`class XMLStreamException`, "on malformed XML")
    throws(`class SPFormatException`, "on SP format problems")
    shared actual IMutableMapNG readMap("The file to read from" JPath file,
            "The Warning instance to use for warnings" Warning warner) {
        try (istream = JFiles.newBufferedReader(file)) {
            return readMapFromStream(file, istream, warner);
        }
    }
}