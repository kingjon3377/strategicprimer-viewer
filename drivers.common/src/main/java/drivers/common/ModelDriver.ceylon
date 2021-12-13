"An interface for drivers which operate on a map model of some kind."
shared interface ModelDriver of CLIDriver|ReadOnlyDriver|GUIDriver satisfies ISPDriver {
    "Run the driver on a driver model."
    shared formal void startDriver();

    "The underlying model."
    shared formal IDriverModel model;
}
