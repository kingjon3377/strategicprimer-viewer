package model.exploration.old;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.map.IEvent;
import model.map.Player;
import model.map.Point;
import model.map.Tile;
import model.map.TileFixture;
import model.map.fixtures.resources.Battlefield;
import model.map.fixtures.resources.Cave;
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
 * @author Jonathan Lovelace
 */
@SuppressWarnings("deprecation")
public class LegacyTable implements EncounterTable {
	/**
	 * The list of events we can return.
	 */
	private final List<String> data;

	/**
	 * Add the text from an Event to the list.
	 *
	 * @param event the event to get the text from
	 */
	private void addData(final IEvent event) {
		data.add(event.toString());
	}

	/**
	 * Constructor.
	 */
	public LegacyTable() {
		final Player independent = new Player(-1, "Independent");
		data = new ArrayList<>();
		addData(new Battlefield(0, -1));
		addData(new Cave(0, -1));
		for (final TownStatus status : TownStatus.values()) {
			for (final TownSize size : TownSize.values()) {
				addData(new City(status, size, 0, "", 0, independent)); // NOPMD
				addData(new Fortification(status, size, 0, "", 0, independent)); // NOPMD
				addData(new Town(status, size, 0, "", 0, independent)); // NOPMD
			}
		}
		for (final MineralKind mineral : MineralKind.values()) {
			addData(new MineralVein(mineral.toString(), true, 0, 0)); // NOPMD
			addData(new MineralVein(mineral.toString(), false, 0, 0)); // NOPMD
		}
		data.add("Nothing interesting here ...");
		for (final StoneKind stone : StoneKind.values()) {
			if (stone != null) {
				addData(new StoneDeposit(stone, 0, 0)); // NOPMD
			}
		}
	}

	/**
	 * @param tile the tile in question
	 * @param point the tile's location
	 * @return the event on that tile
	 */
	@Override
	public String generateEvent(final Point point, final Tile tile) {
		for (final TileFixture fix : tile) {
			if (fix instanceof IEvent) {
				return ((IEvent) fix).getText(); // NOPMD
			}
		}
		return "Nothing interesting here ...";
	}

	/**
	 * @return all events this table can generate.
	 */
	@Override
	public Set<String> allEvents() {
		return new HashSet<>(data);
	}

	/**
	 *
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "LegacyTable";
	}
}
