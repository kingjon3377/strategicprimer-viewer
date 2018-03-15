import lovelace.util.common {
    todo
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
suppressWarnings("expressionTypeNothing")
shared class UtilityMenu(SPFrame parent) extends JMenuBar() {
    void aboutHandler(ActionEvent event) =>
            aboutDialog(parent, parent.windowName).setVisible(true);
    JMenu menu = JMenu("File");
    menu.add(createMenuItem("Close", KeyEvent.vkW, "Close this window",
                parent.dispose, createAccelerator(KeyEvent.vkW)));
    if (platform.systemIsMac) {
        Application.application.setAboutHandler((AppEvent.AboutEvent event) {
            Object source = WindowList.getWindows(true, false).iterable.coalesced.last else event;
            aboutHandler(ActionEvent(source, ActionEvent.actionFirst,
                "About"));
        });
    } else {
        menu.add(createMenuItem("About", KeyEvent.vkB, "Show development credits",
            aboutHandler, createAccelerator(KeyEvent.vkB)));
        menu.addSeparator();
        menu.add(createMenuItem("Quit", KeyEvent.vkQ, "Quit the application",
                    (ActionEvent event) => process.exit(0), createAccelerator(KeyEvent.vkQ)));
    }
    add(menu);
    add(WindowMenu(parent));
}
