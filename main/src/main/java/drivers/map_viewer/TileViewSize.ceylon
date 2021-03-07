import lovelace.util.common {
    todo
}

"How big, in pixels, the GUI representation of a tile should be at the specified zoom
 level."
todo("Even better zoom support", "tests")
Integer scaleZoom(Integer zoomLevel, Integer mapVersion) {
    switch (mapVersion)
    case (1) { return zoomLevel * 2; }
    case (2) { return zoomLevel * 3; }
    else { throw AssertionError("Unknown version"); }
}
