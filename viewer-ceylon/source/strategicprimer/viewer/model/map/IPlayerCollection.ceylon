import ceylon.interop.java {
    createJavaObjectArray
}

import java.lang {
    ObjectArray
}

import lovelace.util.common {
    todo
}

import model.map {
    Player,
    Subsettable
}
"An interface for collections of players."
shared interface IPlayerCollection satisfies {Player*}&Subsettable<IPlayerCollection> {
	"Get the player with the given player-ID, or a new player with that number if we
	 didn't have one."
	todo("Should we store that newly-created Player?")
	shared formal Player getPlayer(Integer player);
	"The current player, or if no player was marked current a new player with a
	 negative number and empty name and get it."
	shared formal Player currentPlayer;
	"""The player that should own "independent" fixtures."""
	shared formal Player independent;
	"Clone the collection."
	shared formal IPlayerCollection copy();
	"The players as an array."
	shared default ObjectArray<Player> asArray() => createJavaObjectArray(this);
}