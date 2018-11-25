import ceylon.file {
    parsePath
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

import lovelace.util.common {
    todo,
    PathWrapper
}

import strategicprimer.model.impl.dbio {
    spDatabaseWriter,
    spDatabaseReader
}
import strategicprimer.model.common.map {
    IMutableMapNG,
    IMapNG
}
import strategicprimer.model.impl.xmlio.fluidxml {
    SPFluidReader
}
import strategicprimer.model.impl.xmlio.yaxml {
    yaXMLWriter
}
import strategicprimer.model.common.xmlio {
    Warning,
    warningLevels
}

"A logger."
Logger log = logger(`module strategicprimer.model.impl`);

"A helper to abstract the details of specific I/O implementations to shield
 callers from them, and in particular to encapsulate the decision of which
 implementation to use in one place."
shared object mapIOHelper {
    "The reader to use to read from XML. (The FluidXML implementation turned
     out to be significantly faster than YAXML, which was written to replace it
     ...)"
    IMapReader reader = SPFluidReader();

    "The writer to use to write to XML."
    SPWriter writer = yaXMLWriter;

    "The writer to use to write to SQLite databases."
    SPWriter dbWriter = spDatabaseWriter;

    "The reader to use to read from SQLite databases."
    IMapReader dbReader = spDatabaseReader;

    "Turn a series of Strings into a series of equvalent Paths."
    shared {PathWrapper+} namesToFiles(String+ names) => names.map(PathWrapper);

    "Read a map from a file or a stream.."
    todo("Port to use ceylon.io or ceylon.buffer")
    shared IMutableMapNG readMap(PathWrapper|JReader file,
            Warning warner = warningLevels.warn) {
        log.trace("In mapIOHelper.readMap");
        if (is PathWrapper file) {
            if (file.string.endsWith(".db")) {
                log.trace("Reading from ``file`` as an SQLite database");
                return dbReader.readMap(file, warner);
            } else {
                log.trace("Reading from ``file``");
                return reader.readMap(file, warner);
            }
        } else {
            log.trace("Reading from a Reader");
            return reader.readMapFromStream(PathWrapper(""), file, warner);
        }
    }

    "Write a map to file."
    shared void writeMap(PathWrapper file, IMapNG map) {
        if (file.filename.endsWith(".db") || file.string.empty) {
            log.trace("Writing to ``file`` as an SQLite database");
            dbWriter.write(parsePath(file.filename), map);
        } else {
            log.trace("Writing to ``file``");
            writer.write(parsePath(file.filename), map);
        }
    }

    "A test that [[namesToFiles]] works as expected.

     This is something of a holdover from the Java version, since it had to
     also strip off the first one."
    test
    shared void testNamesToFiles() {
//        {Path+} expected = [ parsePath("two"), parsePath("three"), parsePath("four")];
        {PathWrapper+} expected = [ PathWrapper("two"), PathWrapper("three"),
            PathWrapper("four")];
        "[[namesToFiles]] should return all names."
        assert (corresponding(namesToFiles("two", "three", "four"), expected));
    }
}
