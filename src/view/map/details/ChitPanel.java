package view.map.details;

import javax.swing.JPanel;

import model.map.TileFixture;
import model.map.events.Forest;
import model.map.events.IEvent;
import model.map.events.MineralEvent;
import model.map.events.NothingEvent;
import model.map.events.StoneEvent;
import model.map.fixtures.Fortress;
import model.map.fixtures.Ground;
import model.map.fixtures.Grove;
import model.map.fixtures.Mine;
import model.map.fixtures.Mountain;
import model.map.fixtures.Oasis;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.Shrub;
import model.map.fixtures.Unit;
import view.map.main.SelectionListener;

/**
 * A panel for displaying chits.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class ChitPanel extends JPanel {
	/**
	 * Constructor.
	 * 
	 * @param list
	 *            the selection listener.
	 */
	public ChitPanel(final SelectionListener list) {
		super();
		listener = list;
	}

	/**
	 * The selection listener.
	 */
	private final SelectionListener listener;

	/**
	 * Clear all chits off the panel, and clear the selection.
	 */
	public void clear() {
		listener.clearSelection();
		removeAll();
	}

	/**
	 * Add a chit for a TileFixture.
	 * 
	 * @param fix
	 *            the Fixture to add a chit for
	 */
	public void add(final TileFixture fix) {
		if (fix instanceof Fortress) {
			add(new FortChit((Fortress) fix, listener));
		} else if (fix instanceof Unit) {
			add(new UnitChit((Unit) fix, listener));
		} else if (fix instanceof StoneEvent) {
			add(new StoneChit((StoneEvent) fix, listener));
		} else if (fix instanceof NothingEvent) {
			// NothingEvents represent the absence of all events, and should
			// never actually occur.
			return; // NOPMD
		} else if (fix instanceof MineralEvent) {
			add(new MineralChit((MineralEvent) fix, listener));
		} else if (fix instanceof IEvent) {
			add(new EventChit((IEvent) fix, listener));
		} else if (fix instanceof Forest) { 
			add(new ForestChit((Forest) fix, listener));
		} else if (fix instanceof Mountain) {
			add(new MountainChit((Mountain) fix, listener));
		} else if (fix instanceof RiverFixture) {
			add(new RiverChit((RiverFixture) fix, listener));
		} else if (fix instanceof Ground) {
			add(new GroundChit((Ground) fix, listener));
		} else if (fix instanceof Shrub) {
			add(new ShrubChit((Shrub) fix, listener));
		} else if (fix instanceof Oasis) {
			add(new OasisChit((Oasis) fix, listener));
		} else if (fix instanceof Grove) {
			add(new GroveChit((Grove) fix, listener));
		} else if (fix instanceof Mine) {
			add(new SimpleChit(fix, listener));
		} else {
			throw new IllegalStateException("We're missing a case here");
		}
	}
}
