package model.viewer;

import model.map.TileFixture;

/**
 * An interface for a filter to tell whether a given fixture should be
 * displayed.
 *
 * @author Jonathan Lovelace
 *
 */
public interface ZOrderFilter {
	/**
	 * @param fix a fixture
	 * @return whether it should be displayed or not
	 */
	boolean shouldDisplay(final TileFixture fix);
}
