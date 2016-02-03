package model.exploration.old;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import model.map.IEvent;
import model.map.Player;
import model.map.Point;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.explorable.Battlefield;
import model.map.fixtures.explorable.Cave;
import model.map.fixtures.resources.MineralKind;
import model.map.fixtures.resources.MineralVein;
import model.map.fixtures.resources.StoneDeposit;
import model.map.fixtures.resources.StoneKind;
import model.map.fixtures.towns.City;
import model.map.fixtures.towns.Fortification;
import model.map.fixtures.towns.Town;
import model.map.fixtures.towns.TownSize;
import model.map.fixtures.towns.TownStatus;

/**
 * A table for legacy "events".
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
@SuppressWarnings("deprecation")
public final class LegacyTable implements EncounterTable {
	/**
	 * The list of events we can return.
	 */
	private final List<String> data = new ArrayList<>();

	/**
	 * Add the text from an Event to the list.
	 *
	 * @param event the event to get the text from
	 */
	private void addData(final IEvent event) {
		data.add(event.toString());
	}

	/**
	 * // $codepro.audit.disable sourceLength Constructor.
	 */
	public LegacyTable() {
		addData(new Battlefield(0, -1));
		addData(new Cave(0, -1));
		final Player player = new Player(-1, "Independent");
		for (final TownStatus status : TownStatus.values()) {
			assert status != null;
			for (final TownSize size : TownSize.values()) {
				assert size != null;
				addData(new City(status, size, 0, "", 0, player)); // NOPMD
				addData(new Fortification(status, size, 0, "", 0, player)); // NOPMD
				addData(new Town(status, size, 0, "", 0, player)); // NOPMD
			}
		}
		for (final MineralKind mineral : MineralKind.values()) {
			addData(new MineralVein(mineral.toString(), true, 0, 0)); // NOPMD
			addData(new MineralVein(mineral.toString(), false, 0, 0));
		}
		data.add("Nothing interesting here ...");
		for (final StoneKind stone : StoneKind.values()) {
			if (stone != null) {
				addData(new StoneDeposit(stone, 0, 0)); // NOPMD
			}
		}
	}

	/**
	 * @param terrain  ignored
	 * @param point    ignored
	 * @param fixtures any fixtures on the tile
	 * @return the event on that tile
	 */
	@Override
	public String generateEvent(final Point point, final TileType terrain,
	                            final Iterable<TileFixture> fixtures) {
		for (final TileFixture fix : fixtures) {
			if (fix instanceof IEvent) {
				return ((IEvent) fix).getText(); // NOPMD
			}
		}
		return "Nothing interesting here ...";
	}
	/**
	 * @param terrain  ignored
	 * @param point    ignored
	 * @param fixtures any fixtures on the tile
	 * @return the event on that tile
	 */
	@Override
	public String generateEvent(final Point point, final TileType terrain,
	                            final Stream<TileFixture> fixtures) {
		return fixtures.filter(IEvent.class::isInstance).map(IEvent.class::cast)
				       .map(IEvent::getText).findFirst()
				       .orElse("Nothing interesting here ...");
	}

	/**
	 * @return all events this table can generate.
	 */
	@Override
	public Set<String> allEvents() {
		return new HashSet<>(data);
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "LegacyTable";
	}
}
