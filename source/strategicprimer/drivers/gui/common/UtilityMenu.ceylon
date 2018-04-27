import lovelace.util.common {
    todo,
	silentListener
}
import com.apple.eawt {
    Application,
    AppEvent
}
import javax.swing {
    JMenuBar,
    JMenu
}
import com.pump.window {
    WindowMenu,
    WindowList
}
import strategicprimer.drivers.gui.common.about {
    aboutDialog
}
import java.awt.event {
    ActionEvent,
    KeyEvent
}
import lovelace.util.jvm {
    platform,
    createMenuItem,
    createAccelerator
}
"A simple menu for utility drivers that don't need the full complement of menus that other
 apps have."
todo("OTOH, they should probably use an almost-fully-disabled SPMenu, for consistency.")
shared class UtilityMenu(SPFrame parent) extends JMenuBar() {
    void aboutHandler(ActionEvent event) => // TODO: Make these static methods?
            aboutDialog(parent, parent.windowName).setVisible(true);
    void macAboutHandler(AppEvent.AboutEvent event) {
        Object source = WindowList.getWindows(true, false).iterable.coalesced.last else event;
        aboutHandler(ActionEvent(source, ActionEvent.actionFirst,
            "About"));
    }
    suppressWarnings("expressionTypeNothing")
    void quit() => process.exit(0);
    JMenu menu = JMenu("File");
    menu.add(createMenuItem("Close", KeyEvent.vkW, "Close this window",
                parent.dispose, createAccelerator(KeyEvent.vkW)));
    if (platform.systemIsMac) {
        Application.application.setAboutHandler(macAboutHandler);
    } else {
        menu.add(createMenuItem("About", KeyEvent.vkB, "Show development credits",
            aboutHandler, createAccelerator(KeyEvent.vkB)));
        menu.addSeparator();
        menu.add(createMenuItem("Quit", KeyEvent.vkQ, "Quit the application",
                    silentListener(quit), createAccelerator(KeyEvent.vkQ)));
    }
    add(menu);
    add(WindowMenu(parent));
}
