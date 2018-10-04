import java.awt {
    Dimension
}
import java.nio.file {
    JPaths=Paths
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
import lovelace.util.common {
    PathWrapper
}

Logger log = logger(`module strategicprimer.drivers.gui.common`);
"A [[TransferHandler]] to allow SP apps to accept dropped files."
class FileDropHandler() extends TransferHandler() {
    shared late SPFrame app;
    shared actual Boolean canImport(TransferSupport support) => support.drop &&
        app.supportsDroppedFiles &&
        support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    shared actual Boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        JList<JFile> payload;
        try {
            value temp = support.transferable.getTransferData(
                DataFlavor.javaFileListFlavor);
            assert (is JList<JFile> temp);
            payload = temp;
        } catch (Exception except) {
            log.warn("Caught an exception trying to unmarshall dropped files",
                except);
            return false;
        }
        for (file in payload) {
            app.acceptDroppedFile(PathWrapper(file.toPath().string));
        }
        return true;
    }
}
"An intermediate subclass of JFrame to take care of some common setup things that can't be
 done in an interface."
shared class SPFrame(String windowTitle, PathWrapper? file, Dimension? minSize = null,
        "Whether this app supports having files dropped on it."
        shared default Boolean supportsDroppedFiles = false,
        Anything(PathWrapper) droppedFileHandler = noop,
        "The name of the window, for use in customizing the About dialog"
        shared actual default String windowName = windowTitle)
        extends JFrame(windowTitle) satisfies ISPWindow {
    if (exists file) { // TODO: Make title dynamic, based on possibly-changing filename and 'modified' flag
        title = "``file`` | ``windowTitle``";
        rootPane.putClientProperty("Window.documentFile",
            JPaths.get(file.string).toFile());
    }
    "Handle a dropped file."
    shared default void acceptDroppedFile(PathWrapper file) => droppedFileHandler(file);
    shared void showWindow() => setVisible(true);
    defaultCloseOperation = WindowConstants.doNothingOnClose;
    if (exists minSize) {
        setMinimumSize(minSize);
    }
    FileDropHandler temp = FileDropHandler();
    transferHandler = temp;
    temp.app = this;
}
