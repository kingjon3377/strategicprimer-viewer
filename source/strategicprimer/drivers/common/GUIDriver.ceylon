import lovelace.util.common {
    PathWrapper
}
import strategicprimer.model.common.map {
    IMutableMapNG
}

"An interface for drivers which operate on a map model of some kind; being GUIs, do
 not need to have the maps written back to file automatically; and have a way to get
 additional files from the user."
shared interface GUIDriver satisfies ModelDriver {
    "Ask the user to choose a file or files. (Or do something equivalent to produce a
     filename.)"
    shared formal {PathWrapper*} askUserForFiles();

    """Open the given map as a "main map." Doing so in a new window would be acceptable,
       especially if the "modified" flag is set on the currently-open main map, unless
       this driver requires at least one subordinate map; replacing the main map in the
       current window would be acceptable unless the "modified" flag is set for the
       currently-main map."""
    shared formal void open(IMutableMapNG map, PathWrapper? path);
}
