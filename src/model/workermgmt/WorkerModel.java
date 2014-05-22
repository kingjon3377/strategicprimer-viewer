package model.workermgmt;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.map.ITile;
import model.map.ITileCollection;
import model.map.MapView;
import model.map.Player;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.towns.Fortress;
import model.misc.AbstractDriverModel;
import util.NullCleaner;
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
	 * @param map
	 *            the map we're wrapping.
	 * @param file
	 *            the file the map was loaded from or should be
	 *            saved to
	 */
	public WorkerModel(final MapView map, final File file) {
		setMap(map, file);
	}
	/**
	 * @param player a player in the map
	 * @return a list of that player's units
	 */
	@Override
	public final List<IUnit> getUnits(final Player player) {
		final List<IUnit> retval = new ArrayList<>();
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
	private static List<IUnit> getUnits(final Iterable<? super Unit> iter,
			final Player player) {
		final List<IUnit> retval = new ArrayList<>();
		for (final Object obj : iter) {
			if (obj instanceof IUnit && ((IUnit) obj).getOwner().equals(player)) {
				retval.add((IUnit) obj);
			} else if (obj instanceof Fortress) {
				retval.addAll(getUnits((Fortress) obj, player));
			}
		}
		return retval;
	}

	/**
	 * @param player a player in the map
	 * @return the "kinds" of unit that player has.
	 */
	@Override
	public List<String> getUnitKinds(final Player player) {
		final Set<String> retval = new HashSet<>();
		final List<IUnit> units = getUnits(player);
		for (final IUnit unit : units) {
			retval.add(unit.getKind());
		}
		return NullCleaner.assertNotNull(Collections
				.unmodifiableList(new ArrayList<>(retval)));
	}

	/**
	 * @param player a player in the map
	 * @param kind a "kind" of unit.
	 * @return a list of the units of that kind in the map belonging to that player
	 */
	@Override
	public List<IUnit> getUnits(final Player player, final String kind) {
		final List<IUnit> units = getUnits(player);
		final List<IUnit> retval = new ArrayList<>();
		for (final IUnit unit : units) {
			if (kind.equals(unit.getKind())) {
				retval.add(unit);
			}
		}
		return retval;
	}
	/**
	 * @param unit the unit to add
	 */
	@Override
	public final void addUnit(final IUnit unit) {
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
