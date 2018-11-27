"A class of exception to throw when asked to parse (or produce) XML that is syntactically
 malformed. We'd use [[ParseException]] in such cases (except when producing), but it
 doesn't allow us to pass in the underlying platform-exception."
shared class MalformedXMLException extends Exception {
    shared new (Throwable cause, String message = cause.message)
            extends Exception(message, cause) {}
    shared new notWrapping(String message) extends Exception(message) {}
}

