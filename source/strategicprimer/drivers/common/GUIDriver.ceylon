import lovelace.util.common {
    PathWrapper
}
"An interface for drivers which operate on a map model of some kind; being GUIs, do
 not need to have the maps written back to file automatically; and have a way to get
 additional files from the user."
shared interface GUIDriver satisfies ModelDriver {
    "Ask the user to choose a file or files. (Or do something equivalent to produce a
     filename.)"
    shared formal {PathWrapper*} askUserForFiles();
}
