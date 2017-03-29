import model.map {
    HasKind
}
"""An interface for fixtures that have a "kind" property that is mutable."""
shared interface HasMutableKind satisfies HasKind {
	"The kind of whatever this is, now specified to be `variable`."
	shared actual formal variable String kind;
}