package controller.map.simplexml;

import controller.map.simplexml.node.AbstractChildNode;
import controller.map.simplexml.node.AnimalNode;
import controller.map.simplexml.node.CacheNode;
import controller.map.simplexml.node.CentaurNode;
import controller.map.simplexml.node.EventNode;
import controller.map.simplexml.node.ForestNode;
import controller.map.simplexml.node.FortressNode;
import controller.map.simplexml.node.GroundNode;
import controller.map.simplexml.node.GroveNode;
import controller.map.simplexml.node.HillNode;
import controller.map.simplexml.node.MapNode;
import controller.map.simplexml.node.MeadowNode;
import controller.map.simplexml.node.MineNode;
import controller.map.simplexml.node.MountainNode;
import controller.map.simplexml.node.OasisNode;
import controller.map.simplexml.node.PlayerNode;
import controller.map.simplexml.node.RiverNode;
import controller.map.simplexml.node.SandbarNode;
import controller.map.simplexml.node.ShrubNode;
import controller.map.simplexml.node.SkippableNode;
import controller.map.simplexml.node.TextNode;
import controller.map.simplexml.node.TileNode;
import controller.map.simplexml.node.UnitNode;
import controller.map.simplexml.node.VillageNode;

/**
 * The kinds of tags the Factory knows how to parse. Note that multiple XML tags
 * can get mapped to a single enumerated tag-type.
 * 
 * @author kingjon
 * 
 */
enum Tag {
	/**
	 * The main map tag.
	 */
	Map(MapNode.class),
	/**
	 * Row and column tags, and perhaps others we'd rather ignore.
	 */
	Skippable(SkippableNode.class),
	/**
	 * An individual tile.
	 */
	Tile(TileNode.class),
	/**
	 * A player.
	 */
	Player(PlayerNode.class),
	/**
	 * A fortress.
	 */
	Fortress(FortressNode.class),
	/**
	 * A unit.
	 */
	Unit(UnitNode.class),
	/**
	 * A river.
	 */
	River(RiverNode.class),
	/**
	 * A lake. Since lakes have no attributes, we can't make them just a special
	 * case of rivers at this stage without just saying that any river tag
	 * without a direction is a lake.
	 */
	Lake(RiverNode.class),
	/**
	 * An Event.
	 */
	Event(EventNode.class),
	/**
	 * A battlefield.
	 */
	Battlefield(EventNode.class),
	/**
	 * A cave.
	 */
	Cave(EventNode.class),
	/**
	 * A city.
	 */
	City(EventNode.class),
	/**
	 * A fortification. FIXME: Again, we want this to use the Fortress tag
	 * instead, eventually.
	 */
	Fortification(EventNode.class),
	/**
	 * A stone deposit.
	 */
	Stone(EventNode.class),
	/**
	 * A non-stone mineral deposit.
	 */
	Mineral(EventNode.class),
	/**
	 * A forest.
	 */
	Forest(ForestNode.class),
	/**
	 * A mountain or mountainous region.
	 */
	Mountain(MountainNode.class),
	/**
	 * The ground (rock) of a tile.
	 */
	Ground(GroundNode.class),
	/**
	 * A shrub or patch of shrubs.
	 */
	Shrub(ShrubNode.class),
	/**
	 * An oasis.
	 */
	Oasis(OasisNode.class),
	/**
	 * A grove or orchard. (An orchard is fruit trees, a grove is other trees.)
	 */
	Grove(GroveNode.class),
	/**
	 * A mine.
	 */
	Mine(MineNode.class),
	/**
	 * An animal or group of animals.
	 */
	Animal(AnimalNode.class),
	/**
	 * A meadow or field. (A meadow is grass, and in forest.)
	 */
	Meadow(MeadowNode.class),
	/**
	 * A hill.
	 */
	Hill(HillNode.class),
	/**
	 * A village.
	 */
	Village(VillageNode.class),
	/**
	 * A (resource) cache (of vegetables, a hidden treasure, ...) on the tile.
	 */
	Cache(CacheNode.class),
	/**
	 * A sandbar.
	 */
	Sandbar(SandbarNode.class),
	/**
	 * Arbitrary text. (That's being moved from the tile itself into fixtures.)
	 */
	Text(TextNode.class),
	/**
	 * A centaur.
	 */
	Centaur(CentaurNode.class),
	/**
	 * A town.
	 */
	Town(EventNode.class);
	/**
	 * Constructor.
	 * @param tclass the Class of the kind of Node that this tag indicates.
	 */
	private Tag(final Class<? extends AbstractChildNode<?>> tclass) {
		tagClass = tclass;
	}
	/**
	 * The kind of Node this tag indicates.
	 */
	private final Class<? extends AbstractChildNode<?>> tagClass;
	/**
	 * @return the kind of Node this tag indicates.
	 */
	public Class<? extends AbstractChildNode<?>> getTagClass() {
		return tagClass;
	}
}
