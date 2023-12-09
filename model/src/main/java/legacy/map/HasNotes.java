package legacy.map;

import common.map.Player;

/**
 * An interface for fixtures players can record notes on. These notes should be
 * ignored in subset calculations, should generally not be shared in map
 * trades, and should not be discovered by other players, but should be
 * serialized to disk.
 */
public interface HasNotes extends IFixture {
    String getNote(Player player);

    String getNote(int player);

    void setNote(Player player, String note);

    Iterable<Integer> getNotesPlayers();
}
