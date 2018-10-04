import strategicprimer.viewer.drivers {
    SPMenu
}
import com.pump.window {
    WindowMenu
}
import javax.swing {
    JMenuBar,
    JFrame
}
import java.awt.event {
    ActionEvent
}
import strategicprimer.drivers.common {
    ISPDriver
}
"A set of menus for the worker GUI (and other related apps)."
shared JMenuBar workerMenu(
        "The broker that handles menu items, or arranges for them to be handled"
        Anything(ActionEvent) handler,
        """The window this is to be attached to, which should close on "Close"."""
        JFrame parentFrame,
        "The current driver."
        ISPDriver driver) => SPMenu(SPMenu.createFileMenu(handler, driver),
            SPMenu.disabledMenu(SPMenu.createMapMenu(handler, driver)),
            SPMenu.createViewMenu(handler, driver), WindowMenu(parentFrame));
