"""An interface for model objects that may have a "portrait" to display in the "fixture
   details" panel."""
shared interface HasPortrait {
    "The filename of an image to use as a portrait."
    shared formal String portrait;
}
