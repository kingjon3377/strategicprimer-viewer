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
    IDriverModel
}
"A set of menus for the worker GUI (and other related apps)."
shared JMenuBar workerMenu(
        "The broker that handles menu items, or arranges for them to be handled"
        Anything(ActionEvent) handler,
        """The window this is to be attached to, whic should close on "Close"."""
        JFrame parentFrame,
        "The current driver model."
        IDriverModel model) {
    SPMenu retval = SPMenu();
    retval.add(retval.createFileMenu(handler, model));
    retval.addDisabled(retval.createMapMenu(handler, model));
    retval.add(retval.createViewMenu(handler, model));
    retval.add(WindowMenu(parentFrame));
    return retval;
}
