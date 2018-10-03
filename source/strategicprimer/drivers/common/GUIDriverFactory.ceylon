import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import lovelace.util.common {
    PathWrapper
}
"An interface for factories producing GUI drivers. This interface exists so the
 app-chooser can detect such drivers before instantiating them (which requires
 deserializing possibly-large maps)."
shared interface GUIDriverFactory satisfies ModelDriverFactory {
    "Create a new instance of the driver with the given environment."
    shared actual formal GUIDriver createDriver(
            "The interface to interact with the user, either on the console or in a window
             emulating a console"
            ICLIHelper cli,
            "Any (already-processed) command-line options"
            SPOptions options,
            "The driver-model that should be used by the app."
            IDriverModel model);
    "Ask the user to choose a file or files. (Or do something equivalent to produce a
     filename.)"
    shared formal {PathWrapper*} askUserForFiles();

}