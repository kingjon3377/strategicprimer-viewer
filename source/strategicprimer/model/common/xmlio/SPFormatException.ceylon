"A custom exception for XML format errors."
shared abstract class SPFormatException(String errorMessage, line, column,
        Throwable? errorCause = null)
        extends Exception(
            "Incorrect SP XML at line ``line``, column ``column``: ``errorMessage``",
            errorCause) {
    "The line of the XML file containing the mistake."
    shared Integer line;
    "The column of the XML file where the mistake begins."
    shared Integer column;
}
