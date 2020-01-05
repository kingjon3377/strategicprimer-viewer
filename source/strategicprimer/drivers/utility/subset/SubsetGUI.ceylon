import java.io {
    IOException
}

import javax.swing {
    SwingUtilities
}

import lovelace.util.common {
    todo,
    PathWrapper,
    silentListener,
    MalformedXMLException
}

import strategicprimer.model.common.xmlio {
    SPFormatException
}

import strategicprimer.drivers.common {
    DriverFailedException,
    SPOptions,
    IncorrectUsageException,
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
        } catch (MalformedXMLException except) {
            throw DriverFailedException(except, "Malformed XML in main map ``first``");
        } catch (SPFormatException except) {
            throw DriverFailedException(except, "Invalid SP XML in main  map ``first``");
        }
        args.rest.map(PathWrapper).each(frame.testFile);
    }

    shared actual void open(PathWrapper path) => frame.testFile(path);
}
