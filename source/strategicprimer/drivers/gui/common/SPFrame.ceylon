import java.awt {
    Dimension
}
import java.nio.file {
    JPath=Path
}

import javax.swing {
    JFrame,
    WindowConstants,
    TransferHandler
}
import java.awt.datatransfer {
    DataFlavor
}
import java.io {
    JFile=File
}
import java.util {
    JList=List
}
import ceylon.logging {
    Logger,
    logger
}
Logger log = logger(`module strategicprimer.drivers.gui.common`);
"A [[TransferHandler]] to allow SP apps to accept dropped files."
class FileDropHandler() extends TransferHandler() {
    shared late SPFrame app;
    shared actual Boolean canImport(TransferSupport support) => support.drop &&
        app.supportsDroppedFiles &&
        support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    shared actual Boolean importData(TransferSupport support) {
        if (!support.drop ||
        !support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            return false;
        }
        JList<JFile> payload;
        try {
            value temp = support.transferable.getTransferData(DataFlavor.javaFileListFlavor);
            assert (is JList<JFile> temp);
            payload = temp;
        } catch (Exception except) {
            log.warn("Caught an exception trying to unmarshall dropped files",
                except);
            return false;
        }
        for (file in payload) {
            app.acceptDroppedFile(file.toPath());
        }
        return true;
    }
}
"An intermediate subclass of JFrame to take care of some common setup things that can't be
 done in an interface."
shared abstract class SPFrame(String windowTitle, JPath? file, Dimension? minSize = null)
        extends JFrame(windowTitle) satisfies ISPWindow {
    if (exists file) {
        title = "``file`` | ``windowTitle``";
        rootPane.putClientProperty("Window.documentFile", file.toFile());
    }
    defaultCloseOperation = WindowConstants.disposeOnClose;
    if (exists minSize) {
        setMinimumSize(minSize);
    }
    "Whether this app supports having files dropped on it."
    shared formal Boolean supportsDroppedFiles;
    "Handle a dropped file."
    shared default void acceptDroppedFile(JPath file) {}
    FileDropHandler temp = FileDropHandler();
    transferHandler = temp;
    temp.app = this;
}
