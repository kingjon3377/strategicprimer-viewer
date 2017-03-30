import ceylon.collection {
    HashMap
}
import java.lang {
    IllegalArgumentException
}
import lovelace.util.common {
    todo
}
import ceylon.language.meta {
    type
}
import java.awt {
    Color
}
import strategicprimer.viewer.model.map.fixtures.terrain {
    Oasis,
    Sandbar,
    Hill,
    Forest
}
import strategicprimer.viewer.model.map {
    TileType
}
import model.map {
    TileFixture
}
import ceylon.language.meta.model {
    ClassOrInterface
}
"An object encapsulating the mapping from tile-types to colors."
object colorHelper {
    String wrap(String wrapped) => "<html<p>``wrapped``</p></html>";
    "Descriptions of the types."
    Map<TileType, String> descriptions = HashMap {
        TileType.borealForest->wrap("Boreal Forest"),
        TileType.desert->wrap("Desert"),
        TileType.jungle->wrap("Jungle"),
        TileType.mountain->wrap("Mountains"),
        TileType.notVisible->wrap("Unknown"),
        TileType.ocean->wrap("Ocean"),
        TileType.plains->wrap("Plains"),
        TileType.temperateForest->wrap("Temperate Forest"),
        TileType.tundra->wrap("Tundra"),
        TileType.steppe->wrap("Steppe")
    };
    "A map from types of features to the colors they can make the tile be. Used to
      show that a tile is forested, e.g., even when that is normally represented by
       an icon and there's a higher icon on the tile."
    Map<ClassOrInterface<TileFixture>, Color> featureColors = HashMap {
        `Forest`->Color(0, 117, 0),
        `Oasis`->Color(72, 218, 164),
        `Sandbar`->Color(249, 233, 28),
        `Hill`->Color(141, 182, 0)
    };
    "A map from map versions to maps from tile-types to colors."
    Map<Integer, Map<TileType, Color>> colors = HashMap {
        1->HashMap {
            TileType.borealForest->Color(72, 218, 164),
            TileType.desert->Color(249, 233, 28),
            TileType.jungle->Color(229, 46, 46),
            TileType.mountain->Color(249, 137, 28),
            TileType.notVisible->Color.white,
            TileType.ocean->Color.\iBLUE,
            TileType.plains->Color(0, 117, 0),
            TileType.temperateForest->Color(72, 250, 72),
            TileType.tundra->Color(153, 153, 153)
        },
        2->HashMap {
            TileType.desert->Color(249, 233, 28),
            TileType.jungle->Color(229, 46, 46),
            TileType.notVisible->Color.white,
            TileType.ocean->Color.\iBLUE,
            TileType.plains->Color(72, 218, 164),
            TileType.tundra->Color(153, 153, 153),
            TileType.steppe->Color(72, 100, 72)
        }
    };
    "Whether the given map version supports the given tile type."
    shared Boolean supportsType(Integer version, TileType type) {
        if (exists map = colors.get(version), exists color = map.get(type)) {
            return true;
        } else {
            return false;
        }
    }
    "Get the color to use for the given tile type in the given map version. Throws
     if the given version does not support that tile type."
    todo("Return null instead?")
    shared Color get(Integer version, TileType type) {
        if (exists map = colors.get(version)) {
            if (exists color = map.get(type)) {
                return color;
            } else {
                throw IllegalArgumentException(
                    "``type`` is not a terrain type version ``version`` can handle");
            }
        } else {
            throw IllegalArgumentException("Not a supported version");
        }
    }
    "Get a String (HTML) representation of the given terrain type."
    todo("Return null instead of throwing on unhandled types?")
    shared String getDescription(TileType type) {
        if (exists retval = descriptions.get(type)) {
            return retval;
        } else {
            throw IllegalArgumentException("No description for that type found");
        }
    }
    "Get the color that a fixture should turn the tile if it's not on top."
    todo("Return null instead of throwing on unhandled fixtures?")
    shared Color getFeatureColor(TileFixture fixture) {
        if (exists color = featureColors.get(type(fixture))) {
            return color;
        } else {
            throw IllegalArgumentException("Not a kind of fixture we can handle");
        }
    }
    "The color to use for background mountains."
    shared Color mountainColor = Color(249, 137, 28);
}
"A fortress is drawn in brown."
Color fortColor = Color(160, 82, 45);
"Units are drawn in purple."
Color unitColor = Color(148, 0, 211);
"Events are drawn in pink."
Color eventColor = Color.pink;
