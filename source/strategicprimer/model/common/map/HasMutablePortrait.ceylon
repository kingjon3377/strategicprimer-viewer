"""An interface for model objects that may have a "portrait" to display in the "fixture
   details" panel, where that portrait can be changed."""
shared interface HasMutablePortrait satisfies HasPortrait {
    "The filename of an image to use as a portrait."
    shared actual formal variable String portrait;
}
