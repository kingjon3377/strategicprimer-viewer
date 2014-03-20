package model.workermgmt;

import java.util.ArrayList;
import java.util.List;

import model.map.ITile;
import model.map.ITileCollection;
import model.map.MapView;
import model.map.Player;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.towns.Fortress;
import model.misc.AbstractDriverModel;
import view.util.SystemOut;

/**
 * A model to underlie the advancement GUI, etc.
 *
 * @author Jonathan Lovelace
 *
 */
public class WorkerModel extends AbstractDriverModel implements IWorkerModel {
	/**
	 * Constructor.
	 *
	 * @param map the map we're wrapping.
	 * @param filename the name of the file the map was loaded from or should be
	 *        saved to
	 */
	public WorkerModel(final MapView map, final String filename) {
		setMap(map, filename);
	}

	/**
	 * @param player a player in the map
	 * @return a list of that player's units
	 */
	@Override
	public final List<Unit> getUnits(final Player player) {
		final List<Unit> retval = new ArrayList<>();
		final ITileCollection tiles = getMap().getTiles();
		for (final Point point : tiles) {
			if (point != null) {
				final ITile tile = tiles.getTile(point);
				retval.addAll(getUnits(tile, player));
			}
		}
		return retval;
	}

	/**
	 * @param iter a sequence of members of that type
	 * @param player a player
	 * @return a list of the members of the sequence that are units owned by the
	 *         player
	 */
	private static List<Unit> getUnits(final Iterable<? super Unit> iter,
			final Player player) {
		final List<Unit> retval = new ArrayList<>();
		for (final Object obj : iter) {
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
	public final void addUnit(final Unit unit) {
		final ITileCollection tiles = getMap().getTiles();
		for (final Point point : tiles) {
			if (point == null) {
				continue;
			}
			final ITile tile = tiles.getTile(point);
			for (final TileFixture fix : tile) {
				if (fix instanceof Fortress
						&& unit.getOwner().equals(((Fortress) fix).getOwner())
						&& "HQ".equals(((Fortress) fix).getName())) {
					((Fortress) fix).addUnit(unit);
					return;
				}
			}
		}
		SystemOut.SYS_OUT.println("No suitable location found");
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "WorkerModel";
	}
}
