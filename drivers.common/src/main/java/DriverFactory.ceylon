import lovelace.util.common {
    todo
}
import strategicprimer.model.common.map {
    HasName
}

"An interface for factories for drivers, so one run() method can start different drivers
 based on user-supplied options, without the drivers having to have no-arg initializers."
todo("Take the driver as a type parameter.",
    "Make ISPDriver implementations no longer shared")
shared interface DriverFactory of UtilityDriverFactory|ModelDriverFactory
        satisfies HasName {
    """An object giving details to describe how the driver should be invoked and used."""
    shared formal IDriverUsage usage;

    "What to call this driver in a CLI list."
    shared actual default String name => usage.shortDescription;
}
