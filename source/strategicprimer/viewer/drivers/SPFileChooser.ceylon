import javax.swing {
    JFileChooser
}
import lovelace.util.jvm {
    FileChooser
}
import lovelace.util.common {
    PathWrapper
}
import java.awt {
    JFileDialog=FileDialog
}
shared class SPFileChooser extends FileChooser {
    shared new open(PathWrapper? loc = null,
            JFileChooser|JFileDialog fileChooser = IOHandler.filteredFileChooser(true))
            extends FileChooser.open(fileChooser, loc) {}
    shared new save(PathWrapper? loc,
            JFileChooser|JFileDialog fileChooser = IOHandler.filteredFileChooser(false))
            extends FileChooser.save(loc, fileChooser) {}
    shared new custom(PathWrapper? loc, String approveText,
            JFileChooser|JFileDialog fileChooser = IOHandler.filteredFileChooser(false))
            extends FileChooser.custom(loc, approveText, fileChooser) {}
}
