package legacy.map;

import lovelace.util.EnumParser;
import lovelace.util.ThrowingFunction;

import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Collectors;

/**
 * Possible tile types.
 *
 * TODO: Other additional types for use in other worlds' maps?
 */
public enum TileType implements HasName {
	/**
	 * Tundra.
	 */
	Tundra("tundra", "tundra", 2),

	/**
	 * Desert.
	 */
	Desert("desert", "desert", 2),

	/**
	 * Ocean, or water more generally.
	 */
	Ocean("ocean", "ocean", 2),

	/**
	 * Plains.
	 */
	Plains("plains", "plains", 2),

	/**
	 * Jungle.
	 */
	Jungle("jungle", "jungle", 2),

	/**
	 * Steppe. This is like plains, but higher-latitude and colder.
	 * Beginning in version 2, a temperate forest is plains plus forest,
	 * and a boreal forest is steppe plus forest, while a mountain tile is
	 * either a desert, a plain, or a steppe that is mountainous.
	 */
	Steppe("steppe", "steppe", 2),

	/**
	 * Swamp.
	 */
	Swamp("swamp", "swamp", 2);

	/**
	 * A description of the instance, for human consumption
	 */
	private final String string;

	/**
	 * A description of the instance, for human consumption
	 */
	@Override
	public String toString() {
		return string;
	}

	/**
	 * How to represent the instance in XML
	 */
	private final String xml;

	/**
	 * How to represent the instance in XML
	 */
	public String getXml() {
		return xml;
	}

	/**
	 * The map versions that support the tile type as such. (For example,
	 * versions 2 and later replace forests as a tile type with forests as
	 * something on the tile.)
	 */
	private final Set<Integer> versions = new HashSet<>();

	/**
	 * The map versions that support the tile type as such. (For example,
	 * versions 2 and later replace forests as a tile type with forests as
	 * something on the tile.)
	 */
	public Collection<Integer> getVersions() {
		return Collections.unmodifiableSet(versions);
	}

	TileType(final String desc, final String xmlDesc, final int... vers) {
		string = desc;
		xml = xmlDesc;
		for (final int ver : vers) {
			versions.add(ver);
		}
	}

	/**
	 * Whether this the given map version supports this tile type.
	 */
	public boolean isSupportedByVersion(final int version) {
		return versions.contains(version);
	}

	/**
	 * A description of the instance, for human consumption
	 */
	@Override
	public String getName() {
		return string;
	}

	/**
	 * All tile types the given version supports.
	 *
	 * TODO: Write tests for this
	 */
	public static Iterable<TileType> getValuesForVersion(final int version) {
		return Stream.of(values()).filter((t) -> t.isSupportedByVersion(version)).collect(Collectors.toList());
	}

	private static final ThrowingFunction<String, TileType, IllegalArgumentException> PARSER =
			new EnumParser<>(TileType.class, values().length, TileType::getXml);

	/**
	 * Parse an XML representation of a tile type.
	 */
	public static TileType parse(final String xml) throws ParseException {
		return PARSER.apply(xml);
	}
}
