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
            app.acceptDroppedFile(file.toPath());
        }
        return true;
    }
}
"An intermediate subclass of JFrame to take care of some common setup things that can't be
 done in an interface."
shared class SPFrame(String windowTitle, JPath? file, Dimension? minSize = null,
		"Whether this app supports having files dropped on it."
		shared default Boolean supportsDroppedFiles = false,
		Anything(JPath) droppedFileHandler = noop,
		"The name of the window, for use in customizing the About dialog"
		shared actual default String windowName = windowTitle)
        extends JFrame(windowTitle) satisfies ISPWindow {
    if (exists file) {
        title = "``file`` | ``windowTitle``";
        rootPane.putClientProperty("Window.documentFile", file.toFile());
    }
    "Handle a dropped file."
    shared default void acceptDroppedFile(JPath file) => droppedFileHandler(file);
    shared void showWindow() => setVisible(true);
    defaultCloseOperation = WindowConstants.disposeOnClose;
    if (exists minSize) {
        setMinimumSize(minSize);
    }
    FileDropHandler temp = FileDropHandler();
    transferHandler = temp;
    temp.app = this;
}
