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
import ceylon.file {
    Path
}

"A logger."
Logger log = logger(`module strategicprimer.model`);
IMapReader reader = yaXMLReader;
SPWriter writer = yaXMLWriter;
"Turn a series of Strings into a series of equvalent Paths, optionally omitting the
 first."
shared {JPath*} namesToFiles(String* names) =>
        { for (name in names) JPaths.get(name) };
"Read a map from a file or a stream.."
todo("Port to use ceylon.file, ceylon.io, or ceylon.buffer")
shared IMutableMapNG readMap(JPath|JReader file, Warning warner = warningLevels.warn) {
    if (is JPath file) {
        return reader.readMap(file, warner);
    } else {
        return reader.readMapFromStream(JPaths.get(""), file, warner);
    }
}
"Write a map to file."
shared void writeMap(Path file, IMapNG map) => writer.write(file, map);
test
void testNamesToFiles() {
    JPath[] expected = [ JPaths.get("two"), JPaths.get("three"), JPaths.get("four") ];
    assertEquals([*namesToFiles("two", "three", "four")], expected,
        "Returns all names");
}
