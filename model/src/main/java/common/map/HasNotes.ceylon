"An interface for fixtures players can record notes on. These notes should be ignored
 in subset calculations, should generally not be shared in map trades, and should not
 be discovered by other players, but should be serialized to disk."
shared interface HasNotes satisfies IFixture {
    shared formal Correspondence<Player|Integer, String>&KeyedCorrespondenceMutator<Player, String> notes;
    shared formal {Integer*} notesPlayers;
}
