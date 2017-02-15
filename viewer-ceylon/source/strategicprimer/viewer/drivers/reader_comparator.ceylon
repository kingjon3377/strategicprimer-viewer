import controller.map.drivers {
    IDriverUsage,
    ParamCount,
    DriverUsage,
    SPOptions
}
import controller.map.iointerfaces {
    IMapReader
}
import controller.map.fluidxml {
    SPFluidReader
}
import controller.map.yaxml {
    YAXMLReader
}
import controller.map.misc {
    ICLIHelper
}
import model.map {
    IMapNG
}
import java.io {
    StringReader
}
import ceylon.file {
    parsePath,
    File,
    Reader,
    Resource
}
import util {
    Warning
}
import java.nio.file {
    JPath = Path, JPaths = Paths
}
"A driver for comparing map readers."
object readerComparator satisfies UtilityDriver {
    IDriverUsage usageObject = DriverUsage(false, "-t", "--test", ParamCount.atLeastOne,
        "Test map readers",
        "Test map-reading implementations by comparing their results on the same file.");
    shared actual IDriverUsage usage = usageObject;
    "Compare the two readers' performance on the given files."
    shared actual void startDriverOnArguments(ICLIHelper cli, SPOptions options,
            String* args) {
        IMapReader one = SPFluidReader();
        IMapReader two = YAXMLReader();
        Warning warner = Warning.ignore;
        for (arg in args.coalesced) {
            cli.println("``arg``:");
            Resource file = parsePath(arg).resource;
            JPath path = JPaths.get(arg);
            if (is File file) {
                String contents = readAll(file);
                Integer startOne = system.nanoseconds;
                IMapNG mapOne = one.readMap(path, StringReader(contents), warner);
                Integer endOne = system.nanoseconds;
                print("Old method took ``endOne - startOne``");
                Integer startTwo = system.nanoseconds;
                IMapNG mapTwo = two.readMap(path, StringReader(contents), warner);
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