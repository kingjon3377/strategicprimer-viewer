// $codepro.audit.disable numericLiterals
package view.map.main;

import java.awt.Color;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import model.map.SPMap;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.terrain.Sandbar;

/**
 * A class enapsulating the mapping from tile-types to colors.
 *
 * @author Jonathan Lovelace
 *
 */
public class TileUIHelper {
	/**
	 * The map we wrap.
	 */
	private final Map<Integer, Map<TileType, Color>> colors;

	/**
	 * Constructor.
	 */
	// ESCA-JAVA0076:
	@SuppressWarnings("deprecation")
	public TileUIHelper() {
		colors = new HashMap<>(SPMap.MAX_VERSION + 1);
		final Map<TileType, Color> one = new EnumMap<>(TileType.class);
		one.put(TileType.BorealForest, new Color(72, 218, 164));
		one.put(TileType.Desert, new Color(249, 233, 28));
		one.put(TileType.Jungle, new Color(229, 46, 46));
		one.put(TileType.Mountain, new Color(249, 137, 28));
		one.put(TileType.NotVisible, new Color(255, 255, 255));
		one.put(TileType.Ocean, new Color(0, 0, 255));
		one.put(TileType.Plains, new Color(0, 117, 0));
		one.put(TileType.TemperateForest, new Color(72, 250, 72));
		one.put(TileType.Tundra, new Color(153, 153, 153));
		colors.put(Integer.valueOf(1), one);
		final Map<TileType, Color> two = new EnumMap<>(TileType.class);
		two.put(TileType.Desert, new Color(249, 233, 28));
		two.put(TileType.Jungle, new Color(229, 46, 46));
		two.put(TileType.NotVisible, new Color(255, 255, 255));
		two.put(TileType.Ocean, new Color(0, 0, 255));
		two.put(TileType.Plains, new Color(72, 218, 164));
		two.put(TileType.Tundra, new Color(153, 153, 153));
		two.put(TileType.Steppe, new Color(72, 100, 72));
		colors.put(Integer.valueOf(2), two);
		descriptions.put(TileType.BorealForest,
				"<html><p>Boreal Forest</p></html>");
		descriptions.put(TileType.Desert, "<html><p>Desert</p></html>");
		descriptions.put(TileType.Jungle, "<html><p>Jungle</p></html>");
		descriptions.put(TileType.Mountain, "<html><p>Mountains</p></html>");
		descriptions.put(TileType.NotVisible, "<html><p>Unknown</p></html>");
		descriptions.put(TileType.Ocean, "<html><p>Ocean</p></html>");
		descriptions.put(TileType.Plains, "<html><p>Plains</p></html>");
		descriptions.put(TileType.TemperateForest,
				"<html><p>Temperate Forest</p></html>");
		descriptions.put(TileType.Tundra, "<html><p>Tundra</p></html>");
		descriptions.put(TileType.Steppe, "<html><p>Steppe</p></html>");
		featureColors = new HashMap<>();
		featureColors.put(Forest.class, new Color(0, 117, 0));
		featureColors.put(Mountain.class, new Color(249, 137, 28));
		featureColors.put(Oasis.class, new Color(72, 218, 164));
		featureColors.put(Sandbar.class, new Color(249, 233, 28));
		featureColors.put(Hill.class, new Color(141, 182, 0));
	}

	/**
	 * @param version what version the map is
	 * @param type a tile type
	 *
	 * @return the tile's color, if any, under that map version
	 */
	public Color get(final int version, final TileType type) {
		final Integer ver = Integer.valueOf(version);
		if (colors.containsKey(ver)) {
			final Map<TileType, Color> colorMap = colors.get(ver);
			if (colorMap.containsKey(type)) {
				final Color retval = colorMap.get(type);
				assert retval != null;
				return retval;
			} else {
				throw new IllegalArgumentException(type + "is not a terrain type we know how to handle in version " + version);
			}
		} else {
			throw new IllegalArgumentException("Not a version we know how to handle");
		}
	}

	/**
	 *
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "TileUIHelper";
	}

	/**
	 * Descriptions of the types.
	 */
	private final Map<TileType, String> descriptions = new EnumMap<>(
			TileType.class);

	/**
	 * @param type a terrain type
	 *
	 * @return a String representation of that terrain type
	 */
	public String getDescription(final TileType type) { // NOPMD
		if (descriptions.containsKey(type)) {
			final String retval = descriptions.get(type);
			assert retval != null;
			return retval;
		} else {
			throw new IllegalArgumentException("Not a type we know how to handle");
		}
	}

	/**
	 * A map from classes of features to the colors they can make the tile be.
	 * Used to show that a tile is mountainous or forested even when those are
	 * represented by icons and there's a higher icon on the tile.
	 */
	private final Map<Class<? extends TileFixture>, Color> featureColors;

	/**
	 * @param fix a fixture
	 * @return the color it should turn the tile
	 */
	public Color getFeatureColor(final TileFixture fix) {
		final Class<? extends TileFixture> cls = fix.getClass();
		if (featureColors.containsKey(cls)) {
			final Color retval = featureColors.get(cls);
			assert retval != null;
			return retval;
		} else {
			throw new IllegalArgumentException("Not a kind of fixture we know how to handle");
		}
	}
}
