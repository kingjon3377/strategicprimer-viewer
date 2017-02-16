import controller.map.drivers {
    DriverFailedException
}
import controller.map.misc {
    ICLIHelper
}

import java.lang {
    Appendable,
    CharSequence
}
import java.nio.file {
    JPath=Path, JPaths = Paths
}
import java.util {
    Formatter
}

import model.misc {
    IDriverModel,
    IMultiMapModel,
    SimpleMultiMapModel
}
import lovelace.util.common {
    todo
}
import view.map.misc {
    SubsetFrame
}
import view.util {
    ErrorShower
}
import javax.swing {
    SwingUtilities
}
import java.io {
    IOException
}
import javax.xml.stream {
    XMLStreamException
}
import controller.map.formatexceptions {
    SPFormatException
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
    shared actual IDriverUsage usage = DriverUsage(false, "-s", "--subset", ParamCount.atLeastTwo,
        "Check players' maps against master",
        "Check that subordinate maps are subsets of the main map, containing nothing that
         it does not contain in the same place.");
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
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
            startDriverOnModel(cli, options, SimpleMultiMapModel(model));
        }
    }
}
"A driver to check whether player maps are subsets of the main map and display the
 results graphically."
todo("Unify with subsetCLI")
object subsetGUI satisfies ISPDriver {
    shared actual IDriverUsage usage = DriverUsage(true, "-s", "--subset", ParamCount.atLeastTwo,
        "Check players' maps against master",
        "Check that subordinate maps are subsets of the main map, containing nothing that
         it does not contain in the same place.");
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IMultiMapModel model) {
            SubsetFrame frame = SubsetFrame();
            SwingUtilities.invokeLater(() => frame.setVisible(true));
            frame.loadMain(model.map);
            for (pair in model.subordinateMaps) {
                frame.test(pair.first(), pair.second());
            }
        } else {
            ErrorShower.showErrorDialog(null,
                "The subset driver doesn't make sense on a non-multi-map driver");
        }
    }
    shared actual void startDriverOnArguments(ICLIHelper cli, SPOptions options,
            String* args) {
        if (args.size < 2) {
            throw IncorrectUsageException(usage);
        }
        SubsetFrame frame = SubsetFrame();
        SwingUtilities.invokeLater(() => frame.setVisible(true));
        assert (exists first = args.first);
        try {
            frame.loadMain(JPaths.get(first));
        } catch (IOException except) {
            throw DriverFailedException("I/O error loading main map ``first``", except);
        } catch (XMLStreamException except) {
            throw DriverFailedException("Malformed XML in main map ``first``", except);
        } catch (SPFormatException except) {
            throw DriverFailedException("Invalid SP XML in main  map ``first``", except);
        }
        for (arg in args.rest) {
            frame.test(JPaths.get(arg));
        }
    }
}