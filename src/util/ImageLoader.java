package util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
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
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2011-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 */
public final class ImageLoader {
	/**
	 * Singleton instance.
	 */
	private static final ImageLoader LOADER = new ImageLoader();

	/**
	 * An icon cache.
	 */
	private final Map<String, Icon> iconCache = new HashMap<>();

	/**
	 * The size of fixture icons.
	 */
	private static final int ICON_SIZE = 20;
	/**
	 * The cache.
	 */
	private final Map<String, Image> cache = new HashMap<>();

	/**
	 * Constructor.
	 */
	private ImageLoader() {
		final TileUIHelper colors = new TileUIHelper();
		for (final TileType type : TileType.values()) {
			assert type != null;
			final BufferedImage buf = new BufferedImage(ICON_SIZE, ICON_SIZE, //NOPMD
					BufferedImage.TYPE_INT_ARGB);
			final Graphics pen = buf.createGraphics();
			if (colors.supportsType(2, type)) {
				pen.setColor(colors.get(2, type));
			}
			pen.fillRect(0, 0, buf.getWidth(), buf.getHeight());
			pen.dispose();
			iconCache.put(type.toXML() + ".png", new ImageIcon(buf)); // NOPMD
		}
	}

	/**
	 * @return the instance.
	 */
	public static ImageLoader getLoader() {
		return LOADER;
	}

	/**
	 * Load an image from the cache, or if not in it, from file.
	 *
	 * @param file
	 *            the name of the file to load
	 * @return the image contained in the file.
	 * @throws IOException
	 *             if the file isn't found, or on other I/O error reading the
	 *             file
	 */
	public Image loadImage(final String file) throws IOException {
		if (!cache.containsKey(file)) {
			try (final ResourceInputStream res = new ResourceInputStream(
					"images/" + file)) {
				cache.put(file, NullCleaner.assertNotNull(ImageIO.read(res)));
			}
		}
		return NullCleaner.assertNotNull(cache.get(file));
	}

	/**
	 * Load an icon from the cache, or if not in it, from file.
	 *
	 * @param file
	 *            the name of the file to load
	 * @return an icon of image contained in the file.
	 * @throws IOException
	 *             if the file isn't found, or on other I/O error reading the
	 *             file
	 */
	public Icon loadIcon(final String file) throws IOException {
		if (!iconCache.containsKey(file)) {
			iconCache.put(file, new ImageIcon(loadImage(file)
					.getScaledInstance(ICON_SIZE, -1, Image.SCALE_DEFAULT)));
		}
		return NullCleaner.assertNotNull(iconCache.get(file));
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ImageLoader";
	}
}
