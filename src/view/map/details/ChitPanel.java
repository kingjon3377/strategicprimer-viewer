package view.map.details;

import javax.swing.JPanel;

import model.map.HasImage;
import model.map.TileFixture;
import model.map.events.IEvent;
import model.map.fixtures.Fortress;
import model.map.fixtures.Ground;
import model.map.fixtures.RiverFixture;
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
	 * Add a chit for a TileFixture. TODO: Should a CacheFixture actually have
	 * its own Chit with a tool-tip advising that the cache's contents were most
	 * likely removed by the explorer?
	 * 
	 * @param fix
	 *            the Fixture to add a chit for
	 */
	public void add(final TileFixture fix) {
		if (fix instanceof Fortress) {
			add(new FortChit((Fortress) fix, listener));
		} else if (fix instanceof Unit) {
			add(new UnitChit((Unit) fix, listener));
		} else if (fix instanceof RiverFixture) {
			add(new RiverChit((RiverFixture) fix, listener));
		} else if (fix instanceof Ground) {
			add(new GroundChit((Ground) fix, listener));
		} else if (fix instanceof HasImage) {
			add(new SimpleChit(fix, listener));
		} else if (fix instanceof IEvent) {
			add(new EventChit((IEvent) fix, listener));
		} else {
			throw new IllegalStateException("We're missing a case here: " + fix.getClass().getSimpleName());
		}
	}
}
