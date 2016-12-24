package util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import model.map.TileType;
import view.map.main.TileUIHelper;

/**
 * A class to load images from file, backed by a cache so no image is loaded more than
 * once. This probably belongs in the controller, but we'd rather avoid package-level
 * circular dependencies. And it shouldn't be singleton, but I can't think of a better way
 * of making the cache actually be shared without making it effectively singleton except
 * for lightweight instances to access the singleton cache.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class ImageLoader {
	/**
	 * Singleton instance.
	 */
	private static final ImageLoader LOADER = new ImageLoader();
	/**
	 * The size of fixture icons.
	 */
	private static final int ICON_SIZE = 20;
	/**
	 * An icon cache.
	 */
	private final Map<String, Icon> iconCache = new HashMap<>();
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
			//noinspection ObjectAllocationInLoop
			final BufferedImage buf =
					new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
			final Graphics pen = buf.createGraphics();
			if (colors.supportsType(2, type)) {
				pen.setColor(colors.get(2, type));
			}
			pen.fillRect(0, 0, buf.getWidth(), buf.getHeight());
			pen.dispose();
			//noinspection ObjectAllocationInLoop
			iconCache.put(type.toXML() + ".png", new ImageIcon(buf));
		}
	}

	/**
	 * The singleton.
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
	 * @throws IOException if the file isn't found, or on other I/O error reading the
	 *                     file
	 */
	@SuppressWarnings("StringConcatenationMissingWhitespace")
	public Image loadImage(final String file) throws IOException {
		if (!cache.containsKey(file)) {
			try (final InputStream res =
						 new ResourceInputStream("images" + File.separatorChar + file)) {
				final BufferedImage image = ImageIO.read(res);
				if (image == null) {
					throw new IOException("No reader could read the file");
				} else {
					cache.put(file, image);
				}
			}
		}
		return cache.get(file);
	}

	/**
	 * Load an icon from the cache, or if not in it, from file.
	 *
	 * @param file the name of the file to load
	 * @return an icon of image contained in the file.
	 * @throws IOException if the file isn't found, or on other I/O error reading the
	 *                     file
	 */
	public Icon loadIcon(final String file) throws IOException {
		if (!iconCache.containsKey(file)) {
			final Image orig = loadImage(file);
			final BufferedImage temp =
					new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
			final Graphics pen = temp.getGraphics();
			pen.drawImage(orig, 0, 0, ICON_SIZE, ICON_SIZE, null);
			pen.dispose();
			iconCache.put(file, new ImageIcon(temp));
		}
		return iconCache.get(file);
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ImageLoader";
	}
}
