package view.map.main;

import java.awt.Color;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import model.map.SPMapNG;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.terrain.Sandbar;

/**
 * A class encapsulating the mapping from tile-types to colors.
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
public final class TileUIHelper {
	/**
	 * Descriptions of the types.
	 */
	private final Map<TileType, String> descriptions = new EnumMap<>(TileType.class);

	/**
	 * A map from classes of features to the colors they can make the tile be. Used to
	 * show that a tile is mountainous or forested even when those are represented by
	 * icons and there's a higher icon on the tile.
	 */
	private final Map<Class<? extends TileFixture>, Color> featureColors;

	/**
	 * The map we wrap.
	 */
	private final Map<Integer, Map<TileType, Color>> colors;

	/**
	 * Constructor.
	 */
	@SuppressWarnings("deprecation")
	public TileUIHelper() {
		colors = new HashMap<>(SPMapNG.MAX_VERSION + 1);
		colors.put(Integer.valueOf(1), verOneColors());
		final Map<TileType, Color> verTwoColors = verTwoColors();
		colors.put(Integer.valueOf(2), verTwoColors);
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
	 * Create the version-2 terrains-to-colors mapping.
	 * @return a mapping from tile types to the colors used for a version-2 map.
	 */
	private static Map<TileType, Color> verTwoColors() {
		final Map<TileType, Color> verTwoColors = new EnumMap<>(TileType.class);
		verTwoColors.put(TileType.Desert, new Color(249, 233, 28));
		verTwoColors.put(TileType.Jungle, new Color(229, 46, 46));
		verTwoColors.put(TileType.NotVisible, Color.white);
		verTwoColors.put(TileType.Ocean, Color.blue);
		verTwoColors.put(TileType.Plains, new Color(72, 218, 164));
		verTwoColors.put(TileType.Tundra, new Color(153, 153, 153));
		verTwoColors.put(TileType.Steppe, new Color(72, 100, 72));
		return verTwoColors;
	}

	/**
	 * Create the version-1 terrains-to-colors mapping.
	 * @return a mapping from tile types to the colors used for a version-1 map
	 */
	@SuppressWarnings("deprecation")
	private static Map<TileType, Color> verOneColors() {
		final Map<TileType, Color> verOneColors = new EnumMap<>(TileType.class);
		verOneColors.put(TileType.BorealForest, new Color(72, 218, 164));
		verOneColors.put(TileType.Desert, new Color(249, 233, 28));
		verOneColors.put(TileType.Jungle, new Color(229, 46, 46));
		verOneColors.put(TileType.Mountain, new Color(249, 137, 28));
		verOneColors.put(TileType.NotVisible, Color.white);
		verOneColors.put(TileType.Ocean, Color.blue);
		verOneColors.put(TileType.Plains, new Color(0, 117, 0));
		verOneColors.put(TileType.TemperateForest, new Color(72, 250, 72));
		verOneColors.put(TileType.Tundra, new Color(153, 153, 153));
		return verOneColors;
	}

	/**
	 * Whether the given map version supports the given tile type.
	 * @param version a map version
	 * @param type    a tile type
	 * @return whether that map version supports that tile type
	 */
	public boolean supportsType(final int version, final TileType type) {
		final Integer ver = Integer.valueOf(version);
		return colors.containsKey(ver) && colors.get(ver).containsKey(type);
	}

	/**
	 * Get the color to use for the given tile type in the given map version.
	 * @param version what version the map is
	 * @param type    a tile type
	 * @return the tile's color, if any, under that map version
	 */
	public Color get(final int version, final TileType type) {
		final Integer ver = Integer.valueOf(version);
		if (colors.containsKey(ver)) {
			final Map<TileType, Color> colorMap = colors.get(ver);
			if (colorMap.containsKey(type)) {
				return colorMap.get(type);
			} else {
				throw new IllegalArgumentException(type +
														   " is not a terrain type " +
														   "version "
														   + version + " can handle");
			}
		} else {
			throw new IllegalArgumentException("Not a version we can handle");
		}
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object.
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TileUIHelper";
	}

	/**
	 * A String representation of the given terrain type.
	 * @param type a terrain type
	 * @return a String representation of that terrain type
	 */
	public String getDescription(final TileType type) {
		if (descriptions.containsKey(type)) {
			return descriptions.get(type);
		} else {
			throw new IllegalArgumentException("Not a type we know how to handle");
		}
	}

	/**
	 * Get the color that a fixture should turn the tile if it's not on top.
	 * @param fix a fixture
	 * @return the color it should turn the tile
	 */
	public Color getFeatureColor(final TileFixture fix) {
		final Class<? extends TileFixture> cls = fix.getClass();
		if (featureColors.containsKey(cls)) {
			return featureColors.get(cls);
		} else {
			throw new IllegalArgumentException("Not a kind of fixture we can handle");
		}
	}
}
