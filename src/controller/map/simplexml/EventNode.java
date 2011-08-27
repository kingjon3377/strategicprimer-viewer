package controller.map.simplexml;

import java.util.Iterator;

import model.viewer.PlayerCollection;
import model.viewer.events.AbstractEvent;

/**
 * A Node that will produce an Event.
 * 
 * @see AbstractEvent
 * @author Jonathan Lovelace
 * 
 */
public class EventNode extends AbstractChildNode<AbstractEvent> implements
		NeedsExtraCanonicalization {
	/**
	 * The property of an Event saying what kind of event it is.
	 */
	private static final String KIND_PROPERTY = "kind";

	/**
	 * Produce the equivalent Event.
	 * 
	 * @param players
	 *            ignored
	 * @return the equivalent event
	 * @throws SPFormatException
	 *             if this Node contains invalid data.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public AbstractEvent produce(final PlayerCollection players)
			throws SPFormatException {
		return ((AbstractChildNode<? extends AbstractEvent>) iterator().next())
				.produce(players);
	}

	/**
	 * Check that this Node contains entirely valid data. An Event is valid if
	 * it has no children (thus towns, etc., shouldn't be Events much longer)
	 * and has a DC property. Additionally, town-related events must have size
	 * and status properties, minerals must have mineral and exposed properties,
	 * and stone events must have a "stone" property. For forward compatibility,
	 * we do not object to unknown properties.
	 * 
	 * @throws SPFormatException
	 *             if it contains any invalid data.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		final Iterator<AbstractXMLNode> iter = iterator();
		if (!iter.hasNext()) {
			throw new SPFormatException(
					"Doesn't have the child that actually holds the Event",
					getLine());
		}
		iter.next().checkNode();
		if (iter.hasNext()) {
			throw new SPFormatException(
					"EventNode shouldn't have more than the one child",
					getLine());
		}
	}

	/**
	 * Convert this node into a wrapper around a more specific Node.
	 * 
	 * @throws SPFormatException
	 *             on format error uncovered by this process
	 */
	@Override
	public void canonicalizeImpl() throws SPFormatException {
		// ESCA-JAVA0177:
		final AbstractChildNode<? extends AbstractEvent> child; // NOPMD
		if ("battlefield".equals(getProperty(KIND_PROPERTY))) {
			child = new BattlefieldEventNode();
		} else if ("cave".equals(getProperty(KIND_PROPERTY))) {
			child = new CaveEventNode();
		} else if ("city".equals(getProperty(KIND_PROPERTY))
				|| "fortification".equals(getProperty(KIND_PROPERTY))
				|| "town".equals(getProperty(KIND_PROPERTY))) {
			child = new TownEventNode();
		} else if ("mineral".equals(getProperty(KIND_PROPERTY))) {
			child = new MineralEventNode();
		} else if ("stone".equals(getProperty(KIND_PROPERTY))) {
			child = new StoneEventNode();
		} else if (hasProperty(KIND_PROPERTY)) {
			throw new SPFormatException("Unknown kind of event", getLine());
		} else {
			throw new SPFormatException("Event must have a \"kind\" property",
					getLine());
		}
		moveEverythingTo(child);
		addChild(child);
	}

}
