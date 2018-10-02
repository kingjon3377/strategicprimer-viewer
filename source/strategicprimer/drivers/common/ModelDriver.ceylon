import strategicprimer.drivers.common.cli {
    ICLIHelper
}
"An interface for drivers which operate on a map model of some kind."
// TODO: Make the required type of driver a type parameter instead of casting/copy-constructing in startDriverOnModel()
shared interface ModelDriver of CLIDriver|ReadOnlyDriver|GUIDriver satisfies ISPDriver {
    "Run the driver on a driver model."
    shared formal void startDriverOnModel(
            "The interface to interact with the user, either on the console or in a window
             emulating a console"
            ICLIHelper cli,
            "Any (already-processed) command-line options"
            SPOptions options,
            "The driver-model that should be used by the app."
            IDriverModel model);
}
