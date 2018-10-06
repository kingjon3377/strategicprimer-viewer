import com.pump.window {
    WindowList
}
import com.apple.eawt {
    QuitResponse,
    Application,
    AppEvent
}
import java.awt.event {
    ActionEvent,
    KeyEvent
}
import strategicprimer.drivers.common {
    ISPDriver,
    ModelDriver,
    WorkerGUI,
    UtilityGUI,
    MultiMapGUIDriver,
    ViewerDriver,
    GUIDriver
}
import javax.swing {
    KeyStroke,
    JMenuItem,
    JMenuBar,
    JMenu
}
import lovelace.util.jvm {
    platform,
    createMenuItem,
    HotKeyModifier,
    createAccelerator
}
import strategicprimer.drivers.gui.common {
    quitHandler
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
                quitHandler.handler = quitResponse.performQuit;
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

        JMenuItem currentPlayerItem;
        if (is WorkerGUI driver) { // TODO: Use enabledForDriver for the treeItems
            currentPlayerItem = createMenuItem("Change current player", KeyEvent.vkP,
                "Look at a different player's units and workers", handler,
                createAccelerator(KeyEvent.vkP));
        } else {
            currentPlayerItem = enabledForDriver<ModelDriver>(createMenuItem(
                "Change current player", KeyEvent.vkP,
                "Mark a player as the current player in the map", handler), driver);
        }
        viewMenu.add(currentPlayerItem);
        viewMenu.add(enabledForDriver<WorkerGUI>(createMenuItem("Reload tree",
            KeyEvent.vkR, "Refresh the view of the workers", handler,
            createAccelerator(KeyEvent.vkR)), driver));
        viewMenu.add(enabledForDriver<WorkerGUI>(createMenuItem("Expand All",
            KeyEvent.vkX, "Expand all nodes in the unit tree", handler), driver));
        viewMenu.add(enabledForDriver<WorkerGUI>(createMenuItem("Expand Unit Kinds",
            KeyEvent.vkK, "Expand all unit kinds to show the units", handler), driver));
        viewMenu.add(enabledForDriver<WorkerGUI>(createMenuItem("Collapse All",
            KeyEvent.vkC, "Collapse all nodes in the unit tree", handler), driver));
        return viewMenu;
    }
    "Disable a menu and return it."
    shared static JMenu disabledMenu(JMenu menu) {
        menu.enabled = false;
        return menu;
    }
    shared new (JMenu* menus) extends JMenuBar() { menus.each(add); }
}
