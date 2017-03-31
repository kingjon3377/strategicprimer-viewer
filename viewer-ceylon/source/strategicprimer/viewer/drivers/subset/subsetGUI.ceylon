import strategicprimer.viewer.xmlio {
    SPFormatException
}
import strategicprimer.viewer.model {
    IMultiMapModel,
    IDriverModel
}
import java.io {
    IOException
}
import java.nio.file {
    JPaths=Paths
}

import javax.swing {
    SwingUtilities
}
import javax.xml.stream {
    XMLStreamException
}

import lovelace.util.common {
    todo
}
import lovelace.util.jvm {
    showErrorDialog
}


import strategicprimer.viewer.drivers {
    DriverFailedException,
    ICLIHelper,
    DriverUsage,
    ParamCount,
    IDriverUsage,
    ISPDriver,
    SPOptions,
    IncorrectUsageException
}
"A driver to check whether player maps are subsets of the main map and display the
 results graphically."
todo("Unify with subsetCLI")
shared object subsetGUI satisfies ISPDriver {
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
            for ([map, file] in model.subordinateMaps) {
                frame.testMap(map, file);
            }
        } else {
            showErrorDialog(null, "Strategic Primer Assistive Programs",
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
            throw DriverFailedException(except, "I/O error loading main map ``first``");
        } catch (XMLStreamException except) {
            throw DriverFailedException(except, "Malformed XML in main map ``first``");
        } catch (SPFormatException except) {
            throw DriverFailedException(except, "Invalid SP XML in main  map ``first``");
        }
        for (arg in args.rest) {
            frame.testFile(JPaths.get(arg));
        }
    }
}