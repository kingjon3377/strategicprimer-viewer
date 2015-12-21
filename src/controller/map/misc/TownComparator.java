package controller.map.misc;

import model.map.fixtures.towns.AbstractTown;
import model.map.fixtures.towns.City;
import model.map.fixtures.towns.Fortification;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.ITownFixture;
import model.map.fixtures.towns.Town;
import model.map.fixtures.towns.TownSize;
import model.map.fixtures.towns.TownStatus;
import org.eclipse.jdt.annotation.NonNull;

import java.util.Comparator;

/**
 * A comparator for towns, to put active ones before abandoned ones before ruined ones
 * before burned-out ones, bigger ones before smaller ones, cities before towns before
 * fortifications, and thereafter alphabetically. We only accept AbstractTowns because I
 * don't want to have to deal with villages and fortresses too ...
 *
 * TODO: Write tests.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class TownComparator implements Comparator<@NonNull AbstractTown> {
	/**
	 * A comparator for town-sizes.
	 */
	private static final Comparator<TownSize> SIZE_CMP = (sizeOne, sizeTwo) -> {
		if (sizeOne == sizeTwo) {
			return 0; // NOPMD
		} else if (TownSize.Large == sizeOne) {
			return -1; // NOPMD
		} else if (TownSize.Large == sizeTwo) {
			return 1; // NOPMD
		} else if (TownSize.Medium == sizeOne) {
			return -1; // NOPMD
		} else {
			return 1;
		}
	};

	/**
	 * A comparator for town-statuses, to put active towns before abandoned ones before
	 * ruined ones before burned-out ones.
	 */
	private static final Comparator<TownStatus> ST_CMP = (statusOne, statusTwo) -> {
		if (statusOne == statusTwo) {
			return 0; // NOPMD
		} else if (TownStatus.Active == statusOne) {
			return -1; // NOPMD
		} else if (TownStatus.Active == statusTwo) {
			return 1; // NOPMD
		} else if (TownStatus.Abandoned == statusOne) {
			return -1; // NOPMD
		} else if (TownStatus.Abandoned == statusTwo) {
			return 1; // NOPMD
		} else if (TownStatus.Ruined == statusOne) {
			return -1; // NOPMD
		} else {
			return 1;
		}
	};

	/**
	 * A comparator for towns, sorting them *only* on the basis of kind, putting
	 * fortresses before cities before towns before fortifications before villages.
	 */
	private static final Comparator<ITownFixture> KIND_CMP = (townOne, townTwo) -> {
		if (townOne instanceof Fortress) {
			if (townTwo instanceof Fortress) {
				return 0; // NOPMD
			} else {
				return -1; // NOPMD
			}
		} else if (townTwo instanceof Fortress) {
			return 1; // NOPMD
		} else if (townOne instanceof City) {
			if (townTwo instanceof City) {
				return 0; // NOPMD
			} else {
				return -1; // NOPMD
			}
		} else if (townTwo instanceof City) {
			return 1; // NOPMD
		} else if (townOne instanceof Town) {
			if (townTwo instanceof Town) {
				return 0; // NOPMD
			} else {
				return -1; // NOPMD
			}
		} else if (townTwo instanceof Town) {
			return 1; // NOPMD
		} else if (townOne instanceof Fortification) {
			if (townTwo instanceof Fortification) {
				return 0; // NOPMD
			} else {
				return -1; // NOPMD
			}
		} else if (townTwo instanceof Fortification) {
			return 1; // NOPMD
		} else {
			// They should be both villages ...
			return 0;
		}
	};

	/**
	 * This is hackishly implemented; fortunately, in each case I can rely on there being
	 * only three (or four, for status) possibilities and the two towns' values for them
	 * not being the same.
	 *
	 * @param townOne the first town
	 * @param townTwo the second
	 * @return a negative integer if the first is "less" than the second, zero if they're
	 * the same, and a positive integer if the first is "greater" than the second.
	 */
	@Override
	public int compare(final AbstractTown townOne,
	                   final AbstractTown townTwo) {
		if (townOne.status() == townTwo.status()) {
			if (townOne.size() == townTwo.size()) {
				if (townOne.getClass().equals(townTwo.getClass())) {
					return townOne.getName().compareTo(townTwo.getName()); // NOPMD
				} else {
					return KIND_CMP.compare(townOne, townTwo); // NOPMD
				}
			} else {
				return SIZE_CMP.compare(townOne.size(), townTwo.size()); // NOPMD
			}
		} else {
			return ST_CMP.compare(townOne.status(), townTwo.status());
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TownComparator";
	}
}
