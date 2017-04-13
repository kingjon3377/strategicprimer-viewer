import ceylon.collection {
    MutableList,
    ArrayList,
    MutableMap,
    HashMap
}
import ceylon.interop.java {
    javaObjectArray
}

import com.apple.eawt {
    Application,
    AppEvent
}
import com.bric.window {
    WindowList
}

import java.awt {
    Component,
    Frame
}
import java.awt.event {
    ActionListener,
    ActionEvent,
    KeyEvent
}

import javax.swing {
    JOptionPane,
    JPopupMenu,
    SwingUtilities,
    JMenuBar,
    JMenu,
    JMenuItem,
    KeyStroke,
    InputMap,
    JComponent
}

import lovelace.util.common {
    todo
}
import lovelace.util.jvm {
    createMenuItem,
    createAccelerator,
    HotKeyModifier,
    platform
}

import strategicprimer.drivers.common {
    IDriverModel,
    IMultiMapModel,
    PlayerChangeListener
}
import strategicprimer.model.map {
    Player
}
import strategicprimer.viewer.drivers.exploration {
    PlayerChangeSource
}
import strategicprimer.viewer.drivers.map_viewer {
    IViewerModel
}
import strategicprimer.drivers.worker.common {
    IWorkerModel
}
"""A class to respond to "change current player" menu items."""
shared class PlayerChangeMenuListener(IDriverModel model)
        satisfies ActionListener&PlayerChangeSource {
    MutableList<PlayerChangeListener> listeners = ArrayList<PlayerChangeListener>();
    shared actual void addPlayerChangeListener(PlayerChangeListener listener) =>
            listeners.add(listener);
    shared actual void removePlayerChangeListener(PlayerChangeListener listener) =>
            listeners.remove(listener);
    Component? eventSource(Anything obj) {
        if (is Component obj) {
            return obj;
        } else {
            return null;
        }
    }
    Frame? getContainingFrame(Component? component) {
        variable Component? temp = component;
        while (exists local = temp) {
            if (is Frame local) {
                return local;
            } else if (is JPopupMenu local) {
                temp = local.invoker;
            }  else {
                temp = local.parent;
            }
        }
        return null;
    }
    shared actual void actionPerformed(ActionEvent event) {
        Player currentPlayer = model.map.currentPlayer;
        {Player*} players = model.map.players;
        if (is Player retval = JOptionPane.showInputDialog(
                getContainingFrame(eventSource(event.source)),
                "Player to view:", "Choose New Player:",
                JOptionPane.plainMessage, null, javaObjectArray(Array<Player?>(players)),
                currentPlayer)) {
            for (listener in listeners) {
                listener.playerChanged(currentPlayer, retval);
            }
        }
    }
}
"A class to match menu item selections to the listeners to handle them. Note that at
 most one listener will be notified of any given action-command; subsequent registrations
 override previous ones."
shared class MenuBroker() satisfies ActionListener {
    """The mapping from "actions" to listeners to handle them."""
    MutableMap<String, Anything(ActionEvent)> mapping = HashMap<String, Anything(ActionEvent)>();
    "Rgister a listener for a series of action commands."
    shared void register(ActionListener|Anything(ActionEvent) listener, String* actions) {
        Anything(ActionEvent) actual;
        if (is ActionListener listener) {
            actual = listener.actionPerformed;
        } else {
            actual = listener;
        }
        for (action in actions) {
            mapping.put(action.lowercased, actual);
        }
    }
    "Handle an event by passing it to the listener that's registered to handle its action
     command. If none is registered, log a warning."
    shared actual void actionPerformed(ActionEvent event) {
        String action = event.actionCommand;
        if (exists listener = mapping.get(action.lowercased)) {
            SwingUtilities.invokeLater(() => listener(event));
        } else {
            log.warn("Unhandled action: ``action``");
        }
    }
}
"A class to hold the logic for building our menus."
todo("Make the methods static once MenuItemCreator has been ported.",
    "Redesign so users just have to say which menus they want enabled instead of
     instantiatng them one by one")
shared class SPMenu() extends JMenuBar() {
    "Create the file menu."
    shared JMenu createFileMenu(/*ActionListener|*/Anything(ActionEvent) handler,
            IDriverModel model) {
        JMenu fileMenu = JMenu("File");
        fileMenu.mnemonic = KeyEvent.vkF;
        JMenuItem newItem = createMenuItem("New", KeyEvent.vkN,
            createAccelerator(KeyEvent.vkN),
            "Create a new, empty map the same size as the current one", handler);
        fileMenu.add(newItem);
        if (!model is IViewerModel) {
            newItem.enabled = false;
        }
        String desc = (model is IMultiMapModel) then "the main" else "the";
        String loadCaption = "Load ``(model is IMultiMapModel) then "the main" else "a"
            `` map from file";
        String saveCaption = "Save ``desc`` map to the file it was loaded from";
        String saveAsCaption = "Save ``desc`` map to file";
        fileMenu.add(createMenuItem("Load", KeyEvent.vkL, createAccelerator(KeyEvent.vkO),
            loadCaption, handler));
        JMenuItem loadSecondaryItem = createMenuItem("Load secondary", KeyEvent.vkE,
            createAccelerator(KeyEvent.vkO, HotKeyModifier.shift),
            "Load an additional secondary map from file", handler);
        fileMenu.add(loadSecondaryItem);
        fileMenu.add(createMenuItem("Save", KeyEvent.vkS, createAccelerator(KeyEvent.vkS),
            saveCaption, handler));
        fileMenu.add(createMenuItem("Save As", KeyEvent.vkA,
            createAccelerator(KeyEvent.vkS, HotKeyModifier.shift), saveAsCaption,
            handler));
        JMenuItem saveAllItem = createMenuItem("Save All", KeyEvent.vkV,
            createAccelerator(KeyEvent.vkL), "Save all maps to their files", handler);
        fileMenu.add(saveAllItem);
        if (!model is IMultiMapModel) {
            loadSecondaryItem.enabled = false;
            saveAllItem.enabled = false;
        }
        fileMenu.addSeparator();
        JMenuItem openViewerItem = createMenuItem("Open in map viwer", KeyEvent.vkM,
            createAccelerator(KeyEvent.vkM),
            "Open the main map in the map viewer for a broader view", handler);
        fileMenu.add(openViewerItem);
        if (model is IViewerModel) {
            openViewerItem.enabled = false;
        }
        JMenuItem openSecondaryViewerItem = createMenuItem(
            "Open secondary map in map viewer", KeyEvent.vkE,
            createAccelerator(KeyEvent.vkE),
            "Open the first secondary map in the map vieer for a broader view", handler);
        fileMenu.add(openSecondaryViewerItem);
        if (model is IViewerModel || !model is IMultiMapModel) {
            openSecondaryViewerItem.enabled = false;
        }
        fileMenu.addSeparator();
        if (platform.systemIsMac) {
            Application.application.setAboutHandler((AppEvent.AboutEvent event) {
                Object source = WindowList.getWindows(true, false).iterable.coalesced
                    .sequence().reversed.first else event;
                handler(ActionEvent(source, ActionEvent.actionFirst,
                    "About"));
            });
        } else {
            fileMenu.add(createMenuItem("About", KeyEvent.vkB,
                createAccelerator(KeyEvent.vkB), "Show development credits", handler));
            fileMenu.addSeparator();
            fileMenu.add(createMenuItem("Quit", KeyEvent.vkQ,
                createAccelerator(KeyEvent.vkQ), "Quit the application", handler));
        }
        return fileMenu;
    }
    """Create the "map" menu, including go-to-tile, find, and zooming functions."""
    shared JMenu createMapMenu(Anything(ActionEvent) handler, IDriverModel model) {
        JMenu retval = JMenu("Map");
        retval.mnemonic = KeyEvent.vkM;
        Integer findKey = KeyEvent.vkF;
        KeyStroke findStroke = createAccelerator(findKey);
        KeyStroke nextStroke = createAccelerator(KeyEvent.vkG);
        JMenuItem gotoTileItem = createMenuItem("Go to tile", KeyEvent.vkT,
            createAccelerator(KeyEvent.vkT), "Go to a tile by coordinates", handler);
        JMenuItem findItem = createMenuItem("Find a fixture", findKey, findStroke,
            "Find a fixture by name, kind or ID #", handler);
        Integer nextKey = KeyEvent.vkN;
        JMenuItem nextItem = createMenuItem("Find next", nextKey, nextStroke,
            "Find the next fixture matching the pattern", handler);
        if (!model is IViewerModel) {
            gotoTileItem.enabled = false;
            findItem.enabled = false;
            nextItem.enabled = false;
        }
        retval.add(gotoTileItem);
        InputMap findInput = findItem.getInputMap(JComponent.whenInFocusedWindow);
        findInput.put(KeyStroke.getKeyStroke(KeyEvent.vkSlash, 0),
            findInput.get(findStroke));
        retval.add(findItem);
        InputMap nextInput = nextItem.getInputMap(JComponent.whenInFocusedWindow);
        nextInput.put(KeyStroke.getKeyStroke(nextKey, 0), nextInput.get(nextStroke));
        retval.add(nextItem);
        retval.addSeparator();
        // vkPlus only works on non-US keyboards, but we leave it as the primary hot-key
        // because it's the best to *show* in the menu.
        KeyStroke plusKey = createAccelerator(KeyEvent.vkPlus);
        JMenuItem zoomInItem = createMenuItem("Zoom in", KeyEvent.vkI, plusKey,
            "Increase the visible size of each tile", handler);
        InputMap zoomInInputMap = zoomInItem.getInputMap(JComponent.whenInFocusedWindow);
        zoomInInputMap.put(createAccelerator(KeyEvent.vkEquals), inputMap.get(plusKey));
        zoomInInputMap.put(createAccelerator(KeyEvent.vkEquals, HotKeyModifier.shift),
            inputMap.get(plusKey));
        zoomInInputMap.put(createAccelerator(KeyEvent.vkAdd), inputMap.get(plusKey));
        retval.add(zoomInItem);
        retval.add(createMenuItem("Zoom out", KeyEvent.vkO,
            createAccelerator(KeyEvent.vkMinus), "Decrease the visible size of each tile",
            handler));
        retval.add(createMenuItem("Reset zoom", KeyEvent.vkR,
            createAccelerator(KeyEvent.vk0), "Reset the zoom level", handler));
        retval.addSeparator();
        retval.add(createMenuItem("Center", KeyEvent.vkC, createAccelerator(KeyEvent.vkC),
            "Center the view on the selected tile", handler));
        return retval;
    }
    """Create the "view" menu."""
    shared JMenu createViewMenu(Anything(ActionEvent) handler, IDriverModel model) {
        JMenu viewMenu = JMenu("View");
        viewMenu.mnemonic = KeyEvent.vkE;

        // We *create* these items here (early) so that we can enable or disable them
        // without an extra branch.
        // TODO: create Ceylon Iterable instead of add()ing to a List
        {JMenuItem*} treeItems = {
            createMenuItem("Reload tree", KeyEvent.vkR, createAccelerator(KeyEvent.vkR),
                "Refresh the view of the workers", handler),
            createMenuItem("Expand All", KeyEvent.vkX, null,
                "Expand all nodes in the unit tree", handler),
            createMenuItem("Expand Unit Kinds", KeyEvent.vkK, null,
                "Expand all unit kinds to show the units", handler),
            createMenuItem("Collapse All", KeyEvent.vkC, null,
                "Collapse all nodes in the unit tree", handler)
        };
        JMenuItem currentPlayerItem;
        if (is IWorkerModel model) {
            currentPlayerItem = createMenuItem("Change current player", KeyEvent.vkP,
                createAccelerator(KeyEvent.vkP),
                "Look at a different player's units and workers", handler);
        } else {
            currentPlayerItem = createMenuItem("Change current player", KeyEvent.vkP,
                null, "Mark a player as the current player in the map", handler);
            for (item in treeItems) {
                item.enabled = false;
            }
        }
        viewMenu.add(currentPlayerItem);
        for (item in treeItems) {
            viewMenu.add(item);
        }
        return viewMenu;
    }
    "Add a menu, but set it to disabled."
    shared JMenu addDisabled(JMenu menu) {
        add(menu);
        menu.enabled = false;
        return menu;
    }
}
