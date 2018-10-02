import strategicprimer.model.common.map {
    HasName
}
"An interface for drivers, so one main() method can start different drivers based on
 options."
shared interface ISPDriver of UtilityDriver|ModelDriver satisfies HasName {
    """The usage object for the driver. The default implementation throws, to allow
       satisfying interfaces to be "functional" (single-formal-method) interfaces, but
       implementations *should* implement this."""
    shared formal IDriverUsage usage;
    "What to call this driver in a CLI list."
    shared actual default String name => usage.shortDescription;
}
