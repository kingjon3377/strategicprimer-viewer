package drivers.map_viewer;

import java.util.EnumMap;

import lovelace.util.LovelaceLogger;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.awt.Color;

import legacy.map.fixtures.terrain.Oasis;
import legacy.map.fixtures.terrain.Hill;
import legacy.map.fixtures.terrain.Forest;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.Icon;
import java.util.Map;
import java.util.HashMap;

import lovelace.util.ResourceInputStream;

import java.awt.image.BufferedImage;

import legacy.map.TileType;
import legacy.map.TileFixture;

import java.io.IOException;
import java.awt.Image;
import java.awt.Graphics;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.Collectors;

/**
 * A helper object to load images from file or the classpath (in the directory
 * this suite expects them to be in), given their filename, and cache them.
 */
public final class ImageLoader {
	private ImageLoader() {
	}

	static {
		LovelaceLogger.debug(Stream.of(ImageIO.getReaderFileSuffixes()).filter(Objects::nonNull)
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
		final BufferedImage retval = new BufferedImage(FIXTURE_ICON_SIZE, FIXTURE_ICON_SIZE,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics pen = retval.createGraphics();
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
		for (final TileType tileType : TileType.values()) {
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
	 * TODO: Return null instead of throwing if not loadable?
	 *
	 * @throws IOException If no reader could read the file (or the file does not exist)
	 */
	public static Image loadImage(final String file) throws IOException {
		if (IMAGE_CACHE.containsKey(file)) {
			return IMAGE_CACHE.get(file);
		} else {
			try (final ResourceInputStream res = new ResourceInputStream("images/" + file,
					ImageLoader.class)) { // TODO: Change back to IOHandler once ported?
				final Image image = ImageIO.read(res);
				if (Objects.isNull(image)) {
					throw new IOException("No reader could read the file images/" + file);
				} else {
					IMAGE_CACHE.put(file, image);
					return image;
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
			final Image orig = loadImage(file);
			final BufferedImage temp = new BufferedImage(FIXTURE_ICON_SIZE, FIXTURE_ICON_SIZE,
					BufferedImage.TYPE_INT_ARGB);
			final Graphics pen = temp.createGraphics();
			pen.drawImage(orig, 0, 0, temp.getWidth(), temp.getHeight(), null);
			pen.dispose();
			final Icon icon = new ImageIcon(temp);
			ICON_CACHE.put(file, icon);
			return icon;
		}
	}

	/**
	 * An encapsulation of the mapping from tile-types to colors.
	 */
	public static final class ColorHelper {
		private ColorHelper() {
		}

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
						.collect(Collectors.toMap(Function.identity(), t -> wrap(t.name()))));

		/**
		 * A map from types of features to the colors they can make the
		 * tile be. Used to show that a tile is forested, e.g., even
		 * when that is normally represented by an icon and there's a
		 * higher icon on the tile.
		 */
		private static final Map<Class<? extends TileFixture>, Color> FEATURE_COLORS;

		static {
			FEATURE_COLORS = Map.of(Forest.class, new Color(0, 117, 0), Oasis.class,
					new Color(72, 218, 164), Hill.class, new Color(141, 182, 0));
		}

		/**
		 * A map from map versions to maps from tile-types to colors.
		 */
		private static final Map<Integer, Map<TileType, Color>> COLORS = Map.of(2, verTwoColors());

		@SuppressWarnings("MagicNumber")
		private static Map<TileType, Color> verTwoColors() {
			final Map<TileType, Color> verTwo = new EnumMap<>(TileType.class);
			verTwo.put(TileType.Desert, new Color(249, 233, 28));
			verTwo.put(TileType.Jungle, new Color(229, 46, 46));
			verTwo.put(TileType.Ocean, Color.BLUE);
			verTwo.put(TileType.Plains, new Color(72, 218, 164));
			verTwo.put(TileType.Tundra, new Color(153, 153, 153));
			verTwo.put(TileType.Steppe, new Color(72, 100, 72));
			verTwo.put(TileType.Swamp, new Color(231, 41, 138));
			// TODO: Somehow check that all types in a version are covered?
			return Collections.unmodifiableMap(verTwo);
		}

		public static boolean supportsType(final int version, final TileType type) {
			return COLORS.getOrDefault(version, Collections.emptyMap()).containsKey(type);
		}

		/**
		 * Get the color to use for the given tile type in the given
		 * map version. Returns null if the given version does not
		 * support that tile type. TODO: throw instead, as we used to do in Java?
		 */
		public static @Nullable Color get(final int version, final @Nullable TileType type) {
			if (Objects.isNull(type)) {
				return null;
			} else if (COLORS.containsKey(version)) {
				final Map<TileType, Color> map = COLORS.get(version);
				if (map.containsKey(type)) {
					return map.get(type);
				} else {
					LovelaceLogger.error("Asked for unsupported type %s in version %d", type, version);
					return null;
				}
			} else {
				LovelaceLogger.error("Asked for %s in unsupported version %d", type, version);
				return null;
			}
		}

		/**
		 * Get a String (HTML) representation of the given terrain
		 * type. TODO: throw on not-found instead of returning null?
		 */
		public static @Nullable String getDescription(final @Nullable TileType type) {
			if (Objects.isNull(type)) {
				return "Unknown";
			} else if (DESCRIPTIONS.containsKey(type)) {
				return DESCRIPTIONS.get(type);
			} else {
				LovelaceLogger.error("No description found for tile type %s", type);
				return null;
			}
		}

		/**
		 * Get the color that a fixture should turn the tile if it's
		 * not on top. TODO: throw on not-found instead of returning
		 * null?
		 */
		public static @Nullable Color getFeatureColor(final TileFixture fixture) {
			if (FEATURE_COLORS.containsKey(fixture.getClass())) {
				return FEATURE_COLORS.get(fixture.getClass());
			} else {
				LovelaceLogger.warning("Asked for color for unsupported fixture: %s", fixture);
				return null;
			}
		}

		/**
		 * The color to use for background mountains.
		 */
		public static final Color MOUNTAIN_COLOR = new Color(249, 137, 28);
	}
}
