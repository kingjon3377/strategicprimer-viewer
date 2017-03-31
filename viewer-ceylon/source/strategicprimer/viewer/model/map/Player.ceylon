import model.map {
    HasName
}
"An interface for the representation of a player in the game."
shared interface Player satisfies Comparable<Player>&HasName {
	"The player's ID number."
	shared formal Integer playerId;
	"Whether this is the current player."
	shared formal Boolean current;
	"""Whether this is the (or an) "independent" player---the "owner" of unowned
	   fixtures."""
	shared default Boolean independent => "independent" == name.lowercased;
}