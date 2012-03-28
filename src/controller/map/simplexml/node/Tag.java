package controller.map.simplexml.node;


/**
 * The kinds of tags the Factory knows how to parse. Note that multiple XML tags
 * can get mapped to a single enumerated tag-type.
 * 
 * @author kingjon
 * 
 */
public enum Tag {
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
	@Deprecated
	Event(EventNode.class),
	/**
	 * A battlefield.
	 */
	Battlefield(BattlefieldEventNode.class),
	/**
	 * A cave.
	 */
	Cave(CaveEventNode.class),
	/**
	 * A city.
	 */
	City(TownEventNode.class),
	/**
	 * A fortification. FIXME: Again, we want this to use the Fortress tag
	 * instead, eventually.
	 */
	Fortification(TownEventNode.class),
	/**
	 * A stone deposit.
	 */
	Stone(StoneEventNode.class),
	/**
	 * A non-stone mineral deposit.
	 */
	Mineral(MineralEventNode.class),
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
	 * A fairy.
	 */
	Fairy(FairyNode.class),
	/**
	 * A djinn.
	 */
	Djinn(DjinnNode.class),
	/**
	 * A dragon.
	 */
	Dragon(DragonNode.class),
	/**
	 * A giant.
	 */
	Giant(GiantNode.class),
	/**
	 * A griffin.
	 */
	Griffin(GriffinNode.class),
	/**
	 * A minotaur.
	 */
	Minotaur(MinotaurNode.class),
	/**
	 * An ogre.
	 */
	Ogre(OgreNode.class),
	/**
	 * A phoenix.
	 */
	Phoenix(PhoenixNode.class),
	/**
	 * A simurgh.
	 */
	Simurgh(SimurghNode.class),
	/**
	 * A sphinx.
	 */
	Sphinx(SphinxNode.class),
	/**
	 * A troll.
	 */
	Troll(TrollNode.class),
	/**
	 * A town.
	 */
	Town(TownEventNode.class);
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
