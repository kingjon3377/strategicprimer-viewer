package controller.map.simplexml;

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
	 * A lake. Since lakes have no attributes, we can't make them just a special
	 * case of rivers at this stage without just saying that any river tag
	 * without a direction is a lake.
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
	 * A forest.
	 */
	Forest,
	/**
	 * A mountain or mountainous region.
	 */
	Mountain,
	/**
	 * The ground (rock) of a tile.
	 */
	Ground,
	/**
	 * A shrub or patch of shrubs.
	 */
	Shrub,
	/**
	 * An oasis.
	 */
	Oasis,
	/**
	 * A grove or orchard. (An orchard is fruit trees, a grove is other trees.)
	 */
	Grove,
	/**
	 * A mine.
	 */
	Mine,
	/**
	 * An animal or group of animals.
	 */
	Animal,
	/**
	 * A meadow or field. (A meadow is grass, and in forest.)
	 */
	Meadow,
	/**
	 * A hill.
	 */
	Hill,
	/**
	 * A village.
	 */
	Village,
	/**
	 * A (resource) cache (of vegetables, a hidden treasure, ...) on the tile.
	 */
	Cache,
	/**
	 * A sandbar.
	 */
	Sandbar,
	/**
	 * A town.
	 */
	Town;
}
