package view.map.details;

import java.awt.event.MouseListener;

import model.map.fixtures.Shrub;
import model.viewer.FixtureTransferable;

/**
 * A chit to represent a shrub.
 * @author Jonathan Lovelace
 *
 */
public class ShrubChit extends Chit {
	/**
	 * @return a description of the shrub
	 */
	@Override
	public String describe() {
		return desc;
	}
	/**
	 * A description of the shrub.
	 */
	private final String desc;
	/**
	 * Constructor.
	 * @param shrub the shrub this chit represents
	 * @param listener the object listening for clicks on this chit
	 */
	public ShrubChit(final Shrub shrub, final MouseListener listener) {
		super(listener, new FixtureTransferable(shrub));
		desc = "<html><p>" + shrub.getDescription() + "</p></html>";
	}
}
