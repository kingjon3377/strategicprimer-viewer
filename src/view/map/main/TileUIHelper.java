// $codepro.audit.disable numericLiterals
package view.map.main;

import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;

import model.map.TileType;

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
	private final Map<TileType, Color> colors = new EnumMap<TileType, Color>(
			TileType.class);

	/**
	 * Constructor.
	 */
	// ESCA-JAVA0076:
	public TileUIHelper() {
		colors.put(TileType.BorealForest, new Color(72, 218, 164));
		colors.put(TileType.Desert, new Color(249, 233, 28));
		colors.put(TileType.Jungle, new Color(229, 46, 46));
		colors.put(TileType.Mountain, new Color(249, 137, 28));
		colors.put(TileType.NotVisible, new Color(255, 255, 255));
		colors.put(TileType.Ocean, new Color(0, 0, 255));
		colors.put(TileType.Plains, new Color(0, 117, 0));
		colors.put(TileType.TemperateForest, new Color(72, 250, 72));
		colors.put(TileType.Tundra, new Color(153, 153, 153));
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
	}

	/**
	 * @param type
	 *            a tile type
	 * 
	 * @return its color, if any
	 */
	public Color get(final TileType type) {
		return colors.get(type);
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
	private final Map<TileType, String> descriptions = new EnumMap<TileType, String>(
			TileType.class);

	/**
	 * @param type
	 *            a terrain type
	 * 
	 * @return a String representation of that terrain type
	 */
	public String getDescription(final TileType type) { // NOPMD
		return descriptions.get(type);
	}
}
