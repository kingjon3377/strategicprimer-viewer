"An interface for drivers which operate on a map model of some kind."
// TODO: Make the required type of driver a type parameter instead of casting/copy-constructing in startDriverOnModel()
shared interface ModelDriver of CLIDriver|ReadOnlyDriver|GUIDriver satisfies ISPDriver {
    "Run the driver on a driver model."
    shared formal void startDriver();
}
