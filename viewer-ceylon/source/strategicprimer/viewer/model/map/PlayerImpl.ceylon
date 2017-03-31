import lovelace.util.common {
    todo
}

"A representation of a player in the game."
shared class PlayerImpl(playerId, name) satisfies MutablePlayer {
	"The player's number."
	shared actual Integer playerId;
	"The player's code name."
	shared actual String name;
	"Whether this is the current player or not."
	todo("Should this really be encapsulated in Player, not PlayerCollection?")
	shared actual variable Boolean current = false;
	"An object is equal iff it is a Player with the same number and name."
	shared actual Boolean equals(Object obj) {
		if (is Player obj) {
			return playerId == obj.playerId && name == obj.name;
		} else {
			return false;
		}
	}
	shared actual Integer hash => playerId;
	shared actual Comparison compare(Player player) => playerId <=> player.playerId;
	"""If the player name is non-empty, use it; otherwise, use "player #NN"."""
	shared actual String string => (name.empty) then "player #``playerId``" else name;
}