package model.map;

/**
 * An "event" --- something, other than a fortress, unit, or river, that can be on a tile.
 * Intended to replace legacy/numeric "events."
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface IEvent extends TileFixture {
	/**
	 * TODO: Should perhaps be mutable, at least for some events
	 *
	 * @return the DC to discover the event.
	 */
	int getDC();

	/**
	 * @return exploration-result text for the event.
	 */
	String getText();
}
