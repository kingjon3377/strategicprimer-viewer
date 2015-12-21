package view.map.main;

import model.viewer.ZOrderFilter;
import util.NullCleaner;

import java.awt.image.ImageObserver;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory for TileDrawHelpers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class TileDrawHelperFactory {
	/**
	 * An instance of this class, for callers who don't want to create their own.
	 */
	public static final TileDrawHelperFactory INSTANCE = new TileDrawHelperFactory();
	/**
	 * A version-1 tile draw helper.
	 */
	private final TileDrawHelper verOneHelper = new DirectTileDrawHelper();
	/**
	 * A mapping from ImageObservers to version-2 helpers.
	 */
	private final Map<ImageObserver, TileDrawHelper> verTwoHelpers = new HashMap<>();

	/**
	 * Constructor.
	 */
	private TileDrawHelperFactory() {
		// Do nothing.
	}

	/**
	 * @param version the version of the map that'll be drawn
	 * @param iobs    the object to be notified when images finish drawing
	 * @param zof     a filter to tell a ver-two helper which tiles to draw
	 * @return a draw helper for the specified map version
	 */
	public TileDrawHelper factory(final int version, final ImageObserver iobs,
	                              final ZOrderFilter zof) {
		switch (version) {
		case 1:
			return verOneHelper; // NOPMD
		case 2:
			if (!verTwoHelpers.containsKey(iobs)) {
				verTwoHelpers.put(iobs, new Ver2TileDrawHelper(iobs, zof));
			}
			return NullCleaner.assertNotNull(verTwoHelpers.get(iobs));
		default:
			throw new IllegalArgumentException("Unsupported map version");
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TileDrawHelperFactory";
	}
}
