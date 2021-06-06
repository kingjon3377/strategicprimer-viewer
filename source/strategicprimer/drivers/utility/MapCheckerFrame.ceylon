import java.awt {
    Dimension,
    Color
}

import javax.swing {
    JScrollPane
}

import lovelace.util.jvm {
    StreamingLabel,
    LabelTextColor
}

import lovelace.util.common {
    PathWrapper
}

import strategicprimer.drivers.common {
    ISPDriver
}

import strategicprimer.model.common.xmlio {
    warningLevels
}

import strategicprimer.drivers.gui.common {
    SPFrame
}

"The map-checker GUI window." // TODO: Merge into MapCheckerGUI
class MapCheckerFrame(ISPDriver driver) extends SPFrame("Strategic Primer Map Checker",
        driver, Dimension(640, 320), true, noop, "Map Checker") {
    StreamingLabel label = StreamingLabel();
    void printParagraph(String paragraph,
            LabelTextColor color = LabelTextColor.black) {
        label.append("<p style=\"color:``color``\">``paragraph``</p>");
    }

    void customPrinter(String string) =>
            printParagraph(string, LabelTextColor.yellow);
    setBackground(Color.white);
    contentPane = JScrollPane(label);
    contentPane.background = Color.white;

    void outHandler(String text) {
        if (text.startsWith("No errors")) {
            printParagraph(text, LabelTextColor.green);
        } else {
            printParagraph(text);
        }
    }

    void errHandler(String text) =>
            printParagraph(text, LabelTextColor.red);

    MapCheckerCLI mapCheckerCLI = MapCheckerCLI(outHandler, errHandler);

    shared void check(PathWrapper filename) =>
            mapCheckerCLI.check(filename, warningLevels.custom(customPrinter));

    shared actual void acceptDroppedFile(PathWrapper file) => check(file);
}
