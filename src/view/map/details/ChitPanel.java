package view.map.details;

import javax.swing.JPanel;

import model.map.Fortress;
import model.map.TileFixture;
import model.map.Unit;
import model.map.events.AbstractEvent;
import view.map.main.SelectionListener;
/**
 * A panel for displaying chits.
 * @author Jonathan Lovelace
 *
 */
public class ChitPanel extends JPanel {
	/**
	 * Constructor.
	 * @param list the selection listener.
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
	 * @param fix the Fixture to add a chit for
	 */
	public void add(final TileFixture fix) {
		if (fix instanceof Fortress) {
			add(new FortChit((Fortress) fix, listener));
		} else if (fix instanceof Unit) {
			add(new UnitChit((Unit) fix, listener));
		} else if (fix instanceof AbstractEvent) {
			add(new EventChit((AbstractEvent) fix, listener));
		}
	}
}
