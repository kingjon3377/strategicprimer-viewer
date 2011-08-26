package controller.map.simplexml;

import model.viewer.Unit;

import org.apache.commons.lang.NotImplementedException;
/**
 * A Node to represent a Unit.
 * @author kingjon
 *
 */
public class UnitNode extends AbstractChildNode<Unit> {
	/**
	 * Produce the equivalent Unit.
	 * @return the equivalent Unit.
	 * @throws SPFormatException if we contain invalid data.
	 */
	@Override
	public Unit produce() throws SPFormatException {
		throw new NotImplementedException("Unit production not yet implemented.");
	}
	/**
	 * Check whether we contain any invalid data.
	 * @throws SPFormatException if we do.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		throw new NotImplementedException("Unit validity-checking not yet implemented.");
	}

}
