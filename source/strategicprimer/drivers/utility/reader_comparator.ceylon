import ceylon.file {
    parsePath,
    File,
    Reader,
    Resource
}

import java.io {
    StringReader
}
import java.nio.file {
    JPath=Path,
    JPaths=Paths
}

import strategicprimer.model.impl.map {
    IMapNG
}
import strategicprimer.model.impl.xmlio {
    IMapReader,
    Warning,
    warningLevels,
    testReaderFactory,
    SPWriter
}
import strategicprimer.drivers.common {
    UtilityDriver,
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SPOptions,
    ISPDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}

"A driver for comparing map readers."
service(`interface ISPDriver`)
shared class ReaderComparator() satisfies UtilityDriver {
    shared actual IDriverUsage usage = DriverUsage(false, ["--test", "--compare-readers"],
        ParamCount.atLeastOne, "Test map readers",
        "Test map-reading implementations by comparing their results on the same file.",
        true, false);
    String readAll(File file) {
        Reader reader = file.Reader();
        StringBuilder builder = StringBuilder();
        while (exists String line = reader.readLine()) {
            builder.append(line);
            builder.appendNewline();
        }
        return builder.string;
    }
    "Compare the two readers' performance on the given files."
    shared actual void startDriverOnArguments(ICLIHelper cli, SPOptions options,
            String* args) {
        IMapReader readerOne = testReaderFactory.oldReader;
        IMapReader readerTwo = testReaderFactory.newReader;
        SPWriter writerOne = testReaderFactory.oldWriter;
        SPWriter writerTwo = testReaderFactory.newWriter;
        Warning warner = warningLevels.ignore;
        for (arg in args) {
            cli.println("``arg``:");
            Resource file = parsePath(arg).resource;
            JPath path = JPaths.get(arg);
            if (is File file) {
                String contents = readAll(file);
                Integer readStartOne = system.nanoseconds;
                IMapNG mapOne = readerOne.readMapFromStream(path, StringReader(contents),
                    warner);
                Integer readEndOne = system.nanoseconds;
                print("Old reader took ``readEndOne - readStartOne``");
                Integer readStartTwo = system.nanoseconds;
                IMapNG mapTwo = readerTwo.readMapFromStream(path, StringReader(contents),
                    warner);
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
