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
import strategicprimer.model.map {
    TileType
}
import java.io {
    IOException
}
import java.awt {
    Image,
    Graphics
}
import ceylon.interop.java {
    javaClassFromInstance,
    javaClassFromDeclaration
}
import strategicprimer.viewer.drivers {
    SPFrame
}
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
MutableMap<String, Icon> iconCache = HashMap<String, Icon> {
    for (tileType in `TileType`.caseValues)
    "``tileType.xml``.png"->createTerrainIcon(tileType)
};
"A cache of loaded images."
MutableMap<String, Image> imageCache = HashMap<String, Image>();
"Load an image from the cache, or if not in it, from file (and add it to the cache)"
shared Image loadImage(String file) {
    if (exists cached = imageCache.get(file)) {
        return cached;
    } else {
        try (res = ResourceInputStream("images/``file``",
                `module strategicprimer.viewer`, javaClassFromDeclaration(`class SPFrame`))) {
            if (exists image = ImageIO.read(res)) {
                imageCache.put(file, image);
                return image;
            } else {
                throw IOException("No reader could read the file images/``file``");
            }
        }
    }
}
"Load an icon from cache, or if not in the cache from file (adding it to the cache)"
shared Icon loadIcon(String file) {
    if (exists cached = iconCache.get(file)) {
        return cached;
    } else {
        Image orig = loadImage(file);
        BufferedImage temp = BufferedImage(fixtureIconSize, fixtureIconSize,
            BufferedImage.typeIntArgb);
        Graphics pen = temp.graphics;
        pen.drawImage(orig, 0, 0, temp.width, temp.height, null);
        pen.dispose();
        Icon icon = ImageIcon(temp);
        iconCache.put(file, icon);
        return icon;
    }
}
