"""An interface for fixtures that have a "kind" property."""
shared interface HasKind {
    "The kind of whatever this is."
    shared formal String kind;
}