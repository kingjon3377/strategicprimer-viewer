"An interface for top-level windows in assistive programs."
shared interface ISPWindow {
    """The name of this window. This method should *not* return a string including the
       loaded file, since it is used only in the About dialog to "personalize" it for the
       particular app."""
    shared formal String windowName;
}
