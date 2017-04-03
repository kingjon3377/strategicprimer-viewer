import ceylon.logging {
    Logger,
    logger
}
import ceylon.test {
    test,
    assertEquals
}

import java.io {
    JReader=Reader
}
import java.nio.file {
    JPath=Path,
    JPaths=Paths
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.map {
    IMutableMapNG,
    IMapNG
}
import strategicprimer.model.xmlio.yaxml {
    yaXMLReader,
    yaXMLWriter
}

"A logger."
Logger log = logger(`module strategicprimer.model`);
IMapReader reader = yaXMLReader;
SPWriter writer = yaXMLWriter;
"Turn a series of Strings into a series of equvalent Paths, optionally omitting the
 first."
todo("Do we really need dropFirst in Ceylon?")
shared {JPath*} namesToFiles(Boolean dropFirst, String* names) {
    if (dropFirst) {
        return {for (name in names.rest) JPaths.get(name) };
    } else {
        return { for (name in names) JPaths.get(name) };
    }
}
"Read a map from a file or a stream.."
todo("Add a default value for Warning argument", "Port to use ceylon.file or ceylon.io")
shared IMutableMapNG readMap(JPath|JReader file, Warning warner) {
    if (is JPath file) {
        return reader.readMap(file, warner);
    } else {
        return reader.readMapFromStream(JPaths.get(""), file, warner);
    }
}
"Write a map to file."
shared void writeMap(JPath file, IMapNG map) => writer.write(file, map);
test
void testNamesToFiles() {
    JPath[] expected = [ JPaths.get("two"), JPaths.get("three"), JPaths.get("four") ];
    assertEquals([*namesToFiles(false, "two", "three", "four")], expected,
        "Returns all names when dropFirst is false");
    assertEquals([*namesToFiles(true, "one", "two", "three", "four")], expected,
        "Drops first name when dropFirst is true");
}
