import ceylon.file {
    Path
}
import ceylon.logging {
    Logger,
    logger
}
import ceylon.test {
    test
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

import strategicprimer.model.dbio {
    spDatabaseWriter,
    spDatabaseReader
}
import strategicprimer.model.map {
    IMutableMapNG,
    IMapNG
}
import strategicprimer.model.xmlio.fluidxml {
    SPFluidReader
}
import strategicprimer.model.xmlio.yaxml {
    yaXMLWriter
}

"A logger."
Logger log = logger(`module strategicprimer.model`);
shared object mapIOHelper {
    IMapReader reader = SPFluidReader();
    SPWriter writer = yaXMLWriter;
    SPWriter dbWriter = spDatabaseWriter;
    IMapReader dbReader = spDatabaseReader;
    "Turn a series of Strings into a series of equvalent Paths."
    shared {JPath+} namesToFiles(String+ names) =>
            // Can't use Iterable.map() instead of a comprehension here because
		    // JPaths.get() is overloaded
            [ for (name in names) JPaths.get(name) ];
    "Read a map from a file or a stream.."
    todo("Port to use ceylon.file, ceylon.io, or ceylon.buffer")
    shared IMutableMapNG readMap(JPath|JReader file,
		    Warning warner = warningLevels.warn) {
        log.trace("In mapIOHelper.readMap");
        if (is JPath file) {
            if (file.string.endsWith(".db")) {
                log.trace("Reading from ``file`` as an SQLite database");
                return dbReader.readMap(file, warner);
            } else {
                log.trace("Reading from ``file``");
                return reader.readMap(file, warner);
            }
        } else {
            log.trace("Reading from a Reader");
            return reader.readMapFromStream(JPaths.get(""), file, warner);
        }
    }
    "Write a map to file."
    shared void writeMap(Path file, IMapNG map) {
        if (file.string.endsWith(".db") || file.string.empty) {
            log.trace("Writing to ``file`` as an SQLite database");
            dbWriter.write(file, map);
        } else {
            log.trace("Writing to ``file``");
            writer.write(file, map);
        }
    }
    test
    shared void testNamesToFiles() {
        {JPath+} expected = [ JPaths.get("two"), JPaths.get("three"),
	        JPaths.get("four") ];
        "[[namesToFiles]] should return all names."
        assert (corresponding(namesToFiles("two", "three", "four"), expected));
    }
}