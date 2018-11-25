import java.io {
    JReader=Reader,
    StringReader,
    FileReader,
    FileNotFoundException
}
import lovelace.util.common {
    MissingFileException,
    PathWrapper
}
import java.nio.file {
    NoSuchFileException
}

"""Get the appropriate reader based on the given filename: if it begins "string:<", strip
   the "string:" prefix, pass the remainder to a [[StringReader]], and return it;
   otherwise return a [[FileReader]]."""
throws(`class MissingFileException`)
JReader magicReader(String possibleFilename) { // TODO: Move into mapIOHelper?
    if (possibleFilename.startsWith("string:<")) {
        return StringReader(possibleFilename.substring(7));
    } else {
        try {
            return FileReader(possibleFilename);
        } catch (FileNotFoundException|NoSuchFileException except) {
            throw MissingFileException(PathWrapper(possibleFilename), except);
        }
    }
}
