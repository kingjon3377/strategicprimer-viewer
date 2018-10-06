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
shared class UtilityMenuHandler(UtilityGUI driver, SPFrame window) {
    void aboutHandler(ActionEvent event) => // TODO: Make these static methods?
            aboutDialog(window, window.windowName).setVisible(true);
    void macAboutHandler(AppEvent.AboutEvent event) {
        Object source =
                WindowList.getWindows(true, false).iterable.coalesced.last else event;
        aboutHandler(ActionEvent(source, ActionEvent.actionFirst,
            "About"));
    }
    if (platform.systemIsMac) {
        Application.application.setAboutHandler(macAboutHandler);
    }
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