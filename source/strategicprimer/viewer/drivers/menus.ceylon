import ceylon.collection {
    MutableMap,
    HashMap
}
import com.apple.eawt {
    Application,
    AppEvent,
    QuitResponse
}
import com.pump.window {
    WindowList
}

import java.awt {
    Window
}
import java.awt.event {
    ActionListener,
    ActionEvent,
    KeyEvent
}

import javax.swing {
    SwingUtilities,
    JMenuBar,
    JMenu,
    JMenuItem,
    KeyStroke
}

import lovelace.util.jvm {
    createMenuItem,
    createAccelerator,
    HotKeyModifier,
    platform
}

import strategicprimer.drivers.common {
    ISPDriver,
    ModelDriver,
    MultiMapGUIDriver,
    UtilityGUI,
    GUIDriver,
    ViewerDriver
}
import strategicprimer.drivers.worker.common {
    IWorkerModel
}
import lovelace.util.common {
    defer
}
"A class to match menu item selections to the listeners to handle them. Note that at
 most one listener will be notified of any given action-command; subsequent registrations
 override previous ones."
shared class MenuBroker() satisfies ActionListener {
    """The mapping from "actions" to listeners to handle them."""
    MutableMap<String, Anything(ActionEvent)> mapping =
            HashMap<String, Anything(ActionEvent)>();
    "Rgister a listener for a series of action commands."
    shared void register(ActionListener|Anything(ActionEvent) listener, String* actions) {
        Anything(ActionEvent) actual;
        if (is ActionListener listener) {
            actual = listener.actionPerformed;
        } else {
            actual = listener;
        }
        for (action in actions) {
            mapping[action.lowercased] = actual;
        }
    }
    "Register a listener for an action command that shows the given window."
    shared void registerWindowShower(Window()|Window window, String* actions) {
        if (is Window window) {
            register((event) => window.setVisible(true), *actions);
        } else {
            register((event) => window().setVisible(true), *actions);
        }
    }
    "Handle an event by passing it to the listener that's registered to handle its action
     command. If none is registered, log a warning."
    shared actual void actionPerformed(ActionEvent event) {
        String action = event.actionCommand;
        if (exists listener = mapping[action.lowercased]) {
            SwingUtilities.invokeLater(defer(listener, [event]));
        } else {
            log.warn("Unhandled action: ``action``");
        }
    }
}
"A class to hold the logic for building our menus."
shared class SPMenu extends JMenuBar {
//    suppressWarnings("expressionTypeNothing")
//    static void simpleQuit() => process.exit(0); // TODO: uncomment these once eclipse/ceylon#7396 fixed
//    static variable Anything() localDefaultQuit = simpleQuit;
//    shared static Anything() defaultQuit => localDefaultQuit;
    static JMenuItem enabledForDriver<Driver>(JMenuItem item, ISPDriver driver)
            given Driver satisfies ISPDriver {
        if (!driver is Driver) {
            item.enabled = false;
        }
        return item;
    }
    static JMenuItem disabledForDriver<Driver>(JMenuItem item, ISPDriver driver)
            given Driver satisfies ISPDriver {
        if (driver is Driver) {
            item.enabled = false;
        }
        return item;
    }
    "Create the file menu."
    shared static JMenu createFileMenu(/*ActionListener|*/Anything(ActionEvent) handler,
            ISPDriver driver) {
        JMenu fileMenu = JMenu("File");
        fileMenu.mnemonic = KeyEvent.vkF;
        fileMenu.add(enabledForDriver<ViewerDriver>(createMenuItem("New", KeyEvent.vkN,
            "Create a new, empty map the same size as the current one", handler,
            createAccelerator(KeyEvent.vkN)), driver));
        String desc;
        String loadCaption;
        String saveCaption;
        String saveAsCaption;
        if (is MultiMapGUIDriver driver) {
            desc = "the main";
            loadCaption = "Load the main map from file";
            saveCaption = "Save the main map to the file it was loaded from";
            saveAsCaption = "Save the main map to file";
        } else {
            desc = "the";
            loadCaption = "Load a map from file";
            saveCaption = "Save the map to the file it was loaded from";
            saveAsCaption = "Save the map to file";
        }
        fileMenu.add(enabledForDriver<GUIDriver|UtilityGUI>(createMenuItem("Load",
                KeyEvent.vkL, loadCaption, handler, createAccelerator(KeyEvent.vkO)),
            driver));
        fileMenu.add(enabledForDriver<MultiMapGUIDriver>(createMenuItem("Load secondary",
            KeyEvent.vkE, "Load an additional secondary map from file", handler,
            createAccelerator(KeyEvent.vkO, HotKeyModifier.shift)), driver));
        fileMenu.add(enabledForDriver<ModelDriver>(createMenuItem("Save",
            KeyEvent.vkS, saveCaption, handler, createAccelerator(KeyEvent.vkS)), driver));
        fileMenu.add(enabledForDriver<ModelDriver>(createMenuItem("Save As",
            KeyEvent.vkA, saveAsCaption, handler, createAccelerator(KeyEvent.vkS,
                HotKeyModifier.shift)), driver));
        fileMenu.add(enabledForDriver<MultiMapGUIDriver>(createMenuItem("Save All",
                KeyEvent.vkV, "Save all maps to their files", handler,
            createAccelerator(KeyEvent.vkL)), driver));
        fileMenu.addSeparator();
        KeyStroke openViewerHotkey;
        if (platform.systemIsMac) {
            openViewerHotkey = KeyStroke.getKeyStroke(KeyEvent.vkM,
                KeyEvent.altDownMask);
        } else {
            openViewerHotkey = createAccelerator(KeyEvent.vkM);
        }
        fileMenu.add(disabledForDriver<ViewerDriver>(enabledForDriver<ModelDriver>(
            createMenuItem("Open in map viewer", KeyEvent.vkM,
                "Open the main map in the map viewer for a broader view", handler,
                openViewerHotkey), driver), driver));
        fileMenu.add(enabledForDriver<MultiMapGUIDriver>(createMenuItem(
            "Open secondary map in map viewer", KeyEvent.vkE,
            "Open the first secondary map in the map vieer for a broader view", handler,
            createAccelerator(KeyEvent.vkE)), driver));
        fileMenu.addSeparator();
        fileMenu.add(createMenuItem("Close", KeyEvent.vkW, "Close this window",
            handler, createAccelerator(KeyEvent.vkW)));
        if (platform.systemIsMac) {
            Application.application.setAboutHandler((AppEvent.AboutEvent event) =>
                handler(ActionEvent(WindowList.getWindows(true, false).iterable.coalesced
                    .last else event, ActionEvent.actionFirst,
                    "About")));
            Application.application.setQuitHandler((AppEvent.QuitEvent event,
                    QuitResponse quitResponse) {
                IOHandler.quitHandler = quitResponse.performQuit;
//                localDefaultQuit = quitResponse.performQuit; // TODO: switch to this once eclipse/ceylon#7396 fixed
                handler(ActionEvent(
                    WindowList.getWindows(true, false).iterable.coalesced.last else event,
                    ActionEvent.actionFirst, "Quit"));
            });
        } else {
            fileMenu.add(createMenuItem("About", KeyEvent.vkB, "Show development credits",
                handler, createAccelerator(KeyEvent.vkB)));
            fileMenu.addSeparator();
            fileMenu.add(createMenuItem("Quit", KeyEvent.vkQ, "Quit the application",
                handler, createAccelerator(KeyEvent.vkQ)));
        }
        return fileMenu;
    }
    """Create the "map" menu, including go-to-tile, find, and zooming functions."""
    shared static JMenu createMapMenu(Anything(ActionEvent) handler, ISPDriver driver) {
        JMenu retval = JMenu("Map");
        retval.mnemonic = KeyEvent.vkM;
        retval.add(enabledForDriver<ViewerDriver>(createMenuItem("Go to tile",
            KeyEvent.vkT, "Go to a tile by coordinates", handler,
            createAccelerator(KeyEvent.vkT)), driver));
        Integer findKey = KeyEvent.vkF;
        retval.add(enabledForDriver<ViewerDriver>(createMenuItem("Find a fixture", findKey,
            "Find a fixture by name, kind or ID #", handler, createAccelerator(findKey),
            KeyStroke.getKeyStroke(KeyEvent.vkSlash, 0)), driver));
        Integer nextKey = KeyEvent.vkN;
        retval.add(enabledForDriver<ViewerDriver>(createMenuItem("Find next", nextKey,
                "Find the next fixture matching the pattern", handler,
                createAccelerator(KeyEvent.vkG), KeyStroke.getKeyStroke(nextKey, 0)),
            driver));
        retval.addSeparator();
        // vkPlus only works on non-US keyboards, but we leave it as the primary hot-key
        // because it's the best to *show* in the menu.
        KeyStroke plusKey = createAccelerator(KeyEvent.vkPlus);
        retval.add(enabledForDriver<ViewerDriver>(createMenuItem("Zoom in", KeyEvent.vkI,
            "Increase the visible size of each tile", handler, plusKey,
            createAccelerator(KeyEvent.vkEquals),
            createAccelerator(KeyEvent.vkEquals, HotKeyModifier.shift),
            createAccelerator(KeyEvent.vkAdd)), driver));
        retval.add(enabledForDriver<ViewerDriver>(createMenuItem("Zoom out", KeyEvent.vkO,
            "Decrease the visible size of each tile", handler,
            createAccelerator(KeyEvent.vkMinus)), driver));
        retval.add(enabledForDriver<ViewerDriver>(createMenuItem("Reset zoom", KeyEvent.vkR,
            "Reset the zoom level", handler, createAccelerator(KeyEvent.vk0)), driver));
        retval.addSeparator();
        KeyStroke centerHotkey;
        if (platform.systemIsMac) {
            centerHotkey = createAccelerator(KeyEvent.vkL);
        } else {
            centerHotkey = createAccelerator(KeyEvent.vkC);
        }
        retval.add(enabledForDriver<ViewerDriver>(createMenuItem("Center", KeyEvent.vkC,
            "Center the view on the selected tile", handler,
            centerHotkey), driver));
        return retval;
    }
    """Create the "view" menu."""
    shared static JMenu createViewMenu(Anything(ActionEvent) handler,
            ISPDriver driver) {
        JMenu viewMenu = JMenu("View");
        viewMenu.mnemonic = KeyEvent.vkE;

        // We *create* these items here (early) so that we can enable or disable them
        // without an extra branch.
        {JMenuItem*} treeItems = [
            createMenuItem("Reload tree", KeyEvent.vkR, "Refresh the view of the workers",
                handler, createAccelerator(KeyEvent.vkR)),
            createMenuItem("Expand All", KeyEvent.vkX,
                "Expand all nodes in the unit tree", handler),
            createMenuItem("Expand Unit Kinds", KeyEvent.vkK,
                "Expand all unit kinds to show the units", handler),
            createMenuItem("Collapse All", KeyEvent.vkC,
                "Collapse all nodes in the unit tree", handler)
        ];
        JMenuItem currentPlayerItem;
        if (is ModelDriver driver, is IWorkerModel model = driver.model) { // TODO: Driver interface for this distinction; use enabledForDriver for the treeItems once it's there
            currentPlayerItem = createMenuItem("Change current player", KeyEvent.vkP,
                "Look at a different player's units and workers", handler,
                createAccelerator(KeyEvent.vkP));
        } else {
            currentPlayerItem = enabledForDriver<ModelDriver>(createMenuItem(
                "Change current player", KeyEvent.vkP,
                "Mark a player as the current player in the map", handler), driver);
            for (item in treeItems) {
                item.enabled = false;
            }
        }
        viewMenu.add(currentPlayerItem);
        // can't use Iterable.each() instead of a loop because JMenu.add() is overloaded
        for (item in treeItems) {
            viewMenu.add(item);
        }
        return viewMenu;
    }
    "Disable a menu and return it."
    shared static JMenu disabledMenu(JMenu menu) {
        menu.enabled = false;
        return menu;
    }
    shared new (JMenu* menus) extends JMenuBar() { menus.each(add); }
}
