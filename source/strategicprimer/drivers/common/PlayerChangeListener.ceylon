import strategicprimer.model.common.map {
    Player
}
"An interface for things that want to be told when the current player changes."
shared interface PlayerChangeListener {
    "Handle a change to which player is current."
    shared formal void playerChanged(Player? previousCurrent, Player newCurrent);
}
