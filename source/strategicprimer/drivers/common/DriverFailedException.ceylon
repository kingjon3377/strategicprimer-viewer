import lovelace.util.common {
    todo
}

"An exception to throw whenever a driver fails, so drivers only have to directly handle
 one exception class."
todo("Is this really necessary any more?")
shared class DriverFailedException extends Exception {
    shared new (Throwable cause,
            String message = "The app could not start because of an exception:")
            extends Exception(message, cause) {}

    shared new illegalState(String message) extends
            DriverFailedException(AssertionError(message), message) {}
}
