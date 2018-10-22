"An encapsulation of a map's dimensions (and its map version as well)."
// This is an interface so we can make a mock object "satisfying" it and guarantee it is
// never referenced by making all of its attributes evaluate [[nothing]].
shared interface MapDimensions {
    "The number of rows in the map."
    shared formal Integer rows;

    "The number of columns in the map."
    shared formal Integer columns;

    "The map version."
    shared formal Integer version;
}
