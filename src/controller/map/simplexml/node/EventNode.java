package controller.map.simplexml.node;

import java.util.Iterator;

import model.map.PlayerCollection;
import model.map.events.IEvent;
import util.Warning;
import controller.map.MissingChildException;
import controller.map.MissingParameterException;
import controller.map.NeedsExtraCanonicalization;
import controller.map.SPFormatException;
import controller.map.UnsupportedTagException;
import controller.map.UnwantedChildException;

/**
 * A Node that will produce an Event.
 * 
 * FIXME: Get rid of this.
 * @see IEvent
 * @author Jonathan Lovelace
 * 
 */
@Deprecated
public class EventNode extends AbstractFixtureNode<IEvent> implements
		NeedsExtraCanonicalization {
	/**
	 * Constructor.
	 */
	public EventNode() {
		super(IEvent.class);
	}
	/**
	 * The property of an Event saying what kind of event it is.
	 */
	private static final String KIND_PROPERTY = "kind";

	/**
	 * Produce the equivalent Event.
	 * 
	 * @param players
	 *            ignored
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @return the equivalent event
	 * @throws SPFormatException
	 *             if this Node contains invalid data.
	 */
	@Override
	public IEvent produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		return ((AbstractChildNode<? extends IEvent>) iterator().next())
				.produce(players, warner);
	}
	/**
	 * @param property the name of a property
	 * @return true---since we'll be moving everything to the implementing child anyway, it can do this check
	 */
	@Override
	public boolean canUse(final String property) {
		return true;
	}
	
	/**
	 * Check that this Node contains entirely valid data. An Event is valid if
	 * it has no children (thus towns, etc., shouldn't be Events much longer)
	 * and has a DC property. Additionally, town-related events must have size
	 * and status properties, minerals must have mineral and exposed properties,
	 * and stone events must have a "stone" property. For forward compatibility,
	 * we do not object to unknown properties.
	 * 
	 * 
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @throws SPFormatException
	 *             if it contains any invalid data.
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		final Iterator<AbstractXMLNode> iter = iterator();
		if (!iter.hasNext()) {
			throw new MissingChildException(getProperty("kind"), getLine());
		}
		iter.next().checkNode(warner);
		if (iter.hasNext()) {
			throw new UnwantedChildException(getProperty("kind"), iter.next()
					.toString(), getLine());
		}
	}

	/**
	 * Convert this node into a wrapper around a more specific Node.
	 * 
	 * 
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @throws SPFormatException
	 *             on format error uncovered by this process
	 */
	@Override
	public void canonicalizeImpl(final Warning warner) throws SPFormatException {
		// ESCA-JAVA0177:
		final AbstractChildNode<? extends IEvent> child; // NOPMD
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
			throw new UnsupportedTagException(getProperty(KIND_PROPERTY), getLine());
		} else {
			throw new MissingParameterException("event", "kind", getLine());
		}
		moveEverythingTo(child, warner);
		addChild(child);
	}

	/**
	 * 
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "EventNode";
	}
}
