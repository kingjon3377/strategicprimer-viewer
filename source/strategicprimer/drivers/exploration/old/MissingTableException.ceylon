"An exception to throw when a table is missing."
shared class MissingTableException(table)
        extends Exception("Missing table ``table``") {
    "The name of the missing table."
    shared String table;
}