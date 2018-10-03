import strategicprimer.model.common.map {
    IMutableMapNG
}
import lovelace.util.common {
    PathWrapper,
    todo
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
"An interface for factories for drivers that operate on map models rather than directly
 on files, which must also produce the models that their drivers consume."
todo("Take a more specific model interface as a type parameter.")
shared interface ModelDriverFactory satisfies DriverFactory {
    "Create a new instance of the driver with the given environment."
    shared formal ModelDriver createDriver(
            "The interface to interact with the user, either on the console or in a window
             emulating a console"
            ICLIHelper cli,
            "Any (already-processed) command-line options"
            SPOptions options,
            "The driver-model that should be used by the app."
            IDriverModel model);
    "Create a model to pass to [[createDriver]]. The 'modified' flag is set to [[false]]."
    shared formal IDriverModel createModel("The map." IMutableMapNG map,
            "The file it was loaded from" PathWrapper? path); // TODO: Default to SimpleDriverModel()?
}