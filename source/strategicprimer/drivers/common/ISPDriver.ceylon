"An interface for the apps in this suite, so a single entry-point can start
 different apps based on options and common code (e.g. file handling) can be
 centralized instead of duplicated."
shared interface ISPDriver of UtilityDriver|ModelDriver {
}
