import ceylon.file {
    parsePath,
    File,
    Reader
}

import java.io {
    StringReader
}

import strategicprimer.model.common.map {
    IMapNG
}
import strategicprimer.model.common.xmlio {
    Warning,
    warningLevels
}
import strategicprimer.model.impl.xmlio {
    IMapReader,
    testReaderFactory,
    SPWriter
}
import strategicprimer.drivers.common {
    UtilityDriver,
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SPOptions,
    DriverFactory,
    UtilityDriverFactory
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import lovelace.util.common {
    PathWrapper
}

"A factory for a driver to compare the performance and results of the two map reading
 implementations."
service(`interface DriverFactory`)
shared class ReaderComparatorFactory() satisfies UtilityDriverFactory {
    shared actual IDriverUsage usage = DriverUsage(false, ["--test", "--compare-readers"],
        ParamCount.atLeastOne, "Test map readers",
        "Test map-reading implementations by comparing their results on the same file.",
        true, false);
    shared actual UtilityDriver createDriver(ICLIHelper cli, SPOptions options) =>
            ReaderComparator(cli);
}
"A driver for comparing map readers."
shared class ReaderComparator satisfies UtilityDriver {
    static String readAll(PathWrapper path) {
        assert (is File file = parsePath(path.filename).resource);
        Reader reader = file.Reader();
        StringBuilder builder = StringBuilder();
        while (exists String line = reader.readLine()) {
            builder.append(line);
            builder.appendNewline();
        }
        return builder.string;
    }
    ICLIHelper cli;
    shared new (ICLIHelper cli) {
        this.cli = cli;
    }
    "Compare the two readers' performance on the given files."
    shared actual void startDriver(String* args) {
        IMapReader readerOne = testReaderFactory.oldReader;
        IMapReader readerTwo = testReaderFactory.newReader;
        SPWriter writerOne = testReaderFactory.oldWriter;
        SPWriter writerTwo = testReaderFactory.newWriter;
        Warning warner = warningLevels.ignore;
        for (arg in args) {
            cli.println("``arg``:");
            PathWrapper path = PathWrapper(arg);
            if (path.possiblyReadable) {
                String contents = readAll(path);
                Integer readStartOne = system.nanoseconds;
                IMapNG mapOne = readerOne.readMapFromStream(path,
                    StringReader(contents), warner);
                Integer readEndOne = system.nanoseconds;
                print("Old reader took ``readEndOne - readStartOne``");
                Integer readStartTwo = system.nanoseconds;
                IMapNG mapTwo = readerTwo.readMapFromStream(path,
                    StringReader(contents), warner);
                Integer readEndTwo = system.nanoseconds;
                print("New reader took ``readEndTwo - readStartTwo``");
                if (mapOne == mapTwo) {
                    print("Readers produce identical results");
                } else {
                    print("Readers differ on ``arg``");
                }
                StringBuilder outOne = StringBuilder();
                Integer writeStartOne = system.nanoseconds;
                writerOne.write(outOne.append, mapOne);
                Integer writeEndOne = system.nanoseconds;
                print("Old writer took ``writeEndOne - writeStartOne``");
                StringBuilder outTwo = StringBuilder();
                Integer writeStartTwo = system.nanoseconds;
                writerTwo.write(outTwo.append, mapTwo);
                Integer writeEndTwo = system.nanoseconds;
                print("New writer took ``writeEndTwo - writeStartTwo``");
                if (outOne.string == outTwo.string) {
                    print("Writers produce identical results");
                } else if (outOne.string.trimmed == outTwo.string.trimmed) {
                    print("Writers produce identical results except for whitespace");
                } else {
                    print("Writers differ on ``arg``");
                }
            }
        }
    }
}
