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
    IOException
}

import strategicprimer.model.common.map {
    HasImage,
    Point,
    TileFixture,
    IMapNG,
    FakeFixture
}
import strategicprimer.model.common.map.fixtures {
    TerrainFixture
}
import strategicprimer.drivers.common {
    FixtureMatcher
}
import lovelace.util.common {
    MissingFileException
}

"A [[TileDrawHelper]] for version-2 maps."
shared class Ver2TileDrawHelper(
        "The object to arrange to be notified as images finish drawing. In Java it's the
         [[ImageObserver]] interface, but we don't want to have to construct *objects*
         for this when a lambda will do."
        Boolean(Image, Integer, Integer, Integer, Integer, Integer) observer,
        "The object to query about whether to display a fixture."
        Boolean(TileFixture) filter,
        "A series of matchers to use to determine what's on top."
        {FixtureMatcher*} matchers) satisfies TileDrawHelper {
    "A comparator to put fixtures in order by the order of the first matcher that matches
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

    for (file in ["trees.png", "mountain.png"]) {
        try {
            imageLoader.loadImage(file);
        } catch (MissingFileException except) {
            log.info("Image ``file`` not found", except);
        } catch (IOException except) {
            log.error("I/O error while loading image ``file``", except);
        }
    }

    "Create the fallback image---made a method so the object reference can be immutable"
    Image createFallbackImage() {
        Image fallbackFallback = BufferedImage(1, 1, BufferedImage.typeIntArgb);
        String filename = "event_fallback.png";
        try {
            return imageLoader.loadImage(filename);
        } catch (MissingFileException except) {
            log.error("Image ``filename`` not found", except);
            return fallbackFallback;
        } catch (IOException except) {
            log.error("I/O error while loading image ``filename``", except);
            return fallbackFallback;
        }
    }

    "A fallback image for when an image file is missing or fails to load."
    Image fallbackImage = createFallbackImage();

    """Get the color representing a "not-on-top" terrain fixture at the given location."""
    Color? getFixtureColor(IMapNG map, Point location) {
        if (exists top = getTopFixture(map, location)) {
            if (exists topTerrain = getDrawableFixtures(map, location)
                    .filter(not(top.equals)).narrow<TerrainFixture>()
                    .first, exists color = colorHelper.getFeatureColor(topTerrain)) {
                return color;
//            } else if (map.mountainous[location]) { // TODO: syntax sugar once compiler bug fixed
            } else if (map.mountainous.get(location)) {
                return colorHelper.mountainColor;
            }
        }

        return colorHelper.get(map.dimensions.version,
//            map.baseTerrain[location]); // TODO: syntax sugar once compiler bug fixed
            map.baseTerrain.get(location));
    }

    "Return either a loaded image or, if the specified image fails to load, the generic
     one."
    Image getImage(String filename) {
        try {
            return imageLoader.loadImage(filename);
        } catch (MissingFileException except) {
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
        } else {
            log.warn("Using fallback image for unexpected kind of fixture");
            return fallbackImage;
        }
    }

    // TODO: Drop (replace in caller(s) with observer()) once eclipse/ceylon#7380 fixed
    object observerWrapper satisfies ImageObserver {
        shared actual Boolean imageUpdate(Image img, Integer infoflags, Integer x,
                Integer y, Integer width, Integer height) =>
                observer(img, infoflags, x, y, width, height);
    }

    "Draw an icon at the specified coordinates."
    void drawIcon(Graphics pen, String|Image icon, Coordinate coordinates, Coordinate dimensions) {
        Image image;
        switch (icon)
        case (is Image) { image = icon; }
        case (is String) { image = getImage(icon); }
        pen.drawImage(image, coordinates.x, coordinates.y, dimensions.x, dimensions.y, observerWrapper);
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
//                map.baseTerrain[location]); // TODO: syntax sugar once compiler bug fixed
                map.baseTerrain.get(location)); // TODO: syntax sugar once compiler bug fixed
        }
        pen.fillRect(coordinates.x, coordinates.y, dimensions.x, dimensions.y);
//        for (river in map.rivers[location]) {
        for (river in map.rivers.get(location)) {
            drawIcon(pen, "river``river.ordinal``.png", coordinates, dimensions);
        }
        for (direction->_ in map.roads[location] else []) {
            drawIcon(pen, "road``direction.ordinal``.png", coordinates, dimensions);
        }
        if (exists top = getTopFixture(map, location)) {
            drawIcon(pen, getImageForFixture(top), coordinates, dimensions);
//        } else if (map.mountainous[location]) { // TODO: syntax sugar once compiler bug fixed
        } else if (map.mountainous.get(location)) {
            drawIcon(pen, "mountain.png", coordinates, dimensions);
        }
        if (location in map.bookmarks) {
            drawIcon(pen, "bookmark.png", coordinates, dimensions);
        }
        pen.color = Color.black;
        pen.drawRect(coordinates.x, coordinates.y, dimensions.x, dimensions.y);
    }

    "The drawable fixtures at the given location."
    {TileFixture*} getDrawableFixtures(IMapNG map, Point location) {
//        return map.fixtures[location] // TODO: syntax sugar once compiler bug fixed
        return map.fixtures.get(location).filter(not(`FakeFixture`.typeOf))
            .filter(filter).sort(compareFixtures);
    }

    """Get the "top" fixture at the given location"""
    TileFixture? getTopFixture(IMapNG map, Point location) =>
            getDrawableFixtures(map, location).first;

    """Whether there is a "terrain fixture" at the gtiven location."""
    Boolean hasTerrainFixture(IMapNG map, Point location) {
        if (!getDrawableFixtures(map, location).narrow<TerrainFixture>().empty) {
            return true;
        } else if (getDrawableFixtures(map, location).first exists,
//                map.mountainous[location]) {
                map.mountainous.get(location)) {
            return true;
        } else {
            return false;
        }
    }

    "Whether we need a different background color to show a non-top fixture (e.g. forest)
     at the given location"
    Boolean needsFixtureColor(IMapNG map, Point location) {
        if (hasTerrainFixture(map, location), exists top = getTopFixture(map, location)) {
            if (exists bottom = getDrawableFixtures(map, location)
                    .narrow<TerrainFixture>().last) {
                return top != bottom;
//            } else if (map.mountainous[location]) {
            } else if (map.mountainous.get(location)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
