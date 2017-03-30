import ceylon.collection {
    MutableSet,
    HashSet
}

import java.awt {
    Image,
    Graphics,
    Color
}
import java.awt.image {
    ImageObserver,
    BufferedImage
}
import java.io {
    FileNotFoundException,
    IOException
}
import java.nio.file {
    NoSuchFileException
}

import lovelace.util.common {
    todo
}

import model.map {
    River,
    HasImage,
    Point
}

import strategicprimer.viewer.model.map {
    TileFixture,
    IMapNG,
    coordinateFactory
}
import strategicprimer.viewer.model.map.fixtures {
    RiverFixture,
    TerrainFixture,
    Ground
}
import strategicprimer.viewer.model.map.fixtures.terrain {
    Forest
}

import view.util {
    Coordinate
}
"A [[TileDrawHelper]] for version-2 maps."
class Ver2TileDrawHelper(
        "The object to arrange to be notified as images finish drawing. In Java it's the
         [[ImageObserver]] interface, but we don't want to have to construct *objects*
         for this when a lambda will do."
        Boolean(Image, Integer, Integer, Integer, Integer, Integer) observer,
        "The object to query about whether to display a fixture."
        Boolean(TileFixture) filter,
        "A series of matchers to use to determine what's on top."
        {FixtureMatcher*} matchers) satisfies TileDrawHelper {
    "A comparator to put fixtures in order by the order of the first fixture that matches
     them."
    Comparison compareFixtures(TileFixture one, TileFixture two) {
        for (matcher in matchers) {
            if (matcher.matches(one)) {
                if (matcher.matches(two)) {
                    return equal;
                } else {
                    return smaller;
                }
            } else if (matcher.matches(two)) {
                return larger;
            }
        }
        return equal;
    }
    "Images we've already determined aren't there."
    MutableSet<String> missingFiles = HashSet<String>();
    "A mapping from river-sets to filenames."
    Map<Set<River>, String> riverFiles = map {
        set<River> { }->"riv00.png", set { River.north }->"riv01.png",
        set { River.east }->"riv02.png", set {River.south}->"riv03.png",
        set {River.west}->"riv04.png", set {River.lake}->"riv05.png",
        set {River.north, River.east}->"riv06.png",
        set {River.north,River.south}->"riv07.png",
        set {River.north,River.west}->"riv08.png",
        set {River.north,River.lake}->"riv09.png",
        set {River.east,River.south}->"riv10.png",
        set {River.east,River.west}->"riv11.png",
        set {River.east,River.lake}->"riv12.png",
        set {River.south,River.west}->"riv13.png",
        set {River.south,River.lake}->"riv14.png",
        set {River.west,River.lake}->"riv15.png",
        set {River.north,River.east,River.south}->"riv16.png",
        set {River.north,River.east,River.west}->"riv17.png",
        set {River.north,River.east,River.lake}->"riv18.png",
        set {River.north,River.south,River.west}->"riv19.png",
        set {River.north,River.south,River.lake}->"riv20.png",
        set {River.north,River.west,River.lake}->"riv21.png",
        set {River.east,River.south,River.west}->"riv22.png",
        set {River.east,River.south,River.lake}->"riv23.png",
        set {River.east,River.west,River.lake}->"riv24.png",
        set {River.south,River.west,River.lake}->"riv25.png",
        set {River.north,River.east,River.south,River.west}->"riv26.png",
        set {River.north,River.south,River.west,River.lake}->"riv27.png",
        set {River.north,River.east,River.west,River.lake}->"riv28.png",
        set {River.north,River.east,River.south,River.lake}->"riv29.png",
        set {River.east,River.south,River.west,River.lake}->"riv30.png",
        set {River.north,River.east,River.south,River.west,River.lake}->"riv31.png"
    };
    "Log, but otherwise ignore, file-not-found or other I/O error from loading an image."
    todo("Essentially inline this")
    void logLoadingError(IOException except,
            "The file we were trying to load from" String filename,
            "True if this was the fallback image (making this error more serious)"
            Boolean fallback) {
        if (except is FileNotFoundException || except is NoSuchFileException) {
            String message = "Image ``filename`` not found";
            if (fallback) {
                log.error(message, except);
            } else {
                log.info(message, except);
            }
        } else {
            log.error("I/O error while loading image ``filename``", except);
        }
    }
    for (file in {"trees.png", "mountain.png"}) {
        try {
            loadImage(file);
        } catch (IOException except) {
            logLoadingError(except, file, false);
        }
    }
    "Create the fallback image---made a method so the object reference can be immutable"
    Image createFallbackImage() {
        try {
            return loadImage("event_fallback.png");
        } catch (IOException except) {
            logLoadingError(except, "event_fallback.png", true);
            return BufferedImage(1, 1, BufferedImage.typeIntArgb);
        }
    }
    "A fallback image for when an image file is missing or fails to load."
    Image fallbackImage = createFallbackImage();
    """Get the color representing a "not-on-top" terrain fixture at the given location."""
    Color getFixtureColor(IMapNG map, Point location) {
        if (exists top = getTopFixture(map, location)) {
            if (exists topTerrain = getDrawableFixtures(map, location)
                .filter((fixture) => fixture != top)
                .filter((fixture) => fixture is TerrainFixture)
                .first) {
                assert (is TerrainFixture topTerrain);
                return colorHelper.getFeatureColor(topTerrain);
            } else if (map.isMountainous(location)) {
                return colorHelper.mountainColor;
            }
        }
        return colorHelper.get(map.dimensions.version,
            map.getBaseTerrain(location));
    }
    "Return either a loaded image or, if the specified image fails to load, the generic
     one."
    Image getImage(String filename) {
        try {
            return loadImage(filename);
        } catch (FileNotFoundException|NoSuchFileException except) {
            if (!missingFiles.contains(filename)) {
                log.error("images/``filename`` not found");
                log.debug("with stack trace", except);
                missingFiles.add(filename);
            }
            return fallbackImage;
        } catch (IOException except) {
            log.error("I/O error reading image images/``filename``", except);
            return fallbackImage;
        }
    }
    "Get the image representing the given fixture."
    Image getImageForFixture(TileFixture fixture) {
        if (is HasImage fixture) {
            String image = fixture.image;
            if (image.empty || missingFiles.contains(image)) {
                return getImage(fixture.defaultImage);
            } else {
                return getImage(image);
            }
        } else if (is RiverFixture fixture) {
            return getImage(riverFiles.get(fixture.rivers) else "");
        } else {
            log.warn("Using fallback image for unexpected kind of fixture");
            return fallbackImage;
        }
    }
    object observerWrapper satisfies ImageObserver {
        shared actual Boolean imageUpdate(Image img, Integer infoflags, Integer x,
                Integer y, Integer width, Integer height) =>
                observer(img, infoflags, x, y, width, height);
    }
    "Draw a tile at the specified coordinates. Because this is at present only called in
     a loop that's the last thing before the graphics context is disposed, we alter the
     state freely and don't restore it."
    shared actual void drawTile(Graphics pen, IMapNG map, Point location,
            Coordinate coordinates, Coordinate dimensions) {
        if (needsFixtureColor(map, location)) {
            pen.color = getFixtureColor(map, location);
        } else {
            pen.color = colorHelper.get(map.dimensions.version,
                map.getBaseTerrain(location));
        }
        pen.fillRect(coordinates.x, coordinates.y, dimensions.x, dimensions.y);
        if (!map.getRivers(location).empty) {
            pen.drawImage(getRiverImage(map.getRivers(location)), coordinates.x,
                coordinates.y, dimensions.x, dimensions.y, observerWrapper);
        }
        if (exists top = getTopFixture(map, location)) {
            pen.drawImage(getImageForFixture(top), coordinates.x, coordinates.y,
                dimensions.x, dimensions.y, observerWrapper);
        } else if (map.isMountainous(location)) {
            pen.drawImage(getImage("mountain.png"), coordinates.x, coordinates.y,
                dimensions.x, dimensions.y, observerWrapper);
        }
        pen.color = Color.black;
        pen.drawRect(coordinates.x, coordinates.y, dimensions.x, dimensions.y);
    }
    "Draw a tile at the upper left corner of the drawing surface."
    shared actual void drawTileTranslated(Graphics pen, IMapNG map, Point location,
            Integer width, Integer height) =>
            drawTile(pen, map, location, coordinateFactory(0, 0),
                coordinateFactory(width, height));
    "The drawable fixtures at the given location."
    {TileFixture*} getDrawableFixtures(IMapNG map, Point location) {
        Ground? ground = map.getGround(location);
        Forest? forest = map.getForest(location);
        {TileFixture?*} allFixtures = {ground, forest,
            *map.getOtherFixtures(location)};
        return allFixtures.coalesced
            .filter((fixture) => !fixture is TileTypeFixture).filter(filter)
            .sort(compareFixtures);
    }
    "Get the image representing the given configuration of rivers."
    Image getRiverImage({River*} rivers) {
        if (is Set<River> rivers) {
            return getImage(riverFiles.get(rivers) else "");
        } else {
            return getImage(riverFiles.get(set {*rivers}) else "");
        }
    }
    """Get the "top" fixture at the given location"""
    TileFixture? getTopFixture(IMapNG map, Point location) =>
            getDrawableFixtures(map, location).first;
    """Whether there is a "terrain fixture" at the gtiven location."""
    Boolean hasTerrainFixture(IMapNG map, Point location) {
        if (getDrawableFixtures(map, location).any((fixture) => fixture is TerrainFixture)) {
            return true;
        } else if (getDrawableFixtures(map, location).first exists, map.isMountainous(location)) {
            return true;
        } else {
            return false;
        }
    }
    "Whether we need a different background color to show a non-top fixture (e.g. forest)
     at the given location"
    Boolean needsFixtureColor(IMapNG map, Point location) {
        if (hasTerrainFixture(map, location), exists top = getTopFixture(map, location)) {
            if (exists bottom = getDrawableFixtures(map, location).reduce((TileFixture? partial, element) => element)) {
                return top != bottom;
            } else if (map.isMountainous(location)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
