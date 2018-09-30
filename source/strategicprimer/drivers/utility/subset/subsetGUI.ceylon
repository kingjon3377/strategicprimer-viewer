import java.io {
    IOException
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

import strategicprimer.model.common.xmlio {
    SPFormatException
}
import strategicprimer.drivers.common {
    DriverFailedException,
    DriverUsage,
    ParamCount,
    IDriverUsage,
    ISPDriver,
    SPOptions,
    IncorrectUsageException,
    UtilityDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import ceylon.file {
    parsePath
}
"A driver to check whether player maps are subsets of the main map and display the
 results graphically."
todo("Unify with [[SubsetCLI]]")
service(`interface ISPDriver`)
shared class SubsetGUI() satisfies UtilityDriver {
    shared actual IDriverUsage usage = DriverUsage(true, ["-s", "--subset"],
        ParamCount.atLeastTwo, "Check players' maps against master",
        "Check that subordinate maps are subsets of the main map, containing nothing that
         it does not contain in the same place.", false, true);
    shared actual void startDriverOnArguments(ICLIHelper cli, SPOptions options,
            String* args) {
        if (args.size < 2) {
            throw IncorrectUsageException(usage);
        }
        SubsetFrame frame = subsetFrame();
        SwingUtilities.invokeLater(frame.showWindow);
        assert (exists first = args.first);
        try { // Errors are reported via the GUI in loadMain(), then rethrown.
            frame.loadMain(parsePath(first));
        } catch (IOException except) {
            throw DriverFailedException(except, "I/O error loading main map ``first``");
        } catch (XMLStreamException except) {
            throw DriverFailedException(except, "Malformed XML in main map ``first``");
        } catch (SPFormatException except) {
            throw DriverFailedException(except, "Invalid SP XML in main  map ``first``");
        }
        args.rest.map(parsePath).each(frame.testFile);
    }
}
