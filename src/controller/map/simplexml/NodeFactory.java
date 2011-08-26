package controller.map.simplexml;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;

/**
 * A class to create properly-typed Nodes (but *not* their contents) based on
 * the tags that represent them.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class NodeFactory { // NOPMD
	/**
	 * The name for the property for what kind of event an event is. We create
	 * this property when setting up nodes for events that had their own unique
	 * tags.
	 */
	private static final String EVENT_KIND_PROP = "kind";

	/**
	 * Do not instantiate.
	 */
	private NodeFactory() {
		// Do nothing.
	}

	/**
	 * A mapping from XML tags to tag types.
	 */
	private static final Map<String, Tag> TAGS = new HashMap<String, Tag>();

	/**
	 * The kinds of tags the Factory knows how to parse. Note that multiple XML
	 * tags can get mapped to a single enumerated tag-type.
	 * 
	 * @author kingjon
	 * 
	 */
	private enum Tag {
		/**
		 * The main map tag.
		 */
		Map,
		/**
		 * Row and column tags, and perhaps others we'd rather ignore.
		 */
		Skippable,
		/**
		 * An individual tile.
		 */
		Tile,
		/**
		 * A player.
		 */
		Player,
		/**
		 * A fortress.
		 */
		Fortress,
		/**
		 * A unit.
		 */
		Unit,
		/**
		 * A river.
		 */
		River,
		/**
		 * A lake. Since lakes have no attributes, we can't make them just a
		 * special case of rivers at this stage without just saying that any
		 * river tag without a direction is a lake.
		 */
		Lake,
		/**
		 * An Event.
		 */
		Event,
		/**
		 * A battlefield.
		 */
		Battlefield,
		/**
		 * A cave.
		 */
		Cave,
		/**
		 * A city.
		 */
		City,
		/**
		 * A fortification. FIXME: Again, we want this to use the Fortress tag
		 * instead, eventually.
		 */
		Fortification,
		/**
		 * A stone deposit.
		 */
		Stone,
		/**
		 * A non-stone mineral deposit.
		 */
		Mineral,
		/**
		 * A town.
		 */
		Town;
	}

	/**
	 * Set up a tag.
	 * 
	 * @param string
	 *            an XML tag
	 * @param tag
	 *            a corresponding tag category
	 */
	private static void addTag(final String string, final Tag tag) {
		TAGS.put(string, tag);
	}

	static {
		addTag("map", Tag.Map);
		addTag("row", Tag.Skippable);
		addTag("tile", Tag.Tile);
		addTag("player", Tag.Player);
		addTag("fortress", Tag.Fortress);
		addTag("unit", Tag.Unit);
		addTag("river", Tag.River);
		addTag("lake", Tag.Lake);
		addTag("event", Tag.Event);
		addTag("battlefield", Tag.Battlefield);
		addTag("cave", Tag.Cave);
		addTag("city", Tag.City);
		addTag("fortification", Tag.Fortification);
		addTag("stone", Tag.Stone);
		addTag("mineral", Tag.Mineral);
		addTag("town", Tag.Town);
	}

	/**
	 * Create a Node from a tag.
	 * 
	 * @param tag
	 *            the tag.
	 * @param line
	 *            the line of the file it's on ... just in case.
	 * @return a Node representing the tag, but not its contents yet.
	 * @throws SPFormatException
	 *             on unrecognized tag
	 */
	public static AbstractChildNode<?> create(final String tag, final int line) //NOPMD
			throws SPFormatException {
		if (!TAGS.containsKey(tag)) {
			throw new SPFormatException("Unknown tag " + tag, line);
		}
		// ESCA-JAVA0177:
		final AbstractChildNode<?> node; // NOPMD
		// ESCA-JAVA0040:
		switch (TAGS.get(tag)) {
		case Battlefield:
			node = new EventNode();
			node.addProperty(EVENT_KIND_PROP, "battlefield");
			break;
		case Cave:
			node = new EventNode();
			node.addProperty(EVENT_KIND_PROP, "cave");
			break;
		case City:
			node = new EventNode();
			node.addProperty(EVENT_KIND_PROP, "city");
			break;
		case Event:
			node = new EventNode();
			break;
		case Fortification:
			node = new EventNode();
			node.addProperty(EVENT_KIND_PROP, "fortification");
			break;
		case Fortress:
			node = new FortressNode();
			break;
		case Lake:
			node = new RiverNode();
			node.addProperty("direction", "lake");
			break;
		case Map:
			node = new MapNode();
			break;
		case Mineral:
			node = new EventNode();
			node.addProperty(EVENT_KIND_PROP, "mineral");
			break;
		case Player:
			node = new PlayerNode();
			break;
		case River:
			node = new RiverNode();
			break;
		case Skippable:
			throw new NotImplementedException("Skippable not implemented yet");
		case Stone:
			node = new EventNode();
			node.addProperty(EVENT_KIND_PROP, "stone");
			break;
		case Tile:
			node = new TileNode();
			break;
		case Town:
			node = new EventNode();
			node.addProperty(EVENT_KIND_PROP, "town");
			break;
		case Unit:
			throw new NotImplementedException("Unit not implemented yet");
		default:
			throw new IllegalStateException("Shouldn't get here!");
		}
		return node;
	}
}
