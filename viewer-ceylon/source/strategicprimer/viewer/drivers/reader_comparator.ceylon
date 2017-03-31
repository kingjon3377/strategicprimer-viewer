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

import strategicprimer.viewer.model.map {
    IMapNG
}
import strategicprimer.viewer.xmlio {
    IMapReader,
    Warning,
    warningLevels
}
import strategicprimer.viewer.xmlio.fluidxml {
    SPFluidReader
}
import strategicprimer.viewer.xmlio.yaxml {
    yaXMLReader
}

"A driver for comparing map readers."
object readerComparator satisfies UtilityDriver {
    shared actual IDriverUsage usage = DriverUsage(false, "-t", "--test", ParamCount.atLeastOne,
        "Test map readers",
        "Test map-reading implementations by comparing their results on the same file.");
    "Compare the two readers' performance on the given files."
    shared actual void startDriverOnArguments(ICLIHelper cli, SPOptions options,
            String* args) {
        IMapReader one = SPFluidReader();
        IMapReader two = yaXMLReader;
        Warning warner = warningLevels.ignore;
        for (arg in args.coalesced) {
            cli.println("``arg``:");
            Resource file = parsePath(arg).resource;
            JPath path = JPaths.get(arg);
            if (is File file) {
                String contents = readAll(file);
                Integer startOne = system.nanoseconds;
                IMapNG mapOne = one.readMapFromStream(path, StringReader(contents), warner);
                Integer endOne = system.nanoseconds;
                print("Old method took ``endOne - startOne``");
                Integer startTwo = system.nanoseconds;
                IMapNG mapTwo = two.readMapFromStream(path, StringReader(contents), warner);
                Integer endTwo = system.nanoseconds;
                print("New method took ``endTwo - startTwo``");
                if (mapOne == mapTwo) {
                    print("Readers produce identical results");
                } else {
                    print("Readers differ on ``arg``");
                }
            }
        }
    }
}
String readAll(File file) {
    Reader reader = file.Reader();
    StringBuilder builder = StringBuilder();
    while (exists String line = reader.readLine()) {
        builder.append(line);
        builder.appendNewline();
    }
    return builder.string;
}
