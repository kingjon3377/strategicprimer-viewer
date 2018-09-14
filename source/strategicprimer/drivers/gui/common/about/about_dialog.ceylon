import java.awt {
    Component,
    Frame,
    BorderLayout,
    Dimension
}
import lovelace.util.common {
    todo,
    as,
    silentListener
}
import ceylon.regex {
    Regex,
    regex
}
import javax.swing {
    JEditorPane,
    ScrollPaneConstants,
    JScrollPane,
    JDialog
}
import lovelace.util.jvm {
    listenedButton,
    centeredHorizontalBox,
    platform
}
import strategicprimer.drivers.gui.common {
    SPDialog
}
import java.lang {
    Types
}
"A dialog to explain what this program is, and the sources of code and graphics."
todo("FIXME: Credits for other images?") // FIXME
shared JDialog aboutDialog(Component? parentComponent, String? app) {
    JDialog retval = SPDialog(as<Frame>(parentComponent), "About");
    retval.setLayout(BorderLayout());
    assert (exists resource =
            `module strategicprimer.drivers.gui.common`.resourceByPath("about.html"));
    Regex matcher = regex("App Name Here", true);
    String html = matcher.replace(resource.textContent(),
        app else "Strategic Primer Assistive Programs");
    JEditorPane pane = JEditorPane("text/html", html);
    pane.caretPosition = 0; // scroll to the top
    pane.editable = false;
    JScrollPane scrollPane;
    if (platform.systemIsMac) {
        scrollPane = JScrollPane(pane,
            ScrollPaneConstants.verticalScrollbarAlways,
            ScrollPaneConstants.horizontalScrollbarAlways);
    } else {
        scrollPane = JScrollPane(pane,
            ScrollPaneConstants.verticalScrollbarAsNeeded,
            ScrollPaneConstants.horizontalScrollbarAsNeeded);
    }
    scrollPane.minimumSize = Dimension(300, 400);
    scrollPane.preferredSize = Dimension(400, 500);
    retval.add(scrollPane, Types.nativeString(BorderLayout.center));
    retval.add(centeredHorizontalBox(listenedButton("Close",
                silentListener(retval.dispose))),
        Types.nativeString(BorderLayout.pageEnd));
    retval.pack();
    return retval;
}
