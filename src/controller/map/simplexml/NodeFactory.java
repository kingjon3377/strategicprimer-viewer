package controller.map.simplexml;

import java.util.HashMap;
import java.util.Map;

import controller.map.SPFormatException;
import controller.map.simplexml.node.AbstractChildNode;
import controller.map.simplexml.node.EventNode;
import controller.map.simplexml.node.ForestNode;
import controller.map.simplexml.node.FortressNode;
import controller.map.simplexml.node.GroundNode;
import controller.map.simplexml.node.MapNode;
import controller.map.simplexml.node.MountainNode;
import controller.map.simplexml.node.PlayerNode;
import controller.map.simplexml.node.RiverNode;
import controller.map.simplexml.node.ShrubNode;
import controller.map.simplexml.node.SkippableNode;
import controller.map.simplexml.node.TileNode;
import controller.map.simplexml.node.UnitNode;

/**
 * A class to create properly-typed Nodes (but *not* their contents) based on
 * the tags that represent them. TODO: Actually implement <include>---and that
 * will entail making this no longer Singleton.
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

	/**
	 * Tags we expect to use in the future; they are SkippableNodes for now and
	 * we'll warn if they're used.
	 */
	private static final String[] FUTURE = { "include", "worker", "explorer",
			"building", "resource", "animal", "changeset", "change",
			"move", "work", "discover" };
	/**
	 * Set up the mappings from tags to node types. And just in case we didn't
	 * remove a tag from FUTURE, we handle those before the tags we *do* handle.
	 */
	static {
		for (final String string : FUTURE) {
			addTag(string, Tag.Skippable);
		}
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
		addTag("forest", Tag.Forest);
		addTag("mountain", Tag.Mountain);
		addTag("ground", Tag.Ground);
		addTag("shrub", Tag.Shrub);
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
	public static AbstractChildNode<?> create(final String tag, final int line) // NOPMD
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
			node = new SkippableNode(tag, line);
			break;
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
			node = new UnitNode();
			break;
		case Forest:
			node = new ForestNode();
			break;
		case Mountain:
			node = new MountainNode();
			break;
		case Ground:
			node = new GroundNode();
			break;
		case Shrub:
			node = new ShrubNode();
			break;
		default:
			throw new IllegalStateException("Shouldn't get here!");
		}
		node.addProperty("line", Integer.toString(line));
		return node;
	}
}
