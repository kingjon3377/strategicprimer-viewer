import controller.map.drivers {
    UtilityDriver,
    ParamCount,
    DriverUsage,
    IDriverUsage,
    SPOptions
}
import controller.map.misc {
    ICLIHelper
}
import view.map.misc {
    MapCheckerFrame
}
import java.nio.file {
    Paths
}
"A driver to check every map file in a list for errors and report the results in a
 window."
object mapCheckerGUI satisfies UtilityDriver {
    IDriverUsage usageObject = DriverUsage(true, "-k", "--check", ParamCount.atLeastOne,
        "Check map for errors", "Check a map file for errors, deprecated syntax, etc.");
    shared actual IDriverUsage usage() => usageObject;
    shared actual void startDriver(ICLIHelper cli, SPOptions options, String?* args) {
        MapCheckerFrame window = MapCheckerFrame();
        window.setVisible(true);
        for (arg in args.coalesced) {
            window.check(Paths.get(arg));
        }
    }
}