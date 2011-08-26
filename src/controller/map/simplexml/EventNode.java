package controller.map.simplexml;

import model.viewer.events.AbstractEvent;
import model.viewer.events.AbstractEvent.TownSize;
import model.viewer.events.AbstractEvent.TownStatus;
import model.viewer.events.BattlefieldEvent;
import model.viewer.events.CaveEvent;
import model.viewer.events.CityEvent;
import model.viewer.events.FortificationEvent;
import model.viewer.events.MineralEvent;
import model.viewer.events.MineralEvent.MineralKind;
import model.viewer.events.StoneEvent;
import model.viewer.events.StoneEvent.StoneKind;
import model.viewer.events.TownEvent;

/**
 * A Node that will produce an Event.
 * @see AbstractEvent
 * @author Jonathan Lovelace
 *
 */
public class EventNode extends AbstractChildNode<AbstractEvent> {
	/**
	 * The property of an Event saying what kind of event it is.
	 */
	private static final String KIND_PROPERTY = "kind";
	/**
	 * Produce the equivalent Event.
	 * @return the equivalent event
	 * @throws SPFormatException if this Node contains invalid data.
	 */
	@Override
	public AbstractEvent produce() throws SPFormatException {
		if ("battlefield".equals(getProperty(KIND_PROPERTY))) {
			return new BattlefieldEvent(Integer.parseInt(getProperty("dc")));
		} else if ("caves".equals(getProperty(KIND_PROPERTY))) {
			return new CaveEvent(Integer.parseInt(getProperty("dc")));
		} else if ("city".equals(getProperty(KIND_PROPERTY))) {
			return new CityEvent(
					TownStatus.parseTownStatus(getProperty("status")),
					TownSize.parseTownSize(getProperty("size")),
					Integer.parseInt(getProperty("dc")));
		} else if ("fortification".equals(getProperty(KIND_PROPERTY))) {
			return new FortificationEvent(
					TownStatus.parseTownStatus(getProperty("status")),
					TownSize.parseTownSize(getProperty("size")),
					Integer.parseInt(getProperty("dc")));
		} else if ("town".equals(getProperty(KIND_PROPERTY))) {
			return new TownEvent(
					TownStatus.parseTownStatus(getProperty("status")),
					TownSize.parseTownSize(getProperty("size")),
					Integer.parseInt(getProperty("dc")));
		} else if ("mineral".equals(getProperty(KIND_PROPERTY))) {
			return new MineralEvent(MineralKind.parseMineralKind(getProperty("mineral")),
					Boolean.parseBoolean(getProperty("exposed")),
					Integer.parseInt(getProperty("dc")));
		} else if ("stone".equals(getProperty(KIND_PROPERTY))) {
			return new StoneEvent(
					StoneKind.parseStoneKind(getProperty("stone")),
					Integer.parseInt(getProperty("dc")));
		} else {
			throw new SPFormatException("Unknown kind of event", getLine());
		}
	}
	/**
	 * Check that this Node contains entirely valid data. An Event is valid if
	 * it has no children (thus towns, etc., shouldn't be Events much longer) and 
	 * has a DC property. Additionally, town-related events must have size and 
	 * status properties, minerals must have mineral and exposed properties, and 
	 * stone events must have stone and exposed properties. For forward compatibility, 
	 * we do not object to unknown properties.   
	 * 
	 * @throws SPFormatException if it contains any invalid data.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Event shouldn't have children", getLine());
		} else if (hasProperty(KIND_PROPERTY) && hasProperty("dc")) {
			if (("town".equals(getProperty(KIND_PROPERTY))
					|| "fortification".equals(getProperty(KIND_PROPERTY)) || "city"
						.equals(getProperty(KIND_PROPERTY)))
					&& (!hasProperty("size") || !hasProperty("status"))) {
				throw new SPFormatException(
						"Town-related events must have \"size\" and \"status\" properties",
						getLine());
			} else if ("mineral".equals(getProperty(KIND_PROPERTY))
					&& (!hasProperty("mineral") || !hasProperty("exposed"))) {
				throw new SPFormatException(
						"Mineral events must have \"mineral\" and \"exposed\" properties.",
						getLine());
			} else if ("stone".equals(getProperty(KIND_PROPERTY))
					&& (!hasProperty("stone") || !hasProperty("exposed"))) {
				throw new SPFormatException(
						"Stone events must have \"stone\" and \"exposed\" properties.",
						getLine());
			}
		} else {
			throw new SPFormatException("Event must have \"kind\" and \"dc\" properties", getLine());
		}
	}

}
