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
    todo,
    PathWrapper,
    silentListener
}

import strategicprimer.model.common.xmlio {
    SPFormatException
}
import strategicprimer.drivers.common {
    DriverFailedException,
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions,
    IncorrectUsageException,
    UtilityDriver,
    DriverFactory,
    UtilityDriverFactory,
    UtilityGUI
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.gui.common {
    WindowCloseListener,
    UtilityMenuHandler,
    SPMenu
}

"A factory for a driver to check whether player maps are subsets of the main map and
 display the results graphically."
service(`interface DriverFactory`)
shared class SubsetGUIFactory satisfies UtilityDriverFactory {
    shared static IDriverUsage staticUsage = DriverUsage(true, ["-s", "--subset"],
        ParamCount.atLeastOne, "Check players' maps against master",
        "Check that subordinate maps are subsets of the main map, containing nothing that
         it does not contain in the same place.", false, true);

    shared actual IDriverUsage usage => staticUsage;

    shared new () {}

    shared actual UtilityDriver createDriver(ICLIHelper cli, SPOptions options) =>
            SubsetGUI(cli, options);
}

"A driver to check whether player maps are subsets of the main map and display the
 results graphically."
todo("Unify with [[SubsetCLI]], like the map-checker GUI")
shared class SubsetGUI(ICLIHelper cli, SPOptions options) satisfies UtilityGUI {
    late SubsetFrame frame;

    variable Boolean initialized = false;

    shared actual void startDriver(String* args) {
        if (args.empty) {
            throw IncorrectUsageException(SubsetGUIFactory.staticUsage);
        }
        if (!initialized) {
            initialized = true;
            frame = SubsetFrame(this);
            frame.jMenuBar = SPMenu.forWindowContaining(frame.contentPane,
                SPMenu.createFileMenu(UtilityMenuHandler(this, frame).handleEvent, this),
                SPMenu.disabledMenu(SPMenu.createMapMenu(noop, this)),
                SPMenu.disabledMenu(SPMenu.createViewMenu(noop, this)));
            frame.addWindowListener(WindowCloseListener(silentListener(frame.dispose)));
        }
        SwingUtilities.invokeLater(frame.showWindow);
        assert (exists first = args.first);
        try { // Errors are reported via the GUI in loadMain(), then rethrown.
            frame.loadMain(PathWrapper(first));
        } catch (IOException except) {
            throw DriverFailedException(except, "I/O error loading main map ``first``");
        } catch (XMLStreamException except) {
            throw DriverFailedException(except, "Malformed XML in main map ``first``");
        } catch (SPFormatException except) {
            throw DriverFailedException(except, "Invalid SP XML in main  map ``first``");
        }
        args.rest.map(PathWrapper).each(frame.testFile);
    }

    shared actual void open(PathWrapper path) => frame.testFile(path);
}
