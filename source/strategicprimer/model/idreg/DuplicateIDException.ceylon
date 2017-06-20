import lovelace.util.common {
    todo
}
import javax.xml.stream {
    Location
}
"An exception to warn about duplicate IDs."
shared class DuplicateIDException extends Exception {
    shared new (Integer id) extends Exception("Duplicate ID #``id``") {}
    shared new atLocation(Integer id, Location location)
            extends Exception("Duplicate ID #``id`` at line ``location.lineNumber
                ``, column ``location.columnNumber``") {}
}