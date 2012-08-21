package model.exploration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.map.Tile;
import model.map.TileFixture;
import model.map.events.BattlefieldEvent;
import model.map.events.CaveEvent;
import model.map.events.CityEvent;
import model.map.events.FortificationEvent;
import model.map.events.IEvent;
import model.map.events.MineralEvent;
import model.map.events.MineralKind;
import model.map.events.StoneEvent;
import model.map.events.StoneKind;
import model.map.events.TownEvent;
import model.map.events.TownSize;
import model.map.events.TownStatus;

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
	 * @param event the event to get the text from
	 */
	private void addData(final IEvent event) {
		data.add(event.toString());
	}
	/**
	 * Constructor.
	 */
	public LegacyTable() {
		data = new ArrayList<String>();
		addData(new BattlefieldEvent(0, -1));
		addData(new CaveEvent(0, -1));
		for (final TownStatus status : TownStatus.values()) {
			for (final TownSize size : TownSize.values()) {
				addData(new CityEvent(status, size, 0, "", 0)); // NOPMD
				addData(new FortificationEvent(status, size, 0, "", 0)); // NOPMD
				addData(new TownEvent(status, size, 0, "", 0)); // NOPMD
			}
		}
		for (final MineralKind mineral : MineralKind.values()) {
			addData(new MineralEvent(mineral.toString(), true, 0, 0)); // NOPMD
			addData(new MineralEvent(mineral.toString(), false, 0, 0)); // NOPMD
		}
		data.add("Nothing interesting here ...");
		for (final StoneKind stone : StoneKind.values()) {
			addData(new StoneEvent(stone, 0, 0)); // NOPMD
		}
	}

	/**
	 * @param tile the tile in question
	 * @return the event on that tile
	 */
	@Override
	public String generateEvent(final Tile tile) {
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
		return new HashSet<String>(data);
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
