import lovelace.util.common {
    todo
}

"""An interface for model objects that may have a "portrait" to display in the "fixture
   details" panel."""
todo("Split mutability into separate interface.")
shared interface HasPortrait {
    "The filename of an image to use as a portrait."
    shared formal variable String portrait;
}
