import controller.map.drivers {
    SimpleDriver,
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SPOptions
}
import controller.map.misc {
    ICLIHelper
}

import java.lang {
    Appendable,
    CharSequence
}
import java.nio.file {
    JPath=Path
}
import java.util {
    Formatter
}

import model.misc {
    IDriverModel,
    IMultiMapModel,
    SimpleMultiMapModel
}
class AppendableHelper(ICLIHelper wrapped) satisfies Appendable {
    shared actual Appendable append(CharSequence csq) {
        wrapped.print(csq.string);
        return this;
    }
    shared actual Appendable append(CharSequence csq, Integer start, Integer end) =>
            append(csq.subSequence(start, end));
    shared actual Appendable append(Character c) => append(c.string);
}
"A driver to check whether player maps are subsets of the main map."
object subsetCLI satisfies SimpleDriver {
    IDriverUsage usageObject = DriverUsage(false, "-s", "--subset", ParamCount.atLeastTwo,
        "Check players' maps against master",
        "Check that subordinate maps are subsets of the main map, containing nothing that
         it does not contain in the same place.");
    shared actual IDriverUsage usage() => usageObject;
    shared actual void startDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
        if (is IMultiMapModel model) {
            for (pair in model.subordinateMaps) {
                String filename = pair.second().map(JPath.string)
                    .orElse("map without a filename");
                cli.print("``filename``\t...\t\t");
                if (model.map.isSubset(pair.first(), Formatter(AppendableHelper(cli)),
                        "In ``filename``:")) {
                    cli.println("OK");
                } else {
                    cli.println("WARN");
                }
            }
        } else {
            log.warn("Subset checking does nothing with no subordinate maps");
            startDriver(cli, options, SimpleMultiMapModel(model));
        }
    }
}