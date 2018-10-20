import strategicprimer.drivers.common {
    UtilityGUI
}
import java.awt.event {
    ActionEvent
}
import lovelace.util.jvm {
    platform
}
import com.apple.eawt {
    Application,
    AppEvent
}
import com.pump.window {
    WindowList
}
import strategicprimer.drivers.gui.common.about {
    aboutDialog
}

"""A class to handle menu items for utility apps that only have "Open",
   "Close", "About", and "Quit" menu items enabled."""
shared class UtilityMenuHandler(UtilityGUI driver, SPFrame window) {
    "Show the About dialog (as a response to a menu-item event)."
    void aboutHandler(ActionEvent event) => // TODO: Make these static methods? // TODO: Drop 'event' parameter; it's unused, and macAboutHandler() has to go to some length to create it
            aboutDialog(window, window.windowName).setVisible(true);
    "Show the About dialog (as a response to the About item in the Mac app-menu
     being chosen)."
    void macAboutHandler(AppEvent.AboutEvent event) {
        Object source =
                WindowList.getWindows(true, false).iterable.coalesced.last else event;
        aboutHandler(ActionEvent(source, ActionEvent.actionFirst,
            "About"));
    }
    if (platform.systemIsMac) {
        Application.application.setAboutHandler(macAboutHandler);
    }

    "Handle the user's chosen menu item."
    shared void handleEvent(ActionEvent event) {
        switch (command = event.actionCommand.lowercased)
        case ("load") {
            SPFileChooser.open(null).call(driver.open);
        }
        case ("close") {
            window.dispose();
        }
        case ("quit") {
            quitHandler.handler();
        }
        case ("about") {
            aboutHandler(event);
        }
        else {
            log.info("Unhandled command ``event.actionCommand``");
        }
    }
}
