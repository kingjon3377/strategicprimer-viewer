import javax.swing {
    JMenuBar
}
import java.awt.event {
    ActionEvent
}
import strategicprimer.drivers.common {
    ISPDriver
}
import strategicprimer.drivers.gui.common {
    SPMenu
}
import java.awt {
    Component
}

"A set of menus for the worker GUI (and other related apps)."
shared JMenuBar workerMenu(
        "The broker that handles menu items, or arranges for them to be handled"
        Anything(ActionEvent) handler,
        """Any component in the window this is to be attached to, which should close on
           "Close"."""
        Component component,
        "The current driver."
        ISPDriver driver) => SPMenu.forWindowContaining(component,
            SPMenu.createFileMenu(handler, driver),
            SPMenu.disabledMenu(SPMenu.createMapMenu(handler, driver)),
            SPMenu.createViewMenu(handler, driver));
