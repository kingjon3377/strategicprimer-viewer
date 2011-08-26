package controller.map.simplexml;

import model.viewer.Fortress;

import org.apache.commons.lang.NotImplementedException;
/**
 * A node to produce a Fortress.
 * @author Jonathan Lovelace
 */
public class FortressNode extends AbstractChildNode<Fortress> {
	/**
	 * Produce the equivalent fortress.
	 * @return the equivalent fortress.
	 * @throws SPFormatException if this node contains invalid data.
	 */
	@Override
	public Fortress produce() throws SPFormatException {
		throw new NotImplementedException("Fortress production not implemented yet");
	}
	/**
	 * Check whether we contain valid data.
	 * @throws SPFormatException if we don't.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		throw new NotImplementedException("Fortress validity-checking not implemented yet.");
	}

}
