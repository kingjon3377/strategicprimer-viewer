"An interface for the representation of a player in the game."
// TODO: Split mutability into separate interface (or just move down to impl?)
shared interface Player satisfies Comparable<Player>&HasName&HasMutablePortrait {
    "The player's ID number."
    shared formal Integer playerId;

    "Whether this is the current player."
    shared formal Boolean current;

    """Whether this is the (or an) "independent" player---the "owner" of unowned
       fixtures."""
    shared default Boolean independent => "independent" == name.lowercased;

    "The filename of a flag for the player."
    shared formal actual variable String portrait;

    "The country the player is associated with."
    shared formal String? country;
}
