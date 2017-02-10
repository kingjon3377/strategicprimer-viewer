import controller.map.drivers {
    UtilityDriver,
    ParamCount,
    DriverUsage,
    IDriverUsage,
    SPOptions,
    MapChecker
}
import controller.map.misc {
    ICLIHelper
}
import java.nio.file {
    JPaths=Paths,
    JPath=Path
}
import java.awt {
    Dimension,
    Color
}
import view.util {
    SPFrame,
    StreamingLabel
}
import java.util {
    Optional
}
import util {
    Warning
}
import javax.swing {
    JScrollPane
}
class MapCheckerFrame() extends SPFrame("Strategic Primer Map Checker",
        Optional.empty<JPath>(), Dimension(640, 320)) {
    shared actual String windowName = "Map Checker";
    StreamingLabel label = StreamingLabel();
    void printParagraph(String paragraph,
            StreamingLabel.LabelTextColor color = StreamingLabel.LabelTextColor.white) {
        try (writer = label.writer) {
            writer.println("<p style=\"color:``color``\">``paragraph``</p>");
        }
        label.repaint();
    }
    Warning.custom.setCustomPrinter(Warning.wrapHandler(
        (string) => printParagraph(string, StreamingLabel.LabelTextColor.yellow)));
    setBackground(Color.black);
    contentPane = JScrollPane(label);
    contentPane.background = Color.black;
    MapChecker checker = MapChecker();
    shared void check(JPath filename) {
        checker.check(filename, (text) {
            if (text.startsWith("No errors")) {
                printParagraph(text, StreamingLabel.LabelTextColor.green);
            } else {
                printParagraph(text);
            }
        }, (text) => printParagraph(text, StreamingLabel.LabelTextColor.red));
    }
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
            window.check(JPaths.get(arg));
        }
    }
}