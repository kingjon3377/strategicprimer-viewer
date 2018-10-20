import javax.swing {
    JFileChooser
}
import lovelace.util.jvm {
    FileChooser,
    platform
}
import lovelace.util.common {
    PathWrapper,
    todo
}
import java.awt {
    JFileDialog=FileDialog,
    Frame
}
import javax.swing.filechooser {
    FileFilter,
    FileNameExtensionFilter
}
import java.io {
    FilenameFilter,
    JFile=File
}

"An extension of the more-generic [[FileChooser]] class that, by default,
 filters the view to show only files that (by their extension) might be SP map
 files, and that chooses between [[Swing|JFileChooser]] and [[AWT|JFileDialog]]
 based on what platform the code is running on."
shared class SPFileChooser extends FileChooser {
    static FileFilter mapExtensionsFilter = FileNameExtensionFilter(
        "Strategic Primer world map files", "map", "xml", "db");

    "A factory method for [[JFileChooser]] (or AWT [[FileDialog|JFileDialog]] taking a
     [[FileFilter]] to apply in the same operation."
    todo("Move functionality into FileChooser somehow?")
    shared static JFileChooser|JFileDialog filteredFileChooser(
            "Whether to allow multi-selection."
            Boolean allowMultiple,
            "The current directory."
            String current = ".",
            "The filter to apply."
            FileFilter? filter = mapExtensionsFilter) {
        if (platform.systemIsMac) {
            JFileDialog retval = JFileDialog(null of Frame?);
            if (exists filter) {
                retval.filenameFilter = object satisfies FilenameFilter {
                    shared actual Boolean accept(JFile dir, String name) =>
                            filter.accept(JFile(dir, name));
                };
            }
            return retval;
        } else {
            JFileChooser retval = JFileChooser(current);
            if (exists filter) {
                retval.fileFilter = filter;
            }
            return retval;
        }
    }

    """Constructor for a filtered "Open" dialog."""
    shared new open(PathWrapper? loc = null,
            JFileChooser|JFileDialog fileChooser = filteredFileChooser(true))
            extends FileChooser.open(fileChooser, loc) {}

    """Constructor for a filtered "Save" dialog."""
    shared new save(PathWrapper? loc,
            JFileChooser|JFileDialog fileChooser = filteredFileChooser(false))
            extends FileChooser.save(loc, fileChooser) {}

    "Constructor for a filtered custom-verb dialog."
    shared new custom(PathWrapper? loc, String approveText,
            JFileChooser|JFileDialog fileChooser = filteredFileChooser(false))
            extends FileChooser.custom(loc, approveText, fileChooser) {}
}
