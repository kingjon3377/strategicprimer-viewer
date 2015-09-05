package model.workermgmt;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.Player;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.ProxyUnit;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.towns.Fortress;
import model.misc.AbstractMultiMapModel;
import util.NullCleaner;
import util.Pair;
import view.util.SystemOut;

/**
 * A model to underlie the advancement GUI, etc.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public class WorkerModel extends AbstractMultiMapModel implements IWorkerModel {
	/**
	 * Constructor.
	 *
	 * @param map
	 *            the map we're wrapping.
	 * @param file
	 *            the file the map was loaded from or should be
	 *            saved to
	 */
	public WorkerModel(final IMutableMapNG map, final File file) {
		setMap(map, file);
	}
	/**
	 * @param player a player in the map
	 * @return a list of that player's units
	 */
	@Override
	public final List<IUnit> getUnits(final Player player) {
		if (getSubordinateMaps().iterator().hasNext()) {
			Map<Integer, IUnit> retval = new TreeMap<>();
			for (Pair<IMutableMapNG, File> pair : getAllMaps()) {
				IMapNG map = pair.first();
				for (Point point : map.locations()) {
					for (IUnit unit : getUnits(map.getOtherFixtures(point),
							player)) {
						if (!retval.containsKey(Integer.valueOf(unit.getID()))) {
							IUnit proxy = new ProxyUnit(unit.getID());
							((ProxyUnit) proxy).addProxied(unit);
							retval.put(NullCleaner.assertNotNull(Integer.valueOf(unit.getID())), proxy);
						} else {
							((ProxyUnit) retval.get(Integer.valueOf(unit.getID()))).addProxied(unit);
						}
					}
				}
			}
			return new ArrayList<>(retval.values());
		} else {
			// Just in case I missed something in the proxy implementation, make
			// sure things work correctly when there's only one map.
			final List<IUnit> retval = new ArrayList<>();
			for (final Point point : getMap().locations()) {
				retval.addAll(
						getUnits(getMap().getOtherFixtures(point), player));
			}
			return retval;
		}
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
		return Collections.unmodifiableList(new ArrayList<>(retval));
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
		for (final Point point : getMap().locations()) {
			for (final TileFixture fix : getMap().getOtherFixtures(point)) {
				if (fix instanceof Fortress
						&& unit.getOwner().equals(((Fortress) fix).getOwner())
						&& "HQ".equals(((Fortress) fix).getName())) {
					((Fortress) fix).addMember(unit);
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
