import javax.xml.stream {
    Location
}
"A custom exception for XML format errors."
shared abstract class SPFormatException(String errorMessage, location,
        Throwable? errorCause = null)
        extends Exception(
            "Incorrect SP XML at line ``location.lineNumber``, column ``location
                .columnNumber``: ``errorMessage``", errorCause) {
    "The location of the mistake in the XML file."
    shared Location location;
    "The line of the XML file containing the mistake."
    shared Integer line => location.lineNumber;
}
