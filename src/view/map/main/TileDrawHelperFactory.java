package view.map.main;

import java.awt.image.ImageObserver;
import model.viewer.FixtureMatcher;
import model.viewer.ZOrderFilter;

/**
 * A factory for TileDrawHelpers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
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
	 * Constructor.
	 */
	private TileDrawHelperFactory() {
		// Do nothing.
	}

	/**
	 * @param version the version of the map that'll be drawn
	 * @param observer    the object to be notified when images finish drawing
	 * @param zof     a filter to tell a ver-two helper which tiles to draw
	 * @param matchers a series of matchers to tell a ver-two helper which fixture is on
	 *                    top.
	 * @return a draw helper for the specified map version
	 */
	public TileDrawHelper factory(final int version, final ImageObserver observer,
								  final ZOrderFilter zof,
								  final Iterable<FixtureMatcher> matchers) {
		switch (version) {
		case 1:
			return verOneHelper;
		case 2:
			return new Ver2TileDrawHelper(observer, zof, matchers);
		default:
			throw new IllegalArgumentException("Unsupported map version");
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TileDrawHelperFactory";
	}
}
