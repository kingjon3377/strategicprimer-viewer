import lovelace.util.common {
    silentListener,
    PathWrapper
}

import strategicprimer.drivers.common {
    UtilityGUI,
    emptyOptions,
    SPOptions
}

import strategicprimer.drivers.gui.common {
    WindowCloseListener,
    SPMenu,
    UtilityMenuHandler
}

"A driver to check every map file in a list for errors and report the results in a
 window."
shared class MapCheckerGUI() satisfies UtilityGUI {
    late MapCheckerFrame window;
    shared actual SPOptions options = emptyOptions;
    variable Boolean initialized = false;

    shared actual void startDriver(String* args) {
        if (!initialized) {
            initialized = true;
            window = MapCheckerFrame(this);
            window.jMenuBar = SPMenu.forWindowContaining(window.contentPane,
                SPMenu.createFileMenu(UtilityMenuHandler(this, window).handleEvent, this),
                SPMenu.disabledMenu(SPMenu.createMapMenu(noop, this)),
                SPMenu.disabledMenu(SPMenu.createViewMenu(noop, this)));
            window.addWindowListener(WindowCloseListener(silentListener(window.dispose)));
        }
        window.showWindow();
        args.coalesced.map(PathWrapper).each(window.check);
    }

    shared actual void open(PathWrapper path) => window.check(path);

}
