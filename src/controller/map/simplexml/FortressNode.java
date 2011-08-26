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
	 * Check whether we contain valid data. A Fortress contains valid
	 * data iff every child is a Unit. At present we don't require it 
	 * to have any properties, not even "owner" or "name"; if or when 
	 * that changes, this should change to check those conditions.
	 * @throws SPFormatException if we don't.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		for (AbstractXMLNode node : this) {
			if (node instanceof UnitNode) {
				node.checkNode();
			} else {
				throw new SPFormatException("Fortress should contain only units", getLine());
			}
		}
	}

}
