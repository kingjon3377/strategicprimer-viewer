package model.exploration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.viewer.Tile;
import model.viewer.TileFixture;
import model.viewer.events.AbstractEvent;
import model.viewer.events.AbstractEvent.TownSize;
import model.viewer.events.AbstractEvent.TownStatus;
import model.viewer.events.BattlefieldEvent;
import model.viewer.events.CaveEvent;
import model.viewer.events.CityEvent;
import model.viewer.events.FortificationEvent;
import model.viewer.events.MineralEvent;
import model.viewer.events.MineralEvent.MineralKind;
import model.viewer.events.NothingEvent;
import model.viewer.events.StoneEvent;
import model.viewer.events.StoneEvent.StoneKind;
import model.viewer.events.TownEvent;

/**
 * A table for legacy "events".
 * 
 * @author Jonathan Lovelace
 */
public class LegacyTable implements EncounterTable {
	/**
	 * The list of events we can return.
	 */
	private final List<String> data;
	/**
	 * Constructor.
	 * 
	 * @param entryMap
	 *            the entries in the table.
	 */
	public LegacyTable(final Map<Integer, String> entryMap) {
		data = new ArrayList<String>();
		data.add(new BattlefieldEvent(0).getText());
		data.add(new CaveEvent(0).getText());
		for (TownStatus status : TownStatus.values()) {
			for (TownSize size : TownSize.values()) {
				data.add(new CityEvent(status, size, 0).getText()); // NOPMD
				data.add(new FortificationEvent(status, size, 0).getText()); // NOPMD
				data.add(new TownEvent(status, size, 0).getText()); // NOPMD
			}
		}
		for (MineralKind mineral : MineralKind.values()) {
			data.add(new MineralEvent(mineral, true, 0).getText()); // NOPMD
			data.add(new MineralEvent(mineral, false, 0).getText()); // NOPMD
		}
		data.add(NothingEvent.NOTHING_EVENT.getText());
		for (StoneKind stone : StoneKind.values()) {
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
		for (TileFixture fix : tile.getContents()) {
			if (fix instanceof AbstractEvent) {
				return ((AbstractEvent) fix).getText(); // NOPMD
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

}
