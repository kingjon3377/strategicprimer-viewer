"An exception to indicate a file that was supposed to be opened was not present. It is
 intended to wrap and replace platform-specific exceptions so only the lowest level of
 code has to explicitly depend on the platform-specific module."
shared class MissingFileException(shared PathWrapper filename, Throwable? cause = null)
    extends Exception("File ``filename`` not found", cause) {}
