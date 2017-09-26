import ceylon.interop.java {
    createJavaObjectArray
}

import java.lang {
    ObjectArray
}

"An interface for collections of players."
shared interface IPlayerCollection satisfies {Player*}&Subsettable<{Player*}> {
    "Get the player with the given player-ID, or a new player with that number if we
     didn't have one. In the latter case, if this is mutable, add it to the collection."
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
