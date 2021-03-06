import javax.imageio {
    ImageIO
}
import javax.swing {
    ImageIcon,
    Icon
}
import ceylon.collection {
    MutableMap,
    HashMap
}
import lovelace.util.jvm {
    ResourceInputStream
}
import java.awt.image {
    BufferedImage
}
import strategicprimer.model.common.map {
    TileType
}
import java.io {
    IOException
}
import java.awt {
    Image,
    Graphics
}
import java.lang {
    Types
}
import strategicprimer.viewer.drivers {
    IOHandler
}
import lovelace.util.common {
    todo,
    MissingFileException
}

"A helper object to load images from file or the classpath (in the directory
 this suite expects them to be in), given their filename, and cache them."
shared object imageLoader {
    log.trace("Expect to be able to load the following image file formats: " +
        ", ".join(ImageIO.readerFileSuffixes.array.coalesced));

    "The size of fixture icons."
    Integer fixtureIconSize = 28;

    "Create a very simple background icon for a terrain type"
    Icon createTerrainIcon(TileType tileType) {
        BufferedImage retval = BufferedImage(fixtureIconSize, fixtureIconSize,
            BufferedImage.typeIntArgb);
        Graphics pen = retval.createGraphics();
        if (colorHelper.supportsType(2, tileType)) {
            pen.color = colorHelper.get(2, tileType);
        }
        pen.fillRect(0, 0, retval.width, retval.height);
        pen.dispose();
        return ImageIcon(retval);
    }

    "An icon cache."
    MutableMap<String, Icon> iconCache = HashMap<String, Icon>();
    for (tileType in `TileType`.caseValues) {
        iconCache.put("``tileType.xml``.png", createTerrainIcon(tileType));
    }

    "A cache of loaded images."
    MutableMap<String, Image> imageCache = HashMap<String, Image>();

    "Load an image from the cache, or if not in it, from file (and add it to the cache)"
    todo("Add support for SVG (presumably using Batik)",
         "Return null instead of throwing if not loadable?")
    throws(`class MissingFileException`, "if the file does not exist")
    throws(`class IOException`, "If no reader could read the file")
    shared Image loadImage(String file) {
        if (exists cached = imageCache[file]) {
            return cached;
        } else {
            try (res = ResourceInputStream("images/``file``",
                    `module strategicprimer.viewer`,
                        Types.classForDeclaration(`class IOHandler`))) {
                if (exists image = ImageIO.read(res)) {
                    imageCache[file] = image;
                    return image;
                } else {
                    throw IOException("No reader could read the file images/``file``");
                }
            }
        }
    }

    "Load an icon from cache, or if not in the cache from file (adding it to the cache)"
    throws(`class IOException`, "If not in the cache and can't be loaded from file")
    shared Icon loadIcon(String file) {
        if (exists cached = iconCache[file]) {
            return cached;
        } else {
            Image orig = loadImage(file);
            BufferedImage temp = BufferedImage(fixtureIconSize, fixtureIconSize,
                BufferedImage.typeIntArgb);
            Graphics pen = temp.graphics;
            pen.drawImage(orig, 0, 0, temp.width, temp.height, null);
            pen.dispose();
            Icon icon = ImageIcon(temp);
            iconCache[file] = icon;
            return icon;
        }
    }
}
