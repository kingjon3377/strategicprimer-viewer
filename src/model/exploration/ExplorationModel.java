package model.exploration;

import java.util.ArrayList;
import java.util.List;

import model.map.IMap;
import model.map.MapView;
import model.map.Player;
import model.map.Point;
import model.map.Tile;
import model.map.TileCollection;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.towns.Fortress;
import model.misc.AbstractMultiMapModel;
import util.Pair;
/**
 * A model for exploration drivers.
 * @author Jonathan Lovelace
 *
 */
public class ExplorationModel extends AbstractMultiMapModel implements
		IExplorationModel {
	/**
	 * Constructor.
	 * @param map the starting main map
	 * @param filename the name it was loaded from
	 */
	public ExplorationModel(final MapView map, final String filename) {
		setMap(map, filename);
	}
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * @return all the players shared by all the maps
	 */
	@Override
	public List<Player> getPlayerChoices() {
		final List<Player> retval = new ArrayList<Player>();
		for (Player player : getMap().getPlayers()) {
			retval.add(player);
		}
		final List<Player> temp = new ArrayList<Player>();
		for (Pair<IMap, String> pair : getSubordinateMaps()) {
			final IMap map = pair.first();
			temp.clear();
			for (Player player : map.getPlayers()) {
				temp.add(player);
			}
			retval.retainAll(temp);
		}
		return retval;
	}
	/**
	 * @param player a player
	 * @return all that player's units in the main map
	 */
	@Override
	public List<Unit> getUnits(final Player player) {
		final List<Unit> retval = new ArrayList<Unit>();
		final TileCollection tiles = getMap().getTiles();
		for (final Point point : tiles) {
			final Tile tile = tiles.getTile(point);
			retval.addAll(getUnits(tile, player));
		}
		return retval;
	}
	/**
	 * @param iter a sequence of members of that type
	 * @param player a player
	 * @return a list of the members of the sequence that are units owned by the player
	 */
	private static List<Unit> getUnits(final Iterable<? super Unit> iter, final Player player) {
		final List<Unit> retval = new ArrayList<Unit>();
		for (Object obj : iter) {
			if (obj instanceof Unit && ((Unit) obj).getOwner().equals(player)) {
				retval.add((Unit) obj);
			} else if (obj instanceof Fortress) {
				retval.addAll(getUnits((Fortress) obj, player));
			}
		}
		return retval;
	}
}
