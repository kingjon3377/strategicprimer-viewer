import java.io {
    JReader=Reader,
    IOException
}
import java.nio.file {
    JPath=Path
}

import javax.xml.stream {
    XMLStreamException
}

import strategicprimer.model.common.map {
    IMutableMapNG
}

import strategicprimer.model.common.xmlio {
    SPFormatException,
    Warning
}

"An interface for map readers."
shared interface IMapReader {
    "Read the map (view) contained in a file."
    throws(`class SPFormatException`, "if the reader can't handle this map version,
                                       doesn't recognize the map format, or finds the file
                                       contains format errors")
    throws(`class XMLStreamException`, "on low-level XML errors")
    throws(`class IOException`,
        "on I/O errors not covered by `XMLStreamException` or `SPFormatException`")
    shared formal IMutableMapNG readMap(
            "The file to read" JPath file,
            "The Warning instance to use for warnings" Warning warner);
    "Read a map from a [[JReader]]."
    throws(`class SPFormatException`, "if the reader can't handle this map version,
                                       doesn't recognize the map format, or finds the file
                                       contains format errors")
    throws(`class XMLStreamException`, "on low-level XML errors")
    shared formal IMutableMapNG readMapFromStream(
            "The name of the file the stream represents" JPath file,
            "The reader to read from" JReader istream,
            "The Warning instance to use for warnings" Warning warner);
}
