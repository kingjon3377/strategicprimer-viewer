import lovelace.util.common {
    todo
}
"Possible tile types."
todo("Ought to include swamp, if not other additional types, for use in other worlds' maps")
suppressWarnings("deprecation")
shared class TileType of tundra|desert|mountain|borealForest|temperateForest|ocean|plains|jungle|steppe|notVisible {
	"All tile types the given version supports."
	todo("Write tests for this")
	shared static {TileType*} valuesForVersion(Integer version) =>
			getTypesForVersion(version);
	"Parse an XML representation of a tile type."
	shared static TileType|ParseException parse(String xml) =>
			parseTileType(xml);
	"A description of the instance, for human consumption"
	shared actual String string;
	"How to represent the instance in XML"
	shared String xml;
	"The map versions that support the tile type as such. (For example, version 2 and
	 later replace forests as a tile type with forests as something on the tile.)"
	{Integer*} versions;
	abstract new delegate(String desc, String xmlDesc, Integer* vers) {
		string = desc;
		xml = xmlDesc;
		versions = {*vers};
	}
	"Tundra."
	shared new tundra extends delegate("tundra", "tundra", 1, 2) {}
	"Desert."
	shared new desert extends delegate("desert", "desert", 1, 2) {}
	"Mountain. Starting in version 2, this is represented in XML as [[plains]],
	 [[steppe]], or [[desert]] plus a mountain on the tile, and in the data model as
	 [[plains]], [[steppe]], or [[desert]] that is additionally mountainous."
	deprecated("Format version 1 only")
	shared new mountain extends delegate("mountain", "mountain", 1) {}
	"Boreal forest. Starting in version 2, this is represented as [[steppe]] with a
	 [[strategicprimer.model.map.fixtures.terrain::Forest]]
	 on the tile."
	deprecated("Format version 1 only")
	shared new borealForest extends delegate("boreal forest", "boreal_forest", 1) {}
	"Temperate forest. Starting in version 2, this is represented as [[plains]] with a
	 [[strategicprimer.model.map.fixtures.terrain::Forest]] on the tile."
	deprecated("Format version 1 only")
	shared new temperateForest
			extends delegate("temperate forest", "temperate_forest", 1) {}
	"Ocean, or water more generally."
	shared new ocean extends delegate("ocean", "ocean", 1, 2) {}
	"Plains."
	shared new plains extends delegate("plains", "plains", 1, 2) {}
	"Jungle."
	shared new jungle extends delegate("jungle", "jungle", 1, 2) {}
	"Steppe. This is like plains, but higher-latitude and colder. Beginning in version
	 2, a temperate forest is plains plus forest, and a boreal forest is steppe plus
	 forest, while [[mountain]] is either a desert, a plain, or a steppe that is
	 mountainous."
	shared new steppe extends delegate("steppe", "steppe", 2) {}
	"Not visible."
	todo("Replace with nullability?")
	shared new notVisible extends delegate("not visible", "not_visible", 1, 2) {}
	"Whether this the given map version supports this tile type."
	shared Boolean isSupportedByVersion(Integer version) => versions.contains(version);
}
{TileType*} getTypesForVersion(Integer version) =>
		`TileType`.caseValues.filter((type) => type.isSupportedByVersion(version));
TileType|ParseException parseTileType(String xml) =>
		`TileType`.caseValues.find((type) => type.xml == xml) else
			ParseException("Failed to parse TileType from '``xml``'");