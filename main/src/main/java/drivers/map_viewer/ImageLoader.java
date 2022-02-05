package drivers.map_viewer;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.awt.Color;
import common.map.fixtures.terrain.Oasis;
import common.map.fixtures.terrain.Hill;
import common.map.fixtures.terrain.Forest;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.Icon;
import java.util.Map;
import java.util.HashMap;
import lovelace.util.ResourceInputStream;
import java.awt.image.BufferedImage;
import common.map.TileType;
import common.map.TileFixture;
import java.io.IOException;
import java.awt.Image;
import java.awt.Graphics;

import lovelace.util.MissingFileException;
import java.util.logging.Logger;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.Collectors;

/**
 * A helper object to load images from file or the classpath (in the directory
 * this suite expects them to be in), given their filename, and cache them.
 */
public final class ImageLoader {
	private ImageLoader() {
	}
	private static Logger LOGGER = Logger.getLogger(ImageLoader.class.getName());

	static {
		LOGGER.fine(Stream.of(ImageIO.getReaderFileSuffixes()).filter(Objects::nonNull)
			.collect(Collectors.joining(", ",
				"Expect to be able to load the following image file formats: ", "")));
	}

	/**
	 * The size of fixture icons.
	 */
	private static final int FIXTURE_ICON_SIZE = 28;

	/**
	 * Create a very simple background icon for a terrain type
	 */
	private static Icon createTerrainIcon(final TileType tileType) {
		BufferedImage retval = new BufferedImage(FIXTURE_ICON_SIZE, FIXTURE_ICON_SIZE,
			BufferedImage.TYPE_INT_ARGB);
		Graphics pen = retval.createGraphics();
		if (ColorHelper.supportsType(2, tileType)) {
			pen.setColor(ColorHelper.get(2, tileType));
		}
		pen.fillRect(0, 0, retval.getWidth(), retval.getHeight());
		pen.dispose();
		return new ImageIcon(retval);
	}

	/**
	 * An icon cache.
	 */
	private static final Map<String, Icon> ICON_CACHE = new HashMap<>();

	static {
		for (TileType tileType : TileType.values()) {
			ICON_CACHE.put(tileType.getXml() + ".png", createTerrainIcon(tileType));
		}
	}

	/**
	 * A cache of loaded images.
	 */
	private static final Map<String, Image> IMAGE_CACHE = new HashMap<>();

	/**
	 * Load an image from the cache, or if not in it, from file (and add it to the cache)
	 *
	 * TODO: Add support for SVG (presumably using Batik)
	 *
	 * TODO: "Return null instead of throwing if not loadable?
	 *
	 * @throws IOException If no reader could read the file (or the file does not exist)
	 */
	public static Image loadImage(final String file) throws IOException {
		if (IMAGE_CACHE.containsKey(file)) {
			return IMAGE_CACHE.get(file);
		} else {
			try (ResourceInputStream res = new ResourceInputStream("images/" + file,
					ImageLoader.class)) { // TODO: Change back to IOHandler once ported?
				Image image = ImageIO.read(res);
				if (image != null) {
					IMAGE_CACHE.put(file, image);
					return image;
				} else {
					throw new IOException("No reader could read the file images/" + file);
				}
			}
		}
	}

	/**
	 * Load an icon from cache, or if not in the cache from file (adding it to the cache)
	 *
	 * @throws IOException If not in the cache and can't be loaded from file
	 */
	public static Icon loadIcon(final String file) throws IOException {
		if (ICON_CACHE.containsKey(file)) {
			return ICON_CACHE.get(file);
		} else {
			Image orig = loadImage(file);
			BufferedImage temp = new BufferedImage(FIXTURE_ICON_SIZE, FIXTURE_ICON_SIZE,
				BufferedImage.TYPE_INT_ARGB);
			Graphics pen = temp.createGraphics();
			pen.drawImage(orig, 0, 0, temp.getWidth(), temp.getHeight(), null);
			pen.dispose();
			Icon icon = new ImageIcon(temp);
			ICON_CACHE.put(file, icon);
			return icon;
		}
	}

	/**
	 * An encapsulation of the mapping from tile-types to colors.
	 */
	public static class ColorHelper {
		private static final Logger LOGGER = Logger.getLogger(ColorHelper.class.getName());
		private ColorHelper() {}
		private static String wrap(final String wrapped) {
			return "<html><p>" + wrapped + "</p></html>";
		}

		/**
		 * Descriptions of the types.
		 * We use Enum::name rather than spelling out the descriptions, as we did in Ceylon,
		 * because the reason we did so in Ceylon was that their names in the code were lowercased
		 * in Ceylon but are uppercased, which is what we want, here. And this will guarantee
		 * that we don't forget to update this if we change the set of possible tile types.
		 */
		private static final Map<TileType, String> DESCRIPTIONS =
			Collections.unmodifiableMap(Stream.of(TileType.values())
				.collect(Collectors.toMap(t -> t, t -> wrap(t.name()))));

		/**
		 * A map from types of features to the colors they can make the
		 * tile be. Used to show that a tile is forested, e.g., even
		 * when that is normally represented by an icon and there's a
		 * higher icon on the tile.
		 */
		private static final Map<Class<? extends TileFixture>, Color> FEATURE_COLORS;

		static {
			Map<Class<? extends TileFixture>, Color> featureColors = new HashMap<>();
			featureColors.put(Forest.class, new Color(0, 117, 0));
			featureColors.put(Oasis.class, new Color(72, 218, 164));
			featureColors.put(Hill.class, new Color(141, 182, 0));
			FEATURE_COLORS = Collections.unmodifiableMap(featureColors);
		}

		/**
		 * A map from map versions to maps from tile-types to colors.
		 */
		private static final Map<Integer, Map<TileType, Color>> COLORS;

		static {
			Map<TileType, Color> verTwo = new HashMap<>();
			verTwo.put(TileType.Desert, new Color(249, 233, 28));
			verTwo.put(TileType.Jungle, new Color(229, 46, 46));
			verTwo.put(TileType.Ocean, Color.BLUE);
			verTwo.put(TileType.Plains, new Color(72, 218, 164));
			verTwo.put(TileType.Tundra, new Color(153, 153, 153));
			verTwo.put(TileType.Steppe, new Color(72, 100, 72));
			verTwo.put(TileType.Swamp, new Color(231,41,138));
			// TODO: Somehow check that all types in a version are covered?
			Map<Integer, Map<TileType, Color>> colors = new HashMap<>();
			colors.put(2, Collections.unmodifiableMap(verTwo));
			COLORS = Collections.unmodifiableMap(colors);
		}

		public static boolean supportsType(final int version, final TileType type) {
			return COLORS.getOrDefault(version, Collections.emptyMap()).containsKey(type);
		}

		/**
		 * Get the color to use for the given tile type in the given
		 * map version. Returns null if the given version does not
		 * support that tile type. TODO: throw instead, as we used to do in Java?
		 */
		@Nullable
		public static Color get(final int version, @Nullable final TileType type) {
			if (type == null) {
				return null;
			} else if (COLORS.containsKey(version)) {
				Map<TileType, Color> map = COLORS.get(version);
				if (map.containsKey(type)) {
					return map.get(type);
				} else {
					LOGGER.severe(String.format(
						"Asked for unsupported type %s in version %d",
						type, version));
					return null;
				}
			} else {
				LOGGER.severe(String.format("Asked for %s in unsupported version %d",
					type, version));
				return null;
			}
		}

		/**
		 * Get a String (HTML) representation of the given terrain
		 * type. TODO: throw on not-found instead of returning null?
		 */
		@Nullable
		public static String getDescription(@Nullable final TileType type) {
			if (type == null) {
				return "Unknown";
			} else if (DESCRIPTIONS.containsKey(type)) {
				return DESCRIPTIONS.get(type);
			} else {
				LOGGER.severe("No description found for tile type " + type);
				return null;
			}
		}

		/**
		 * Get the color that a fixture should turn the tile if it's
		 * not on top. TODO: throw on not-found instead of returning
		 * null?
		 */
		@Nullable
		public static Color getFeatureColor(final TileFixture fixture) {
			if (FEATURE_COLORS.containsKey(fixture.getClass())) {
				return FEATURE_COLORS.get(fixture.getClass());
			} else {
				LOGGER.warning("Asked for color for unsupported fixture: " + fixture);
				return null;
			}
		}

		/**
		 * The color to use for background mountains.
		 */
		public static final Color MOUNTAIN_COLOR = new Color(249, 137, 28);
	}
}
