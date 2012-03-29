package model.exploration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.map.Tile;
import model.map.TileFixture;
import model.map.events.IEvent;
import model.map.events.BattlefieldEvent;
import model.map.events.CaveEvent;
import model.map.events.CityEvent;
import model.map.events.FortificationEvent;
import model.map.events.MineralEvent;
import model.map.events.MineralKind;
import model.map.events.NothingEvent;
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
	 * Constructor.
	 */
	public LegacyTable() {
		data = new ArrayList<String>();
		data.add(new BattlefieldEvent(0).getText());
		data.add(new CaveEvent(0).getText());
		for (final TownStatus status : TownStatus.values()) {
			for (final TownSize size : TownSize.values()) {
				data.add(new CityEvent(status, size, 0, "").getText()); // NOPMD
				data.add(new FortificationEvent(status, size, 0, "").getText()); // NOPMD
				data.add(new TownEvent(status, size, 0, "").getText()); // NOPMD
			}
		}
		for (final MineralKind mineral : MineralKind.values()) {
			data.add(new MineralEvent(mineral.toString(), true, 0).getText()); // NOPMD
			data.add(new MineralEvent(mineral.toString(), false, 0).getText()); // NOPMD
		}
		data.add(NothingEvent.NOTHING_EVENT.getText());
		for (final StoneKind stone : StoneKind.values()) {
			data.add(new StoneEvent(stone, 0).getText()); // NOPMD
		}
	}

	/**
	 * @param tile
	 *            the tile in question
	 * @return the event on that tile
	 */
	@Override
	public String generateEvent(final Tile tile) {
		for (final TileFixture fix : tile.getContents()) {
			if (fix instanceof IEvent) {
				return ((IEvent) fix).getText(); // NOPMD
			}
		}
		return NothingEvent.NOTHING_EVENT.getText();
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
