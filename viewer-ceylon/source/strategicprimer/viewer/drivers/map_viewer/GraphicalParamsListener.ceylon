import lovelace.util.common {
    todo
}

import model.viewer {
    VisibleDimensions
}
import java.util {
    EventListener
}
"An interface for objects that want to keep abreast of visible dimensions and zoom level."
todo("Take a polymorphic Event object instead of specifying two methods?")
shared interface GraphicalParamsListener satisfies EventListener {
    "Handle a change in map dimensions."
    shared formal void dimensionsChanged(
            "The previous dimensions"
            VisibleDimensions oldDimensions,
            "The new dimensions"
            VisibleDimensions newDimensions);
    "Handle a change in tile size (i.e. zoom level)."
    shared formal void tileSizeChanged(
            "The previous tile size/zoom level"
            Integer oldSize,
            "The new tile size/zoom level"
            Integer newSize);
}