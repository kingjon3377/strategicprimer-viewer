import strategicprimer.drivers.common.cli {
    ICLIHelper
}
"An interface for factories for drivers that operate on files rather than
 pre-parsed maps."
shared interface UtilityDriverFactory satisfies DriverFactory {
    "The driver."
    shared formal UtilityDriver createDriver(
            "The interface to interact with the user, either on the console or in a window
             emulating a console"
            ICLIHelper cli,
            "Any (already-processed) command-line options"
            SPOptions options);
}