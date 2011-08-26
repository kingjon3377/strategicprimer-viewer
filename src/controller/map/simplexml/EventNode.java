package controller.map.simplexml;

import model.viewer.PlayerCollection;
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
	 * A stone event.
	 */
	private static final String STONE_KIND = "stone";
	/**
	 * A mineral event.
	 */
	private static final String MINERAL_KIND = "mineral";
	/**
	 * The property of a town-like event saying how big it is.
	 */
	private static final String SIZE_PROPERTY = "size";
	/**
	 * The property of a town-like event saying what its status is.
	 */
	private static final String STATUS_PROP = "status";
	/**
	 * The property of an event saying how difficult it is to find it.
	 */
	private static final String DC_PROPERTY = "dc";
	/**
	 * The property of an Event saying what kind of event it is.
	 */
	private static final String KIND_PROPERTY = "kind";
	/**
	 * Produce the equivalent Event.
	 * @param players ignored
	 * @return the equivalent event
	 * @throws SPFormatException if this Node contains invalid data.
	 */
	@Override
	public AbstractEvent produce(final PlayerCollection players) throws SPFormatException {
		// ESCA-JAVA0177:
		final AbstractEvent event; // NOPMD
		if ("battlefield".equals(getProperty(KIND_PROPERTY))) {
			event = new BattlefieldEvent(Integer.parseInt(getProperty(DC_PROPERTY)));
		} else if ("cave".equals(getProperty(KIND_PROPERTY))) {
			event = new CaveEvent(Integer.parseInt(getProperty(DC_PROPERTY)));
		} else if ("city".equals(getProperty(KIND_PROPERTY))) {
			event = new CityEvent(
					TownStatus.parseTownStatus(getProperty(STATUS_PROP)),
					TownSize.parseTownSize(getProperty(SIZE_PROPERTY)),
					Integer.parseInt(getProperty(DC_PROPERTY)));
		} else if ("fortification".equals(getProperty(KIND_PROPERTY))) {
			event = new FortificationEvent(
					TownStatus.parseTownStatus(getProperty(STATUS_PROP)),
					TownSize.parseTownSize(getProperty(SIZE_PROPERTY)),
					Integer.parseInt(getProperty(DC_PROPERTY)));
		} else if ("town".equals(getProperty(KIND_PROPERTY))) {
			event = new TownEvent(
					TownStatus.parseTownStatus(getProperty(STATUS_PROP)),
					TownSize.parseTownSize(getProperty(SIZE_PROPERTY)),
					Integer.parseInt(getProperty(DC_PROPERTY)));
		} else if (MINERAL_KIND.equals(getProperty(KIND_PROPERTY))) {
			event = new MineralEvent(MineralKind.parseMineralKind(getProperty("mineral")),
					Boolean.parseBoolean(getProperty("exposed")),
					Integer.parseInt(getProperty(DC_PROPERTY)));
		} else if (STONE_KIND.equals(getProperty(KIND_PROPERTY))) {
			event = new StoneEvent(
					StoneKind.parseStoneKind(getProperty("stone")),
					Integer.parseInt(getProperty(DC_PROPERTY)));
		} else {
			throw new SPFormatException("Unknown kind of event", getLine());
		}
		return event;
	}
	/**
	 * Check that this Node contains entirely valid data. An Event is valid if
	 * it has no children (thus towns, etc., shouldn't be Events much longer) and 
	 * has a DC property. Additionally, town-related events must have size and 
	 * status properties, minerals must have mineral and exposed properties, and 
	 * stone events must have a "stone" property. For forward compatibility, 
	 * we do not object to unknown properties.   
	 * 
	 * @throws SPFormatException if it contains any invalid data.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Event shouldn't have children", getLine());
		} else if (hasProperty(KIND_PROPERTY) && hasProperty(DC_PROPERTY)) {
			if (("town".equals(getProperty(KIND_PROPERTY))
					|| "fortification".equals(getProperty(KIND_PROPERTY)) || "city"
						.equals(getProperty(KIND_PROPERTY)))
					&& (!hasProperty(SIZE_PROPERTY) || !hasProperty(STATUS_PROP))) {
				throw new SPFormatException(
						"Town-related events must have \"size\" and \"status\" properties",
						getLine());
			} else if (MINERAL_KIND.equals(getProperty(KIND_PROPERTY))
					&& (!hasProperty("mineral") || !hasProperty("exposed"))) {
				throw new SPFormatException(
						"Mineral events must have \"mineral\" and \"exposed\" properties.",
						getLine());
			} else if (STONE_KIND.equals(getProperty(KIND_PROPERTY))
					&& !hasProperty("stone")) {
				throw new SPFormatException(
						"Stone events must have \"stone\" property.",
						getLine());
			}
		} else {
			throw new SPFormatException("Event must have \"kind\" and \"dc\" properties", getLine());
		}
	}

}
