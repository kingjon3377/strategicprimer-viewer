package util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import model.map.TileType;
import view.map.main.TileUIHelper;

/**
 * A class to load images from file, backed by a cache so no image is loaded
 * more than once. This probably belongs in the controller, but we'd rather
 * avoid package-level circular dependencies. And it shouldn't be singleton, but
 * I can't think of a better way of making the cache actually be shared without
 * making it effectively singleton except for lightweight instances to access
 * the singleton cache.
 *
 * @author Jonathan Lovelace
 */
public final class ImageLoader {
	/**
	 * The cache.
	 */
	private final Map<String, Image> cache = new HashMap<String, Image>();
	/**
	 * The object we use for our actual file I/O.
	 */
	private final LoadFile helper = new LoadFile();

	/**
	 * Constructor.
	 */
	private ImageLoader() {
		final TileUIHelper colors = new TileUIHelper();
		for (TileType type : TileType.values()) {
			final BufferedImage buf = new BufferedImage(20, 20, //NOPMD
					BufferedImage.TYPE_INT_ARGB);
			final Graphics pen = buf.createGraphics();
			pen.setColor(colors.get(2, type));
			pen.fillRect(0, 0, buf.getWidth(), buf.getHeight());
			pen.dispose();
			iconCache.put(type.toXML() + ".png", new ImageIcon(buf)); //NOPMD
		}
	}

	/**
	 * Singleton instance.
	 */
	private static final ImageLoader LOADER = new ImageLoader();

	/**
	 * @return the instance.
	 */
	public static ImageLoader getLoader() {
		return LOADER;
	}

	/**
	 * Load an image from the cache, or if not in it, from file.
	 *
	 * @param file the name of the file to load
	 * @return the image contained in the file.
	 * @throws FileNotFoundException if the file isn't found.
	 * @throws IOException on I/O error reading the file
	 */
	public Image loadImage(final String file) throws FileNotFoundException,
			IOException {
		if (!cache.containsKey(file)) {
			cache.put(file,
					ImageIO.read(helper.doLoadFileAsStream("images/" + file)));
		}
		return cache.get(file);
	}

	/**
	 * An icon cache.
	 */
	private final Map<String, Icon> iconCache = new HashMap<String, Icon>();

	/**
	 * Load an icon from the cache, or if not in it, from file.
	 *
	 * @param file the name of the file to load
	 * @return an icon of image contained in the file.
	 * @throws FileNotFoundException if the file isn't found.
	 * @throws IOException on I/O error reading the file
	 */
	public Icon loadIcon(final String file) throws FileNotFoundException,
			IOException {
		if (!iconCache.containsKey(file)) {
			iconCache.put(file, new ImageIcon(loadImage(file)
					.getScaledInstance(20, -1, Image.SCALE_DEFAULT)));
		}
		return iconCache.get(file);
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ImageLoader";
	}
}
