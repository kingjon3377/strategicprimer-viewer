package view.map.main;

import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;

import model.viewer.TileType;

/**
 * A class enapsulating the mapping from tile-types to colors.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class TileColors {
	/**
	 * The map we wrap.
	 */
	private final Map<TileType, Color> colors = new EnumMap<TileType, Color>(
			TileType.class);
	/**
	 * Constructor.
	 */
	// ESCA-JAVA0076:
	public TileColors() {
		colors.put(TileType.BorealForest, new Color(72, 218, 164));
		colors.put(TileType.Desert, new Color(249, 233, 28));
		colors.put(TileType.Jungle, new Color(229, 46, 46));
		colors.put(TileType.Mountain, new Color(249, 137, 28));
		colors.put(TileType.NotVisible, new Color(255, 255, 255));
		colors.put(TileType.Ocean, new Color(0, 0, 255));
		colors.put(TileType.Plains, new Color(0, 117, 0));
		colors.put(TileType.TemperateForest, new Color(72, 250, 72));
		colors.put(TileType.Tundra, new Color(153, 153, 153));
	}
	/**
	 * @param type a tile type
	 * @return its color, if any
	 */
	public Color get(final TileType type) {
		return colors.get(type);
	}
}
