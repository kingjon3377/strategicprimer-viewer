package controller.map.simplexml;

import model.viewer.events.AbstractEvent;

import org.apache.commons.lang.NotImplementedException;

/**
 * A Node that will produce an Event.
 * @see AbstractEvent
 * @author Jonathan Lovelace
 *
 */
public class EventNode extends AbstractChildNode<AbstractEvent> {
	/**
	 * Produce the equivalent Event.
	 * @return the equivalent event
	 * @throws SPFormatException if this Node contains invalid data.
	 */
	@Override
	public AbstractEvent produce() throws SPFormatException {
		throw new NotImplementedException("Event production not implemented yet.");
	}
	/**
	 * Check that this Node contains entirely valid data.
	 * @throws SPFormatException if it contains any invalid data.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		throw new NotImplementedException("Event validity checking not implemented yet.");
	}

}
