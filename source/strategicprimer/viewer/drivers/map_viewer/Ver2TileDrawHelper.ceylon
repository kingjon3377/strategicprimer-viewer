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

import strategicprimer.model.map {
    Point,
    River,
    TileFixture,
    HasImage,
    IMapNG
}
import strategicprimer.model.map.fixtures {
    TerrainFixture
}
import strategicprimer.drivers.common {
	FixtureMatcher
}
import lovelace.util.common {
	matchingValue,
	inverse,
	simpleSet,
	simpleMap
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
    "A mapping from river-sets to filenames."
    Map<Set<River>, String> riverFiles = simpleMap(
        emptySet->"riv00.png", simpleSet(River.north)->"riv01.png",
        simpleSet(River.east)->"riv02.png", simpleSet(River.south)->"riv03.png",
        simpleSet(River.west)->"riv04.png", simpleSet(River.lake)->"riv05.png",
        simpleSet(River.north, River.east)->"riv06.png",
        simpleSet(River.north,River.south)->"riv07.png",
        simpleSet(River.north,River.west)->"riv08.png",
        simpleSet(River.north,River.lake)->"riv09.png",
        simpleSet(River.east,River.south)->"riv10.png",
        simpleSet(River.east,River.west)->"riv11.png",
        simpleSet(River.east,River.lake)->"riv12.png",
        simpleSet(River.south,River.west)->"riv13.png",
        simpleSet(River.south,River.lake)->"riv14.png",
        simpleSet(River.west,River.lake)->"riv15.png",
        simpleSet(River.north,River.east,River.south)->"riv16.png",
        simpleSet(River.north,River.east,River.west)->"riv17.png",
        simpleSet(River.north,River.east,River.lake)->"riv18.png",
        simpleSet(River.north,River.south,River.west)->"riv19.png",
        simpleSet(River.north,River.south,River.lake)->"riv20.png",
        simpleSet(River.north,River.west,River.lake)->"riv21.png",
        simpleSet(River.east,River.south,River.west)->"riv22.png",
        simpleSet(River.east,River.south,River.lake)->"riv23.png",
        simpleSet(River.east,River.west,River.lake)->"riv24.png",
        simpleSet(River.south,River.west,River.lake)->"riv25.png",
        simpleSet(River.north,River.east,River.south,River.west)->"riv26.png",
        simpleSet(River.north,River.south,River.west,River.lake)->"riv27.png",
        simpleSet(River.north,River.east,River.west,River.lake)->"riv28.png",
        simpleSet(River.north,River.east,River.south,River.lake)->"riv29.png",
        simpleSet(River.east,River.south,River.west,River.lake)->"riv30.png",
        simpleSet(River.north,River.east,River.south,River.west,River.lake)->"riv31.png"
    );
    for (file in ["trees.png", "mountain.png"]) {
        try {
            imageLoader.loadImage(file);
        } catch (FileNotFoundException|NoSuchFileException except) {
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
        } catch (FileNotFoundException|NoSuchFileException except) {
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
                    .filter(matchingValue(false, top.equals))
                    .narrow<TerrainFixture>()
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
//                map.baseTerrain[location]); // TODO: syntax sugar once compiler bug fixed
                map.baseTerrain.get(location)); // TODO: syntax sugar once compiler bug fixed
        }
        pen.fillRect(coordinates.x, coordinates.y, dimensions.x, dimensions.y);
//        if (!map.rivers[location].empty) {
        if (!map.rivers.get(location).empty) {
//            pen.drawImage(getRiverImage(map.rivers[location]), coordinates.x,
            pen.drawImage(getRiverImage(map.rivers.get(location)), coordinates.x,
                coordinates.y, dimensions.x, dimensions.y, observerWrapper);
        }
        if (exists top = getTopFixture(map, location)) {
            pen.drawImage(getImageForFixture(top), coordinates.x, coordinates.y,
                dimensions.x, dimensions.y, observerWrapper);
//        } else if (map.mountainous[location]) { // TODO: syntax sugar once compiler bug fixed
        } else if (map.mountainous.get(location)) {
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
//        return map.fixtures[location] // TODO: syntax sugar once compiler bug fixed
        return map.fixtures.get(location).filter(inverse(`TileTypeFixture`.typeOf))
            .filter(filter).sort(compareFixtures);
    }
    "Get the image representing the given configuration of rivers."
    Image getRiverImage({River*} rivers) {
        if (exists file = riverFiles[set(rivers)]) {
            return getImage(file);
        } else {
            log.error("No image found for the River set ``set(rivers)``");
            return getImage("riv00.png");
        }
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
            if (exists bottom = getDrawableFixtures(map, location).last) {
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
