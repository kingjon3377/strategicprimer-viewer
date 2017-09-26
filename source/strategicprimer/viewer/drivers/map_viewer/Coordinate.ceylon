import lovelace.util.common {
    todo
}
"An (x, y) pair, to reduce the number of arguments to a [[TileDrawHelper]]."
todo("Use a Tuple instead?", "Test performance implications of that")
shared class Coordinate(x, y) {
    "The X coordinate or width."
    shared Integer x;
    "The Y coordinate or height."
    shared Integer y;
    shared actual String string => "[``x``, ``y``]";
}
