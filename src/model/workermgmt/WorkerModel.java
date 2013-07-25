package model.workermgmt;

import java.util.ArrayList;
import java.util.List;

import model.map.MapView;
import model.map.Player;
import model.map.Point;
import model.map.Tile;
import model.map.TileCollection;
import model.map.TileFixture;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.towns.Fortress;
import model.misc.AbstractDriverModel;
/**
 * A model to underlie the advancement GUI, etc.
 * @author Jonathan Lovelace
 *
 */
public class WorkerModel extends AbstractDriverModel implements IWorkerModel {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Constructor.
	 * @param map the map we're wrapping.
	 * @param filename the name of the file the map was loaded from or should be saved to
	 */
	public WorkerModel(final MapView map, final String filename) {
		setMap(map, filename);
	}
	/**
	 * @param player a player in the map
	 * @return a list of that player's units
	 */
	@Override
	public List<Unit> getUnits(final Player player) {
		final List<Unit> retval = new ArrayList<>();
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
		final List<Unit> retval = new ArrayList<>();
		for (Object obj : iter) {
			if (obj instanceof Unit && ((Unit) obj).getOwner().equals(player)) {
				retval.add((Unit) obj);
			} else if (obj instanceof Fortress) {
				retval.addAll(getUnits((Fortress) obj, player));
			}
		}
		return retval;
	}
	/**
	 * @param unit the unit to add
	 */
	@Override
	public void addUnit(final Unit unit) {
		final TileCollection tiles = getMap().getTiles();
		for (final Point point : tiles) {
			final Tile tile = tiles.getTile(point);
			for (final TileFixture fix : tile) {
				if (fix instanceof Fortress
						&& unit.getOwner().equals(((Fortress) fix).getOwner())
						&& "HQ".equals(((Fortress) fix).getName())) {
					((Fortress) fix).addUnit(unit);
					return;
				}
			}
		}
	}
}
