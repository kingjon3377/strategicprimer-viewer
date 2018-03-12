"An exception to warn about duplicate IDs."
shared class DuplicateIDException extends Exception {
    shared new (Integer id) extends Exception("Duplicate ID #``id``") {}
    shared new atLocation(Integer id, Integer line, Integer column)
            extends Exception("Duplicate ID #``id`` at line ``line``, column ``column``") {}
}
