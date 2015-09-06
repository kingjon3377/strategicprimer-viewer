package controller.map.misc;

import java.util.Comparator;

import org.eclipse.jdt.annotation.Nullable;

import model.map.fixtures.towns.AbstractTown;
import model.map.fixtures.towns.City;
import model.map.fixtures.towns.Fortification;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.ITownFixture;
import model.map.fixtures.towns.Town;
import model.map.fixtures.towns.TownSize;
import model.map.fixtures.towns.TownStatus;

/**
 * A comparator for towns, to put active ones before abandoned ones before
 * ruined ones before burned-out ones, bigger ones before smaller ones, cities
 * before towns before fortifications, and thereafter alphabetically. We only
 * accept AbstractTowns because I don't want to have to deal with villages and
 * fortresses too ...
 *
 * TODO: Write tests.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
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
 */
public final class TownComparator implements Comparator<AbstractTown> {
	/**
	 * A comparator for town-sizes.
	 */
	private static final Comparator<TownSize> SIZE_CMP = new Comparator<TownSize>() {
		@Override
		public int compare(@Nullable final TownSize one, @Nullable final TownSize two) {
				if (one == null || two == null) {
					throw new IllegalArgumentException("Asked to compare null");
				} else if (one.equals(two)) {
					return 0; // NOPMD
				} else if (TownSize.Large.equals(one)) {
					return -1; // NOPMD
				} else if (TownSize.Large.equals(two)) {
					return 1; // NOPMD
				} else if (TownSize.Medium.equals(one)) {
					return -1; // NOPMD
				} else {
					return 1;
				}
			}
	};

	/**
	 * A comparator for town-statuses, to put active towns before abandoned ones
	 * before ruined ones before burned-out ones.
	 */
	private static final Comparator<TownStatus> ST_CMP =
			new Comparator<TownStatus>() {
				@Override
				public int compare(@Nullable final TownStatus one, @Nullable final TownStatus two) {
					if (one == null || two == null) {
						throw new IllegalArgumentException(
								"Asked to compare null");
					} else if (one.equals(two)) {
						return 0; // NOPMD
					} else if (TownStatus.Active.equals(one)) {
						return -1; // NOPMD
					} else if (TownStatus.Active.equals(two)) {
						return 1; // NOPMD
					} else if (TownStatus.Abandoned.equals(one)) {
						return -1; // NOPMD
					} else if (TownStatus.Abandoned.equals(two)) {
						return 1; // NOPMD
					} else if (TownStatus.Ruined.equals(one)) {
						return -1; // NOPMD
					} else {
						return 1;
					}
				}
			};

	/**
	 * A comparator for towns, sorting them *only* on the basis of kind, putting
	 * fortresses before cities before towns before fortifications before villages.
	 */
	private static final Comparator<ITownFixture> KIND_CMP =
			new Comparator<ITownFixture>() {
				@Override
				public int compare(@Nullable final ITownFixture one,
						@Nullable final ITownFixture two) {
					if (one == null || two == null) {
						throw new IllegalArgumentException(
								"Asked to compare null");
					} else if (one instanceof Fortress) {
						if (two instanceof Fortress) {
							return 0; // NOPMD
						} else {
							return -1; // NOPMD
						}
					} else if (two instanceof Fortress) {
						return 1; // NOPMD
					} else if (one instanceof City) {
						if (two instanceof City) {
							return 0; // NOPMD
						} else {
							return -1; // NOPMD
						}
					} else if (two instanceof City) {
						return 1; // NOPMD
					} else if (one instanceof Town) {
						if (two instanceof Town) {
							return 0; // NOPMD
						} else {
							return -1; // NOPMD
						}
					} else if (two instanceof Town) {
						return 1; // NOPMD
					} else if (one instanceof Fortification) {
						if (two instanceof Fortification) {
							return 0; // NOPMD
						} else {
							return -1; // NOPMD
						}
					} else if (two instanceof Fortification) {
						return 1; // NOPMD
					} else {
						// They should be both villages ...
						return 0;
					}
				}
			};

	/**
	 * This is hackishly implemented; fortunately, in each case I can rely on
	 * there being only three (or four, for status) possibilities and the two
	 * towns' values for them not being the same.
	 *
	 * @param one the first town
	 * @param two the second
	 * @return a negative integer if the first is "less" than the second, zero
	 *         if they're the same, and a positive integer if the first is
	 *         "greater" than the second.
	 */
	@Override
	public int compare(@Nullable final AbstractTown one, @Nullable final AbstractTown two) {
		if (one == null || two == null) {
			throw new IllegalArgumentException("Asked to compare null");
		} else if (one.status().equals(two.status())) {
			if (one.size().equals(two.size())) {
				if (one.getClass().equals(two.getClass())) {
					return one.getName().compareTo(two.getName()); // NOPMD
				} else {
					return KIND_CMP.compare(one, two); // NOPMD
				}
			} else {
				return SIZE_CMP.compare(one.size(), two.size()); // NOPMD
			}
		} else {
			return ST_CMP.compare(one.status(), two.status());
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
