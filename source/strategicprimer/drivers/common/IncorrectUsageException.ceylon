"An exception to throw when a driver fails because the user tried to use it improperly."
shared class IncorrectUsageException(correctUsage)
        extends DriverFailedException(AssertionError("Incorrect usage"),
    "Incorrect usage") {
    """The "usage object" for the driver, describing its correct usage."""
    shared IDriverUsage correctUsage;
}
