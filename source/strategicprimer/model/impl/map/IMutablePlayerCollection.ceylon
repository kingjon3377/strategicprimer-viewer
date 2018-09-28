"An interface for player collections that can be modified."
shared interface IMutablePlayerCollection satisfies IPlayerCollection {
    "Add a player to the collection."
    shared formal void add(Player player);
    "Remove a player from the collection."
    shared formal void remove(Player|Integer obj);
    "Clone the collection."
    shared actual formal IMutablePlayerCollection copy();
    "The current player."
    shared actual formal variable Player currentPlayer;
}