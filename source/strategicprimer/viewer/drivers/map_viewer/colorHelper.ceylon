import ceylon.language.meta {
    type
}
import java.awt {
    Color
}
import strategicprimer.model.common.map.fixtures.terrain {
    Oasis,
    Hill,
    Forest
}
import strategicprimer.model.common.map {
    TileType,
    TileFixture
}
import ceylon.language.meta.model {
    ClassOrInterface
}
import lovelace.util.common {
    simpleMap
}

"An object encapsulating the mapping from tile-types to colors."
object colorHelper {
    String wrap(String wrapped) => "<html><p>``wrapped``</p></html>";
    "Descriptions of the types."
    Map<TileType, String> descriptions = simpleMap(
        TileType.desert->wrap("Desert"),
        TileType.jungle->wrap("Jungle"),
        TileType.ocean->wrap("Ocean"),
        TileType.plains->wrap("Plains"),
        TileType.tundra->wrap("Tundra"),
        TileType.steppe->wrap("Steppe"),
        TileType.swamp->wrap("Swamp")
    );

    "A map from types of features to the colors they can make the tile be. Used to
      show that a tile is forested, e.g., even when that is normally represented by
       an icon and there's a higher icon on the tile."
    Map<ClassOrInterface<TileFixture>, Color> featureColors = simpleMap(
        `Forest`->Color(0, 117, 0),
        `Oasis`->Color(72, 218, 164),
        `Hill`->Color(141, 182, 0)
    );

    "A map from map versions to maps from tile-types to colors."
    Map<Integer, Map<TileType, Color>> colors = simpleMap(
        2->simpleMap(
            TileType.desert->Color(249, 233, 28),
            TileType.jungle->Color(229, 46, 46),
            TileType.ocean->Color.\iBLUE,
            TileType.plains->Color(72, 218, 164),
            TileType.tundra->Color(153, 153, 153),
            TileType.steppe->Color(72, 100, 72),
            TileType.swamp->Color(231,41,138)
        )
    );

    "Whether the given map version supports the given tile type."
    shared Boolean supportsType(Integer version, TileType type) {
        if (exists map = colors[version], exists color = map[type]) {
            return true;
        } else {
            return false;
        }
    }

    "Get the color to use for the given tile type in the given map version. Throws
     if the given version does not support that tile type."
    shared Color? get(Integer version, TileType? type) {
        if (exists type) {
            if (exists map = colors[version]) {
                if (exists color = map[type]) {
                    return color;
                } else {
                    log.error("Asked for unsupported type ``type`` in version ``
                        version``");
                    return null;
                }
            } else {
                log.error("Asked for ``type`` in unsupported version ``version``");
                return null;
            }
        } else {
            return Color.white;
        }
    }

    "Get a String (HTML) representation of the given terrain type."
    shared String? getDescription(TileType? type) {
        if (exists type) {
            if (exists retval = descriptions[type]) {
                return retval;
            } else {
                log.error("No description found for tile type ``type``");
                return null;
            }
        } else {
            return "Unknown";
        }
    }

    "Get the color that a fixture should turn the tile if it's not on top."
    shared Color? getFeatureColor(TileFixture fixture) {
        if (exists color = featureColors[type(fixture)]) {
            return color;
        } else {
            log.warn("Asked for color for unsupported fixture: ``fixture``");
            return null;
        }
    }
    "The color to use for background mountains."
    shared Color mountainColor = Color(249, 137, 28);
}
