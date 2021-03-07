import lovelace.util.common {
    todo,
    matchingValue
}

"Possible tile types."
todo("Other additional types for use in other worlds' maps?")
shared class TileType
        of tundra|desert|ocean|plains|jungle|steppe|swamp satisfies HasName {
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
        versions = vers.sequence();
    }

    "Tundra."
    shared new tundra extends delegate("tundra", "tundra", 2) {}

    "Desert."
    shared new desert extends delegate("desert", "desert", 2) {}

    "Ocean, or water more generally."
    shared new ocean extends delegate("ocean", "ocean", 2) {}

    "Plains."
    shared new plains extends delegate("plains", "plains", 2) {}

    "Jungle."
    shared new jungle extends delegate("jungle", "jungle", 2) {}

    "Steppe. This is like plains, but higher-latitude and colder. Beginning in version
     2, a temperate forest is plains plus forest, and a boreal forest is steppe plus
     forest, while a mountain tile is either a desert, a plain, or a steppe that is
     mountainous."
    shared new steppe extends delegate("steppe", "steppe", 2) {}

    "Swamp."
    shared new swamp extends delegate("swamp", "swamp", 2) {}

    "Whether this the given map version supports this tile type."
    shared Boolean isSupportedByVersion(Integer version) => versions.contains(version);

    "A description of the instance, for human consumption"
    shared actual String name => string;
}

{TileType*} getTypesForVersion(Integer version) =>
        `TileType`.caseValues.filter(shuffle(TileType.isSupportedByVersion)(version));

TileType|ParseException parseTileType(String xml) =>
        `TileType`.caseValues.find(matchingValue(xml, TileType.xml)) else
            ParseException("Failed to parse TileType from '``xml``'");
